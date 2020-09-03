package com.twisleton.gathering.dtos;

import java.util.UUID;

public record UserConnectMessage(UUID id, Object message) implements Message {
    public MessageType type() {
        return MessageType.USER_CONNECT;
    }
}
