package com.twisleton.gathering.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.twisleton.gathering.dtos.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;


public class UserPersistence {
    private static final Logger logger = LoggerFactory.getLogger(UserPersistence.class);
    private static final Gson gson = new Gson();

    public static Set<User> loadUsers(Path path) {
        try {
            var bufferedReader = new BufferedReader(new FileReader(path.toFile()));
            var type = new TypeToken<Set<User>>() {}.getType();
            Set<User> usersFromFile = gson.fromJson(bufferedReader, type);

            return usersFromFile;
        } catch (IOException e) {
            logger.warn("failed to load users from disk; created empty user set", e);
            return Collections.emptySet();
        } catch (JsonSyntaxException | JsonIOException e) {
            logger.error("Failed to load users from disk: ", e);
            return Collections.emptySet();
        }
    }

    public static void saveUsers(Set<User> users, Path path) {
        try {
            var fileWriter = new FileWriter(path.toFile());
            gson.toJson(users, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            logger.info("Failed to save state on shutdown: ", e);
        }
    }
}
