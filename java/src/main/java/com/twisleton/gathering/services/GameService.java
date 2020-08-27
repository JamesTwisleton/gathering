package com.twisleton.gathering.services;

import com.twisleton.gathering.records.User;
import com.twisleton.gathering.records.World;
import com.twisleton.gathering.server.GatheringServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.HashMap;

@Service
public class GameService {

    private Logger logger = LoggerFactory.getLogger(GatheringServer.class);
    private World world;
    private final int worldMaxXCoordinate;
    private final int worldMaxYCoordinate;

    public GameService(@Value("${world.x.limit:100}") int worldMaxXCoordinate,
                       @Value("${world.y.limit:100}") int worldMaxYCoordinate) {
        this.worldMaxXCoordinate = worldMaxXCoordinate;
        this.worldMaxYCoordinate = worldMaxYCoordinate;
        world = new World(
                worldMaxXCoordinate,
                worldMaxYCoordinate,
                new HashMap<String, User>());
    }

    public World getWorld() {
        return world;
    }

    public User createUser(String id) {
        User user = new User(id, generateRandomCoordinate());
        world.users().put(id, user);
        logger.info("User {} added!", user.id());
        return user;
    }

    private Point generateRandomCoordinate() {
        return new Point((int) ((Math.random() * (worldMaxXCoordinate - 0)) + 0), (int) ((Math.random() * (worldMaxXCoordinate - 0)) + 0));
    }
}
