package com.javaworld.core;

import com.javaworld.core.entity.Entity;
import com.javaworld.core.update.EntityUpdate;

import java.util.ArrayList;
import java.util.List;

import static com.javaworld.core.update.UpdateType.CREATE;
import static com.javaworld.core.update.UpdateType.REMOVE;

public class ServerWorld extends World {
    private final List<EntityUpdate> entityUpdates;

    public ServerWorld() {
        super(0);
        this.entityUpdates = new ArrayList<>();
    }

    public void addWorldTime() {
        worldTime++;
    }

    public void createEntity(Entity entity) {
        entity.setWorld(this);
        entity.setSerial(entitySerial);
        entities.put(entitySerial++, entity);
        entityUpdates.add(new EntityUpdate(CREATE, entity));
    }

    @Override
    public void removeEntity(Entity entity) {
        entity.setWorld(null);
        entities.remove(entity.getSerial());
        entityUpdates.add(new EntityUpdate(REMOVE, entity));
    }

    public void getEntityUpdates(List<EntityUpdate> output) {
        output.addAll(entityUpdates);
        entityUpdates.clear();
    }

    public int getEntityUpdateCount() {
        return entityUpdates.size();
    }
}
