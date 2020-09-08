package com.twisleton.gathering.servermessages;

import com.google.gson.Gson;

// type tag for deserialization
enum ServerMessageType {

    UPDATE_WHOLE_WORLD(ResponseStrategy.RESPOND_DIRECTLY),
    USER_MOVE(ResponseStrategy.RESPOND_ALL),
    USER_DISCONNECTED(ResponseStrategy.RESPOND_ALL);

    public final ResponseStrategy responseStrategy;

    ServerMessageType(ResponseStrategy responseStrategy) {
        this.responseStrategy = responseStrategy;
    }

}

public interface ServerMessage {

    Gson gson = new Gson();

    ServerMessageType type();

    default String serialize() {
        return gson.toJson(this);
    }

    default ResponseStrategy getResponseStrategy() {
        return type().responseStrategy;
    }

}
