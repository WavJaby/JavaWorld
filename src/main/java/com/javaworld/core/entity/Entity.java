package com.javaworld.core.entity;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.World;
import com.javaworld.adapter.entity.EntityType;
import lombok.Getter;
import lombok.Setter;

public abstract class Entity implements com.javaworld.adapter.entity.Entity {
    public final EntityType type;
    protected final Vec2 position;
    @Setter
    protected float direction;
    @Setter
    protected World world;

    // Internal
    @Getter
    final Vec2 velocity = new Vec2(0, 0);
    @Getter
    @Setter
    int serial = -1;
    @Getter
    @Setter
    Vec2 currentDest = null;

    public Entity(EntityType type, Vec2 position, float direction) {
        this.type = type;
        this.position = position;
        this.direction = direction;
    }

    public void setVelocity(Vec2 velocity) {
        this.velocity.set(velocity);
    }

    public void setVelocityZero() {
        this.velocity.setZero();
    }

    public void setPosition(Vec2 position) {
        this.position.set(position);
    }

    @Override
    public boolean isMoving() {
        return velocity.length() > 0;
    }

    @Override
    public Vec2 getPosition() {
        return new Vec2(position);
    }

    @Override
    public float getDirection() {
        return direction;
    }

    @Override
    public World getWorld() {
        return world;
    }
}
