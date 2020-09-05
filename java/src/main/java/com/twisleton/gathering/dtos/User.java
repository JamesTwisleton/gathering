package com.twisleton.gathering.dtos;

import java.awt.geom.Point2D;
import java.util.UUID;

public record User(
        UUID id,
        Point2D.Double position,
        String color,
        String lastConnectionTime
) {

    public User updatePosition(Point2D.Double newPosition) {
        return new User(
            this.id,
            newPosition,
            color,
            lastConnectionTime
        );
    }

}

