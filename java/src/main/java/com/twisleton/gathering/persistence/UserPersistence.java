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
import java.util.HashMap;

public class UserPersistence {
    private static final Logger logger = LoggerFactory.getLogger(UserPersistence.class);
    private static final Gson gson = new Gson();

    public static HashMap<String, User> loadUsers(Path path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toFile()));
            Type type = new TypeToken<HashMap<String, User>>() {
            }.getType();
            HashMap<String, User> usersFromFile = gson.fromJson(bufferedReader, type);
            if(!java.util.Objects.isNull(usersFromFile)){
                return usersFromFile;
            }
            return new HashMap<String, User>();
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            logger.info("Failed to load users from disk: ", e);
            return new HashMap<String, User>();
        }
    }

    public static void saveUsers(HashMap<String, User> users, Path path) {
        try {
            FileWriter fileWriter = new FileWriter(path.toFile());
            gson.toJson(users, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            logger.info("Failed to save state on shutdown: ", e);
        }
    }
}
