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

    public GameService(@Value("${world.x.limit:100}") int worldMaxXCoordinate,
                       @Value("${world.y.limit:100}") int worldMaxYCoordinate,
                       @Value("${user.save.path:gamedata/users.json}") String userSavePath) {
        this.worldMaxXCoordinate = worldMaxXCoordinate;
        this.worldMaxYCoordinate = worldMaxYCoordinate;
        this.userSavePath = Paths.get(userSavePath);
        HashMap<String, User> users = UserPersistence.loadUsers(this.userSavePath);
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
        String id = socket.getRemoteSocketAddress().getHostString();
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
        User existingUserWithUpdatedConnectionTime =
                new User(user.id(), user.position(), user.color(), Instant.now().toString());
        world.users().put(user.id(), existingUserWithUpdatedConnectionTime);
        logger.info("User {} connected at {}, last connection was {}",
                user.id(),
                existingUserWithUpdatedConnectionTime.lastConnectionTime(),
                user.lastConnectionTime());
    }

    public void handleMessage(WebSocket socket, String received) {
        Message message = gson.fromJson(received, Message.class);
        if (message.id().equals("move")) {
            handleMovement(socket, message);
        } else {
            logger.info("unknown request received: {}", message.id());
        }
    }

    private void handleMovement(WebSocket socket, Message directionMessage) {
        Direction direction = Direction.valueOf((String) directionMessage.message());
        Optional<User> user = Optional.ofNullable(world.users().get(socket.getRemoteSocketAddress().getHostString()));
        logger.info("movement request received!");
        if (user.isEmpty()) {
            handleNewUser(socket.getRemoteSocketAddress().getHostString());
        } else {
            movePlayer(user.get(), direction);
        }
    }

    private void movePlayer(User user, Direction direction) {
        logger.info("Moving User: {} in direction: {}", user.id(), direction);
        int playerX = user.position().x;
        int playerY = user.position().y;
        Point newPosition = null;
        switch (direction) {
            case UP -> newPosition = new Point(playerX, playerY - 1);
            case DOWN -> newPosition = new Point(playerX, playerY + 1);
            case LEFT -> newPosition = new Point(playerX - 1, playerY);
            case RIGHT -> newPosition = new Point(playerX + 1, playerY);
        }
        world.users().put(user.id(), new User(user.id(), newPosition, user.color(), user.lastConnectionTime()));
        updateClientMaps();
    }

    private void updateClientMaps() {
        for (WebSocket connection : connections) {
            connection.send(gson.toJson(new Message("world", world)));
        }
    }

    private Point generateRandomCoordinates() {
        return new Point((int) ((Math.random() * (worldMaxXCoordinate)) + 0), (int) ((Math.random() * (worldMaxYCoordinate)) + 0));
    }

    // todo - probably best to prevent colors that are too light from being generated
    private String generateRandomColor() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder("#");
        while (sb.length() < 6) {
            sb.append(Integer.toHexString(r.nextInt()));
        }

        // prevent anything longer than 7
        return sb.toString().substring(0, 7);
    }
}
