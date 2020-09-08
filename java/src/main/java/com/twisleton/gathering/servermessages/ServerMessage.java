package com.twisleton.gathering.servermessages;

import com.google.gson.Gson;

// type tag for deserialization
enum ServerMessageType {
    UPDATE_WHOLE_WORLD,
    USER_MOVE
}

public interface ServerMessage {
    Gson gson = new Gson();

    ServerMessageType type();

    default String serialize() {
        return gson.toJson(this);
    }

}
