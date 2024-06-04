package com.javaworld.core;

import com.javaworld.core.block.Block;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.update.EntityUpdate;
import lombok.Getter;

import java.util.*;

import static com.javaworld.adapter.block.Chunk.CHUNK_SIZE_MASK;
import static com.javaworld.adapter.block.Chunk.CHUNK_SIZE_SHIFT;

public class World implements com.javaworld.adapter.World {
    final Map<Integer, Map<Integer, Chunk>> chunks = new HashMap<>();
    final Map<Integer, Entity> entities = new HashMap<>();
    @Getter
    long worldTime;
    int entitySerial = 0;

    public World(long worldTime) {
        this.worldTime = worldTime;
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

    public Chunk getChunk(int x, int y) {
        Map<Integer, Chunk> chunksY = chunks.get(x);
        if (chunksY == null) return null;
        return chunksY.get(y);
    }

    public List<Chunk> getChunks() {
        List<Chunk> chunksList = new ArrayList<>();
        for (Map<Integer, Chunk> value : chunks.values()) {
            chunksList.addAll(value.values());
        }

        return chunksList;
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

    public Collection<Entity> getEntities() {
        return entities.values();
    }

    public void setBlock(int x, int y, int z, BlockData blockData, BlockState blockState) {
        Chunk chunk = loadChunk(x >> CHUNK_SIZE_SHIFT, y >> CHUNK_SIZE_SHIFT);
        chunk.setBlock(blockData, blockState, (byte) (x & CHUNK_SIZE_MASK), (byte) (y & CHUNK_SIZE_MASK), (byte) z);
    }

    public Block getTopBlock(int x, int y) {
        Map<Integer, Chunk> chunksY = chunks.get(x >> CHUNK_SIZE_SHIFT);
        if (chunksY == null) return null;
        Chunk chunk = chunksY.get(y >> CHUNK_SIZE_SHIFT);
        if (chunk == null) return null;
        return chunk.getTopBlock((byte) (x & CHUNK_SIZE_MASK), (byte) (y & CHUNK_SIZE_MASK));
    }
}
