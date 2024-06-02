package com.javaworld.core.block;

import com.javaworld.adapter.block.BlockID;
import com.javaworld.core.Chunk;

public abstract class Block implements com.javaworld.adapter.block.Block {
    public final BlockData blockData;
    public final BlockState blockState;
    public final Chunk chunk;
    public final byte x, y, z;

    public Block(final BlockData blockData, final BlockState blockState, final Chunk chunk,
                 final byte x, final byte y, final byte z) {
        this.blockData = blockData;
        this.blockState = blockState;
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public abstract void tickUpdate();

    public abstract void render();

    @Override
    public int getBlockX() {
        return x;
    }

    @Override
    public int getBlockY() {
        return y;
    }

    @Override
    public int getBlockZ() {
        return z;
    }

    @Override
    public BlockID getBlockID() {
        return blockData;
    }

    @Override
    public String toString() {
        return blockData.toString();
    }
}
