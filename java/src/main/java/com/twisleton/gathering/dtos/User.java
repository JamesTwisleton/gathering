package com.twisleton.gathering.dtos;

import java.awt.*;

public record User(String id, Point position, String color, String lastConnectionTime) {
};
