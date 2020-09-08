package com.twisleton.gathering.servermessages;

import com.twisleton.gathering.dtos.User;

import java.util.Set;
import java.util.UUID;

public class ServerMessages {

    public static record UpdateWholeWord(Set<User> users) implements ServerMessage {
        public ServerMessageType type() {
            return ServerMessageType.UPDATE_WHOLE_WORLD;
        }
    }

    public static record UserMove(UUID userId) implements ServerMessage {
        public ServerMessageType type() {
            return ServerMessageType.USER_MOVE;
        }
    }

}
