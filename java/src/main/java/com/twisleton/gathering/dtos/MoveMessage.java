package com.twisleton.gathering.dtos;

public record MoveMessage( ) implements Message {
    public MessageType type() {
        return MessageType.USER_MOVE;
    }
}
