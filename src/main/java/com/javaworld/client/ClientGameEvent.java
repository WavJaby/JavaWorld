package com.javaworld.client;

import com.javaworld.core.entity.Entity;

public interface ClientGameEvent {
    void entityCreate(Entity e);

    void entityUpdate(Entity e);

    void entityRemove(Entity e);
}