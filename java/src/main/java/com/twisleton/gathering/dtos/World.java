package com.twisleton.gathering.dtos;

import java.util.HashMap;

public record World(int maxX, int maxY, HashMap<String, User> users) {
};
