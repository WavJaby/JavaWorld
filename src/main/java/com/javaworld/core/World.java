package com.javaworld.core;

import com.javaworld.core.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class World implements com.javaworld.adapter.World {
    final Map<Integer, Map<Integer, Chunk>> chunks = new HashMap<>();
    final Map<Integer, Entity> entities = new HashMap<>();
    final long worldCreateTime;
    int entitySerial = 0;

    public World(long worldCreateTime) {
        this.worldCreateTime = worldCreateTime;
    }

    @Override
    public long getTime() {
        return System.currentTimeMillis() - worldCreateTime;
    }

    public Chunk loadChunk(int x, int y) {
        Map<Integer, Chunk> chunksY = chunks.computeIfAbsent(x, (a) -> new HashMap<>());
        Chunk chunk = chunksY.get(y);
        if (chunk == null) {
            chunk = new Chunk(x, y);
            chunksY.put(y, chunk);
        }
        return chunk;
    }

    public Entity getEntity(int serial) {
        return entities.get(serial);
    }

    public void createEntity(int serial, Entity entity) {
        entity.setWorld(this);
        entity.setSerial(serial);
        entities.put(serial, entity);
    }

    public void removeEntity(Entity entity) {
        entity.setWorld(null);
        entities.remove(entity.getSerial());
    }

    public void updateEntity(Entity entity, EntityUpdate update) {
        entity.setPosition(update.entityPosition);
        entity.setDirection(update.entityDirection);
    }

    public Chunk getChunk(int x, int y) {
        Map<Integer, Chunk> chunksY = chunks.get(x);
        if (chunksY == null) return null;
        return chunksY.get(y);
    }

    public Entity[] getEntities() {
        return entities.values().toArray(new Entity[0]);
    }
}
