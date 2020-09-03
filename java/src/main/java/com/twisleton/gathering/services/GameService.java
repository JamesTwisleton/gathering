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
        double difX = 0, difY = 0;
        switch (direction) {
            case NORTH -> difY = -playerMoveDistance;
            case SOUTH -> difY = playerMoveDistance;
            case WEST -> difX = -playerMoveDistance;
            case EAST -> difX = playerMoveDistance;
            case NORTHEAST -> {
                difX = playerDiagonalMoveDistance;
                difY = -playerDiagonalMoveDistance;
            }
            case NORTHWEST -> {
                difX = -playerDiagonalMoveDistance;
                difY = -playerDiagonalMoveDistance;
            }
            case SOUTHEAST -> {
                difX = playerDiagonalMoveDistance;
                difY = playerDiagonalMoveDistance;
            }
            case SOUTHWEST -> {
                difX = -playerDiagonalMoveDistance;
                difY = playerDiagonalMoveDistance;
            }
        }
        Point2D.Double newPosition = checkPathing(user.position(), difX, difY);
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

    /**
     * The goal of this method is to handle the pathing during a player move.
     * This would include things such as collisions with other players, but
     * for now, I'm just doing the world coordinates because when I am testing
     * I don't want my characters flying off the screen.
     * @return if the move is invalid, the user's original location; else, a new location from the move
     */
    private Point2D.Double checkPathing(Point2D.Double currentPosition, double difX, double difY) {
        double newX = Math.max(0, Math.min(currentPosition.getX() + difX, world.maxX()));
        double newY = Math.max(0, Math.min(currentPosition.getY() + difY, world.maxY()));
        return new Point2D.Double(newX, newY);
    }

    /**
     * private static helper method to generate a random hexadecimal color code
     * simplest way I could think of to differentiate players, probably a set
     * color list or something like that would be better, but this works well
     * enough for testing and offers some basic variety.
     * @return a HTML-compatible hexadecimal color code
     */
    private static String generateRandomColor() {
        var r = new Random();
        var sb = new StringBuilder("#");
        while (sb.length() < 7) {
            sb.append(Integer.toHexString(r.nextInt()));
        }

        // prevent anything longer than 7
        return sb.toString().substring(0, 7);
    }
}
