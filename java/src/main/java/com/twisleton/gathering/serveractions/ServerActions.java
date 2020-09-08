package com.twisleton.gathering.serveractions;

import com.twisleton.gathering.dtos.User;

import java.net.InetSocketAddress;
import java.util.Set;

public class ServerActions {

    public static record None() implements ServerAction {
    }

    public static record UpdateWorld(Set<User> newUsers, User user) implements ServerAction {
    }

    public static record UserMoved(User userWithNewPosition) implements ServerAction {
    }

    public static record UserConnected(InetSocketAddress from, User user) implements ServerAction {
    }

    public static record UserDisconnected(InetSocketAddress from) implements ServerAction {
    }
}
