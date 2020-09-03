package com.twisleton.gathering.dtos;

import java.awt.geom.Point2D;
import java.util.UUID;

public record User(
        UUID id,
        Point2D.Double position,
        String color,
        String lastConnectionTime
) {
};
