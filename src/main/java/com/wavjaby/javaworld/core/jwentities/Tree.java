package com.wavjaby.javaworld.core.jwentities;

import com.almasb.fxgl.core.math.Vec2;
import com.wavjaby.javaworld.adapter.entity.EntityType;
import com.wavjaby.javaworld.core.entity.Entity;

public class Tree extends Entity {
    public Tree(Vec2 position, float direction) {
        super(EntityType.PLANT, position, direction);
    }
}
