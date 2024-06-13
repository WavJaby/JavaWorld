package com.javaworld.adapter;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.block.Block;
import com.javaworld.adapter.block.BlockID;
import com.javaworld.adapter.entity.Entity;
import com.javaworld.adapter.entity.Player;

public interface Self extends Player {
    /**
     * Move to new position with speed 3m/s
     *
     * @param position Target position
     */
    void moveTo(Vec2 position);

    /**
     * Get block at current location
     *
     * @return block
     */
    Block getBlock();

    /**
     * Get all blocks in chunk, get null if block underground
     *
     * @return blocks
     */
    Block[][] getBlocks();

    /**
     * Get chunk block at specific chunk layer, get null if block underground
     *
     * @param z Block z location in chunk
     * @return blocks
     */
    Block[] getBlocks(int z);

    /**
     * Get all block id in game
     *
     * @return blocks
     */
    BlockID[] getBlockIDs();

    /**
     * Hoe a block for planting, takes 1s
     */
    void hoeBlock();

    /**
     * Check hoeing state
     *
     * @return True if player is hoeing grass block
     */
    boolean isHoeingBlock();

    /**
     * Plant a tree at current location, takes 2 s
     */
    void plantTree();

    /**
     * Check planting state
     *
     * @return True if player is planting
     */
    boolean isPlanting();

    /**
     * Harvest plant at current location, takes 2 s
     */
    void harvestPlant();

    /**
     * Check planting state
     *
     * @return True if player is harvesting
     */
    boolean isHarvesting();

    /**
     * Find entity on current block
     *
     * @return entities
     */
    Entity[] getCurrentBlockEntities();

    /**
     * Find nearest entity in range of 5m
     *
     * @return entity
     */
    Entity getNearestEntity();

    /**
     * Find all entities in range of 5m
     *
     * @return entities
     */
    Entity[] getEntities();

    /**
     * Grab entity, range limit with 1m
     *
     * @param entity Entity to grab
     */
    void grabEntity(Entity entity);

    /**
     * Put down grabbed entity at current location
     */
    void putEntity();
}
