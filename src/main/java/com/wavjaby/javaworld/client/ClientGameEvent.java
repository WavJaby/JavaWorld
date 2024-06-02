package com.wavjaby.javaworld.client;

import com.wavjaby.javaworld.core.entity.Entity;

public interface ClientGameEvent {
    void entityCreate(Entity e);

    void entityUpdate(Entity e);

    void entityRemove(Entity e);
}
