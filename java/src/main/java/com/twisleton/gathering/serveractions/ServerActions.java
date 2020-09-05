package com.twisleton.gathering.serveractions;

import com.twisleton.gathering.dtos.User;
import com.twisleton.gathering.dtos.World;

public class ServerActions {

    public static record None() implements ServerAction { }

    public static record UpdateWorld(World newWorld) implements ServerAction { }

    public static record UserConnected(User user) implements ServerAction { }

}
