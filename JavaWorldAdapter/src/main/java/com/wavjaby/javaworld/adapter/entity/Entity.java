package com.wavjaby.javaworld.adapter.entity;

import com.almasb.fxgl.core.math.Vec2;
import com.wavjaby.javaworld.adapter.World;

public interface Entity {
    Vec2 getPosition();

    float getDirection();

    World getWorld();

    boolean isMoving();
}
