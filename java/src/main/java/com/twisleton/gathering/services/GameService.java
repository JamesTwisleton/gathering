package com.twisleton.gathering.services;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.twisleton.gathering.dtos.*;
import com.twisleton.gathering.persistence.UserPersistence;
import com.twisleton.gathering.server.GatheringServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ArrayList<WebSocket> connections;
    private final int worldMaxXCoordinate;
    private final int worldMaxYCoordinate;
    private final UserService userService;

    private final double playerMoveDistance = 0.25;
    private final double playerDiagonalMoveDistance = playerMoveDistance * Math.sqrt(2) / 2;

    public GameService(
            @Value("${world.x.limit:100}") int worldMaxXCoordinate,
            @Value("${world.y.limit:100}") int worldMaxYCoordinate,
            @Autowired UserService userService
    ) {
        this.worldMaxXCoordinate = worldMaxXCoordinate;
        this.worldMaxYCoordinate = worldMaxYCoordinate;
        world = new World(
                worldMaxXCoordinate,
                worldMaxYCoordinate
                );
        connections = new ArrayList<WebSocket>();
        this.userService = userService;
    }

    public void dispatchOnMessageType(WebSocket socket, String received) {
        var message = Message.parseMessage(received);
        if (message instanceof UserConnectMessage connectMessage)
            userService.connectUser(connectMessage.id());
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

}
