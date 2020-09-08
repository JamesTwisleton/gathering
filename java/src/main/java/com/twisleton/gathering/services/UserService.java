package com.twisleton.gathering.services;

import com.twisleton.gathering.dtos.Direction;
import com.twisleton.gathering.dtos.User;
import com.twisleton.gathering.persistence.UserPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

@Service
public class UserService {

    private final static double playerMoveDistance = 0.25;
    private final static double playerDiagonalMoveDistance = playerMoveDistance * Math.sqrt(2) / 2;

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    final Path userSavePath;
    Set<User> allUsers;
    final Map<InetSocketAddress, UUID> connectedUserIds;

    // TODO refactor out to world service or something
    private final int worldMaxXCoordinate;
    private final int worldMaxYCoordinate;

    public UserService(
            @Value("${user.save.path:gamedata/users.json}") String userSavePath,
            @Value("${world.x.limit:100}") int worldMaxXCoordinate,
            @Value("${world.y.limit:100}") int worldMaxYCoordinate
    ) {
        this.worldMaxXCoordinate = worldMaxXCoordinate;
        this.worldMaxYCoordinate = worldMaxYCoordinate;

        this.userSavePath = Paths.get(userSavePath);
        this.allUsers = new HashSet<>();
        connectedUserIds = new HashMap<>();
    }

    public void saveUsers() {
        UserPersistence.saveUsers(this.allUsers, this.userSavePath);
    }

    public User getOrCreateUser(UUID userId) {
        var user = this.findById(userId)
                .orElseGet(() -> this.generateNewUser(userId));
        allUsers.add(user);
        return user;
    }

    public Optional<User> findById(UUID userId) {
        return allUsers.stream().filter(u ->
                u.id().equals(userId)
        ).findAny();
    }

    public void loadUsers() {
        this.allUsers = UserPersistence.loadUsers(this.userSavePath);
    }

    public User movePlayer(User user, Direction direction) {
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
        // hmm maybe this is a mess. idk.
        var updatedUser = user.updatePosition(newPosition);
        if (this.allUsers.remove(user)) {
            this.allUsers.add(updatedUser);
        }
        return updatedUser;
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
        double newX = Math.max(0, Math.min(currentPosition.getX() + difX, worldMaxXCoordinate));
        double newY = Math.max(0, Math.min(currentPosition.getY() + difY, worldMaxYCoordinate));
        return new Point2D.Double(newX, newY);
    }

    private Point2D.Double generateRandomCoordinates() {
        return new Point2D.Double(
                (Math.random() * (worldMaxXCoordinate)) + 0,
                (Math.random() * (worldMaxYCoordinate)) + 0
        );
    }

    private User generateNewUser(UUID newUserId) {
        return new User(
                newUserId,
                generateRandomCoordinates(),
                generateRandomColor(),
                Instant.now().toString()
        );
    }

    /**
     * private static helper method to generate a random hexadecimal color code
     * simplest way I could think of to differentiate players, probably a set
     * color list or something like that would be better, but this works well
     * enough for testing and offers some basic variety.
     *
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
