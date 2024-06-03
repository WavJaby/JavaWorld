package com.javaworld.adapter;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.block.Block;
import com.javaworld.adapter.block.BlockID;
import com.javaworld.adapter.entity.Entity;
import com.javaworld.adapter.entity.Player;

public interface Self extends Player {
    /**
     * Move to new position with speed 1m/s
     */
    void moveTo(Vec2 position);

    /**
     * Get all blocks in chunk, get null if block underground
     */
    Block[][] getBlocks();

    /**
     * Get chunk block at specific chunk layer, get null if block underground
     */
    Block[] getBlocks(int z);

    /**
     * Get all block id in game
     */
    BlockID[] getBlockIDs();

    /**
     * Hoe a block for planting, takes 1s
     */
    void hoeBlock();

    /**
     * Check hoeing state
     */
    boolean isHoeingBlock();

    /**
     * Find all entities in range of 5m
     */
    Entity[] getEntities();

    /**
     * Find nearest entities in range of 5m
     */
    Entity getNearestEntity();

    /**
     * Grab entity, range limit with 1m
     */
    void grabEntity(Entity entity);

    /**
     * Put down grabbed entity at current location
     */
    void putEntity();
}
