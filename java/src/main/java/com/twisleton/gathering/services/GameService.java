package com.twisleton.gathering.services;

import com.twisleton.gathering.clientmessages.ClientMessage;
import com.twisleton.gathering.clientmessages.ClientMessages;
import com.twisleton.gathering.dtos.*;
import com.twisleton.gathering.server.GatheringServer;
import com.twisleton.gathering.serveractions.ServerAction;
import com.twisleton.gathering.serveractions.ServerActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;

@Service
public class GameService {

    private final Logger logger = LoggerFactory.getLogger(GatheringServer.class);
    private final World world;
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
        this.userService = userService;
    }

    public ServerAction interpretClientMessage(String received) {
        var message = ClientMessage.parseMessage(received);
        if (message instanceof ClientMessages.UserConnect connectMessage) {
            userService.connectUser(connectMessage.userId());
            return new ServerActions.None();
        } else if (message instanceof ClientMessages.Move moveMessage) {
            return handleMovement(moveMessage);
        }
        throw new RuntimeException("Missing action handler for " + message);
    }

    private ServerActions.UpdateWorld handleMovement(ClientMessages.Move moveMessage) {
        logger.info("movement request received! {}", moveMessage);
        var newWorld = userService.findById(moveMessage.userId())
                .map(u -> movePlayer(u, moveMessage.direction()))
                .orElseThrow(() -> new RuntimeException("Missing player??"));

        return new ServerActions.UpdateWorld(newWorld);
    }

    private World movePlayer(User user, Direction direction) {
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
        // TODO UPDATE WORLD
        return new World(0, 0);
        // world.users().put(user.id(), new User(user.id(), newPosition, user.color(), user.lastConnectionTime()));
    }

    /**
     * The goal of this method is to handle the pathing during a player move.
     * This would include things such as collisions with other players, but
     * for now, I'm just doing the world coordinates because when I am testing
     * I don't want my characters flying off the screen.
     *
     * @return if the move is invalid, the user's original location; else, a new location from the move
     */
    private Point2D.Double checkPathing(Point2D.Double currentPosition, double difX, double difY) {
        double newX = Math.max(0, Math.min(currentPosition.getX() + difX, world.maxX()));
        double newY = Math.max(0, Math.min(currentPosition.getY() + difY, world.maxY()));
        return new Point2D.Double(newX, newY);
    }

}
