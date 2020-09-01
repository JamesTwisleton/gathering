package com.twisleton.gathering.services;

import com.google.gson.Gson;
import com.twisleton.gathering.dtos.Direction;
import com.twisleton.gathering.dtos.Message;
import com.twisleton.gathering.dtos.User;
import com.twisleton.gathering.dtos.World;
import com.twisleton.gathering.persistence.UserPersistence;
import com.twisleton.gathering.server.GatheringServer;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.awt.*;
import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

@Service
public class GameService {

    private final Logger logger = LoggerFactory.getLogger(GatheringServer.class);
    private final World world;
    private final Path userSavePath;
    private final ArrayList<WebSocket> connections;
    private final int worldMaxXCoordinate;
    private final int worldMaxYCoordinate;
    private final Gson gson = new Gson();

    private final double playerMoveDistance = 0.25;
    private final double playerDiagonalMoveDistance = playerMoveDistance * Math.sqrt(2) / 2;

    public GameService(@Value("${world.x.limit:100}") int worldMaxXCoordinate,
                       @Value("${world.y.limit:100}") int worldMaxYCoordinate,
                       @Value("${user.save.path:gamedata/users.json}") String userSavePath) {
        this.worldMaxXCoordinate = worldMaxXCoordinate;
        this.worldMaxYCoordinate = worldMaxYCoordinate;
        this.userSavePath = Paths.get(userSavePath);
        var users = UserPersistence.loadUsers(this.userSavePath);
        world = new World(
                worldMaxXCoordinate,
                worldMaxYCoordinate,
                users);
        connections = new ArrayList<WebSocket>();
    }

    @PreDestroy
    public void persistOnShutdown() {
        UserPersistence.saveUsers(world.users(), userSavePath);
    }

    public void handleUserConnection(WebSocket socket) {
        connections.add(socket);
        var id = socket.getRemoteSocketAddress().getHostString();
        if (world.users().isEmpty()) {
            handleNewUser(id);
        } else {
            Optional.ofNullable(world.users().get(id)).ifPresentOrElse(this::handleExistingUser, () -> {
                handleNewUser(id);
            });
        }
        socket.send(gson.toJson(new Message("world", world)));
    }

    public void handleUserDisconnection(WebSocket socket) {
        connections.remove(socket);
    }

    private void handleNewUser(String id) {
        world.users().put(id, new User(id, generateRandomCoordinates(), generateRandomColor(), Instant.now().toString()));
        logger.info("User {} added to world!", id);
    }

    private void handleExistingUser(User user) {
        var existingUserWithUpdatedConnectionTime =
                new User(user.id(), user.position(), user.color(), Instant.now().toString());
        world.users().put(user.id(), existingUserWithUpdatedConnectionTime);
        logger.info("User {} connected at {}, last connection was {}",
                user.id(),
                existingUserWithUpdatedConnectionTime.lastConnectionTime(),
                user.lastConnectionTime());
    }

    public void handleMessage(WebSocket socket, String received) {
        var message = gson.fromJson(received, Message.class);
        if (message.id().equals("move")) {
            handleMovement(socket, message);
        } else {
            logger.info("unknown request received: {}", message.id());
        }
    }

    private void handleMovement(WebSocket socket, Message directionMessage) {
        var direction = Direction.valueOf((String) directionMessage.message());
        var user = Optional.ofNullable(world.users().get(socket.getRemoteSocketAddress().getHostString()));
        logger.info("movement request received!");
        if (user.isEmpty()) {
            handleNewUser(socket.getRemoteSocketAddress().getHostString());
        } else {
            movePlayer(user.get(), direction);
        }
    }

    private void movePlayer(User user, Direction direction) {
        logger.info("Moving User: {} in direction: {}", user.id(), direction);
        var playerX = user.position().getX();
        var playerY = user.position().getY();
        Point2D.Double newPosition = null;
        switch (direction) {
            case NORTH -> newPosition = new Point2D.Double(playerX, playerY - playerMoveDistance);
            case SOUTH -> newPosition = new Point2D.Double(playerX, playerY + playerMoveDistance);
            case WEST -> newPosition = new Point2D.Double(playerX - playerMoveDistance, playerY);
            case EAST -> newPosition = new Point2D.Double(playerX + playerMoveDistance, playerY);
            case NORTHEAST -> newPosition = new Point2D.Double(playerX + playerDiagonalMoveDistance, playerY - playerDiagonalMoveDistance);
            case NORTHWEST -> newPosition = new Point2D.Double(playerX - playerDiagonalMoveDistance, playerY - playerDiagonalMoveDistance);
            case SOUTHEAST -> newPosition = new Point2D.Double(playerX + playerDiagonalMoveDistance, playerY + playerDiagonalMoveDistance);
            case SOUTHWEST -> newPosition = new Point2D.Double(playerX - playerDiagonalMoveDistance, playerY + playerDiagonalMoveDistance);
        }
        world.users().put(user.id(), new User(user.id(), newPosition, user.color(), user.lastConnectionTime()));
        updateClientMaps();
    }

    private void updateClientMaps() {
        for (WebSocket connection : connections) {
            connection.send(gson.toJson(new Message("world", world)));
        }
    }

    private Point2D.Double generateRandomCoordinates() {
        return new Point2D.Double((Math.random() * (worldMaxXCoordinate)) + 0, (Math.random() * (worldMaxYCoordinate)) + 0);
    }

    // todo - probably best to prevent colors that are too light from being generated
    private String generateRandomColor() {
        var r = new Random();
        var sb = new StringBuilder("#");
        while (sb.length() < 7) {
            sb.append(Integer.toHexString(r.nextInt()));
        }

        // prevent anything longer than 7
        return sb.toString().substring(0, 7);
    }
}
