package com.javaworld.core.jwentities;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.block.Block;
import com.javaworld.adapter.block.BlockID;
import com.javaworld.adapter.entity.Entity;
import com.javaworld.core.Chunk;
import com.javaworld.core.ServerWorld;
import com.javaworld.core.block.BlockData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Self extends Player implements com.javaworld.adapter.Self {
    @Getter
    Vec2 dest = null;
    @Getter
    Entity grabbedEntity = null;
    @Getter
    Block hoeingBlock = null;
    @Getter
    @Setter
    long hoeingBlockTime = -1;

    public Self(String name) {
        super(name, new Vec2(0, 0), 0);
    }

    public void stopHoeingBlock() {
        hoeingBlockTime = -1;
        hoeingBlock = null;
    }

    @Override
    public void moveTo(Vec2 position) {
        dest = position;
    }

    @Override
    public Block[][] getBlocks() {
        Chunk chunk = ((ServerWorld) world).getChunk((int) position.x >> Chunk.CHUNK_SIZE_SHIFT, (int) position.y >> Chunk.CHUNK_SIZE_SHIFT);
        if (chunk == null)
            return new Block[0][0];

        return chunk.getBlocksCopy();
    }

    @Override
    public Block[] getBlocks(int z) {
        Chunk chunk = ((ServerWorld) world).getChunk((int) position.x >> Chunk.CHUNK_SIZE_SHIFT, (int) position.y >> Chunk.CHUNK_SIZE_SHIFT);
        if (chunk == null)
            return new Block[0];
        return chunk.getBlocksCopy(z);
    }

    @Override
    public BlockID[] getBlockIDs() {
        return BlockData.getBlockIDs();
    }

    @Override
    public void hoeBlock() {
        hoeingBlock = ((ServerWorld) world).getTopBlock((int) position.x, (int) position.y);
    }

    @Override
    public boolean isHoeingBlock() {
        return hoeingBlockTime != -1;
    }

    @Override
    public void grabEntity(Entity entity) {
        if (grabbedEntity != null || entity == null) return;
        if (position.distance(entity.getPosition()) > 1) return;
        grabbedEntity = entity;
    }

    @Override
    public void putEntity() {
        grabbedEntity = null;
    }

    @Override
    public Entity[] getEntities() {
        List<Entity> entities = new ArrayList<>();
        for (com.javaworld.core.entity.Entity entity : ((ServerWorld) world).getEntities()) {
            double distance = entity.getPosition().distance(position);
            if (distance > 5) continue;
            entities.add(entity);
        }
        return entities.toArray(new Entity[0]);
    }

    @Override
    public Entity getNearestEntity() {
        com.javaworld.core.entity.Entity nearestEntity = null;
        double minDistance = 0;
        for (com.javaworld.core.entity.Entity entity : ((ServerWorld) world).getEntities()) {
            double distance = entity.getPosition().distance(position);
            if (distance > 5) continue;
            // Get nearest entity
            if (nearestEntity == null || distance < minDistance)
                nearestEntity = entity;
        }
        return nearestEntity;
    }
}
