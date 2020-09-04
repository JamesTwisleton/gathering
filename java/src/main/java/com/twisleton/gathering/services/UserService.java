package com.twisleton.gathering.services;

import com.twisleton.gathering.dtos.User;
import com.twisleton.gathering.persistence.UserPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

@Service
public class UserService {

    final Set<User> connectedUsers;
    final Path userSavePath;
    Set<User> allUsers;

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
        this.connectedUsers = new HashSet<>();
    }

    public void saveUsers() {
        UserPersistence.saveUsers(this.allUsers, this.userSavePath);
    }

    public void connectUser(UUID userId) {
        final var existingUser = this.findById(userId);
        existingUser.ifPresentOrElse(
                connectedUsers::add,
                () -> {
                    var user = this.generateNewUser(userId);
                    connectedUsers.add(user);
                    allUsers.add(user);
                }
        );
    }

    public Optional<User> findById(UUID userId) {
        return allUsers.stream().filter(u ->
            u.id().equals(userId)
        ).findAny();
    }

    public void disconnectUser(User user) {
        connectedUsers.remove(user);
    }

    public void loadUsers() {
        this.allUsers = UserPersistence.loadUsers(this.userSavePath);
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
