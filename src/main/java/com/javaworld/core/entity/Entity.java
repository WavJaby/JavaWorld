package com.javaworld.core.entity;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.World;
import com.javaworld.adapter.entity.EntityID;
import lombok.Getter;
import lombok.Setter;

public abstract class Entity implements com.javaworld.adapter.entity.Entity {
    public final EntityData entityData;
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

    public Entity(EntityData entityData, Vec2 position, float direction) {
        this.entityData = entityData;
        this.position = position;
        this.direction = direction;
    }

    public abstract void tickUpdate();

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

    @Override
    public EntityID getEntityID() {
        return entityData;
    }
}
