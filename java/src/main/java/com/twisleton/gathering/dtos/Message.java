package com.twisleton.gathering.dtos;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

enum MessageType {
    USER_CONNECT,
    USER_MOVE;
}

public interface Message {
    static final Gson gson = new Gson();

    public MessageType type();

    public static Message parseMessage(String input) {
        var jsonObject = JsonParser.parseString(input).getAsJsonObject();
        var messageType = jsonObject.get("type").getAsString();
        return Message.fromTypeName(messageType);
    }

    public default String serialize() {
        return gson.toJson(this);
    }

    static Message fromTypeName(String input) {
        var messageType = MessageType.valueOf(input);
        return switch (messageType) {
            case USER_CONNECT ->
                    gson.fromJson(input, UserConnectMessage.class);
            case USER_MOVE ->
                    gson.fromJson(input, MoveMessage.class);
        };
    }
}