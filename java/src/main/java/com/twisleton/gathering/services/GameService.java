package com.twisleton.gathering.services;

import com.google.gson.Gson;
import com.twisleton.gathering.records.Direction;
import com.twisleton.gathering.records.Message;
import com.twisleton.gathering.records.User;
import com.twisleton.gathering.records.World;
import com.twisleton.gathering.server.GatheringServer;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

@Service
public class GameService {

    private final Logger logger = LoggerFactory.getLogger(GatheringServer.class);
    private final World world;
    private final int worldMaxXCoordinate;
    private final int worldMaxYCoordinate;
    private final Gson gson = new Gson();

    public GameService(@Value("${world.x.limit:100}") int worldMaxXCoordinate,
                       @Value("${world.y.limit:100}") int worldMaxYCoordinate) {
        this.worldMaxXCoordinate = worldMaxXCoordinate;
        this.worldMaxYCoordinate = worldMaxYCoordinate;
        world = new World(
                worldMaxXCoordinate,
                worldMaxYCoordinate,
                new HashMap<String, User>());
    }

    public void handleUserConnection(WebSocket socket) {
        String id = socket.getRemoteSocketAddress().getHostString();
        if (world.users().isEmpty()) {
            handleNewUser(id);
        } else {
            Optional<User> optionalExistingUser = Optional.of(world.users().get(id));
            optionalExistingUser.ifPresentOrElse(this::handleExistingUser, () -> {
                handleNewUser(id);
            });
        }
        socket.send(gson.toJson(new Message("world", world)));
    }

    private void handleNewUser(String id) {
        world.users().put(id, new User(id, generateRandomCoordinates(), Instant.now().toString()));
        logger.info("User {} added to world!", id);
    }

    private void handleExistingUser(User user) {
        User existingUserWithUpdatedConnectionTime =
                new User(user.id(), user.position(), Instant.now().toString());
        world.users().put(user.id(), existingUserWithUpdatedConnectionTime);
        logger.info("User {} connected at {}, last connection was {}",
                user.id(),
                existingUserWithUpdatedConnectionTime.lastConnectionTime(),
                user.lastConnectionTime());
    }

    public void handleMessage(WebSocket socket, String message) {
        Message pong = new Message("pong", "Message received, my ID is " + socket.getLocalSocketAddress().toString());
        socket.send(gson.toJson(pong));
        Message received = gson.fromJson(message, Message.class);
        if (received.id().equals("move")) {
            Optional<User> user = Optional.of(world.users().get(socket.getRemoteSocketAddress().getHostString()));
            logger.info("movement request received!");
            Direction direction = Direction.valueOf((String) received.message());
            if(user.isEmpty()) {
                handleNewUser(socket.getRemoteSocketAddress().getHostString());
            } else {
                movePlayer(user.get(), direction);
            }
            logger.info("direction requested is {}", direction);
        } else {
            logger.info("unknown request received: {}", received.id());
        }
    }

    private void movePlayer(User user, Direction direction) {
        logger.info("Moving User: {} in direction: {}", user.id(), direction);
    }

    private Point generateRandomCoordinates() {
        return new Point((int) ((Math.random() * (worldMaxXCoordinate)) + 0), (int) ((Math.random() * (worldMaxYCoordinate)) + 0));
    }
}
