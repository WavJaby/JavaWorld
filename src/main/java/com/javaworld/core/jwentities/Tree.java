package com.javaworld.core.jwentities;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.core.World;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.entity.EntityData;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

import static com.javaworld.core.GameManager.TICK;

public class Tree extends Entity {
    @Setter
    @Getter
    Player owner = null;
    long nextAddTime = -1;

    public Tree(EntityData entityData, Vec2 position, float direction) {
        super(entityData, position, direction);
    }

    @Override
    public void tickUpdate() {
        if (owner == null) {
            // Tree die if no owner
            Random r = new Random();
            if (world.getWorldTime() - nextAddTime > (long) TICK * r.nextInt(10, 20)) {
                ((World) world).removeEntity(this);
            }
            return;
        }
        if (world.getWorldTime() < nextAddTime) return;
        nextAddTime = world.getWorldTime() + 20;

        owner.score++;
    }
}
