package com.twisleton.gathering.dtos;

import java.awt.geom.Point2D;

public record User(String id, Point2D.Double position, String color, String lastConnectionTime) {
};
