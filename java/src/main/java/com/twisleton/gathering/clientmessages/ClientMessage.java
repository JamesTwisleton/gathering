package com.twisleton.gathering.clientmessages;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

enum MessageType {
    USER_CONNECT,
    USER_MOVE;
}

public interface ClientMessage {
    Gson gson = new Gson();

    MessageType type();

    static ClientMessage parseMessage(String input) {
        var jsonObject = JsonParser.parseString(input).getAsJsonObject();
        var messageType = jsonObject.get("type").getAsString();
        return ClientMessage.fromTypeName(messageType);
    }

    static ClientMessage fromTypeName(String input) {
        var messageType = MessageType.valueOf(input);
        return switch (messageType) {
            case USER_CONNECT ->
                    gson.fromJson(input, ClientMessages.UserConnect.class);
            case USER_MOVE ->
                    gson.fromJson(input, ClientMessages.Move.class);
        };
    }

    default String serialize() {
        return gson.toJson(this);
    }

}