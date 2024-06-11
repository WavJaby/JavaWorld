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
    Entity grabEntityTarget = null;
    @Getter
    @Setter
    boolean grabbingEntity = false;

    @Getter
    Block hoeingBlockTarget = null;
    @Getter
    @Setter
    long hoeingBlockTime = -1;

    @Getter
    Block plantTreeTarget = null;
    @Getter
    @Setter
    long plantTreeTime = -1;


    public Self(String name, Vec2 pos) {
        super(name, pos, 0);
    }

    public void stopMoving() {
        setVelocityZero();
        dest = null;
    }

    public void stopGrabbing() {
        setVelocityZero();
        dest = null;
    }

    public void stopHoeingBlock() {
        hoeingBlockTime = -1;
        hoeingBlockTarget = null;
    }

    public void stopPlanting() {
        plantTreeTime = -1;
        plantTreeTarget = null;
    }

    public boolean noAction() {
        return hoeingBlockTime == -1 && plantTreeTime == -1 && !grabbingEntity;
    }

    @Override
    public void moveTo(Vec2 position) {
        dest = position;
    }

    @Override
    public Block getBlock() {
        return ((ServerWorld) world).getTopBlock((int) position.x, (int) position.y);
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
        hoeingBlockTarget = ((ServerWorld) world).getTopBlock((int) position.x, (int) position.y);
    }

    @Override
    public boolean isHoeingBlock() {
        return hoeingBlockTime != -1;
    }

    @Override
    public void plantTree() {
        plantTreeTarget = ((ServerWorld) world).getTopBlock((int) position.x, (int) position.y);
    }

    @Override
    public boolean isPlanting() {
        return plantTreeTime != -1;
    }

    @Override
    public void harvestPlant() {
    }

    @Override
    public boolean isHarvesting() {
        return false;
    }

    @Override
    public void grabEntity(Entity entity) {
        if (grabEntityTarget != null || entity == null) return;
        if (position.distance(entity.getPosition()) > 1) return;
        grabEntityTarget = entity;
    }

    @Override
    public void putEntity() {
        grabEntityTarget = null;
    }

    @Override
    public Entity[] getEntities() {
        List<Entity> entities = new ArrayList<>();
        for (com.javaworld.core.entity.Entity entity : ((ServerWorld) world).getEntities()) {
            double distance = entity.getPosition().distance(position);
            if (distance > 5) continue;
            entities.add(entity);
        }
        entities.sort((a, b) -> Double.compare(a.getPosition().distance(position), b.getPosition().distance(position)));
        return entities.toArray(new Entity[0]);
    }

    @Override
    public Entity getNearestEntity() {
        com.javaworld.core.entity.Entity nearestEntity = null;
        double minDistance = 0;
        for (com.javaworld.core.entity.Entity entity : ((ServerWorld) world).getEntities()) {
            double distance = entity.getPosition().distance(position);
            if (entity == this || distance > 5) continue;
            // Get nearest entity
            if (nearestEntity == null || distance < minDistance)
                nearestEntity = entity;
        }
        return nearestEntity;
    }

    @Override
    public Entity[] getCurrentBlockEntities() {
        int blockX = (int) position.x, blockY = (int) position.y;
        List<Entity> entities = new ArrayList<>();
        for (com.javaworld.core.entity.Entity entity : ((ServerWorld) world).getEntities()) {
            if (entity == this) continue;
            Vec2 pos = entity.getPosition();
            if (pos.x >= blockX && pos.x < blockX + 1 &&
                    pos.y >= blockY && pos.y < blockY + 1)
                entities.add(entity);
        }
        return entities.toArray(new Entity[0]);
    }

    public void reset() {
        stopMoving();
        stopGrabbing();
        stopPlanting();
        stopHoeingBlock();
    }
}
