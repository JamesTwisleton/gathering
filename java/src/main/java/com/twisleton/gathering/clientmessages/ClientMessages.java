package com.twisleton.gathering.clientmessages;

import com.twisleton.gathering.dtos.Direction;

import java.util.UUID;

public class ClientMessages {

    public static record Move(
            UUID userId,
            Direction direction
    ) implements ClientMessage {
        public MessageType type() {
            return MessageType.USER_MOVE;
        }
    }

    public static record UserConnect(
            UUID userId
    ) implements ClientMessage {
        public MessageType type() {
            return MessageType.USER_CONNECT;
        }
    }

}
