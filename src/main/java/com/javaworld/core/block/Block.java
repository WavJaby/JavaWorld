package com.javaworld.core.block;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.block.BlockID;
import com.javaworld.core.Chunk;

import static com.javaworld.adapter.block.Chunk.CHUNK_SIZE_SHIFT;

public abstract class Block implements com.javaworld.adapter.block.Block {
    public final BlockData blockData;
    public final BlockState blockState;
    public final Chunk chunk;
    public final int x, y, z;

    public Block(final BlockData blockData, final BlockState blockState, final Chunk chunk,
                 final byte x, final byte y, final byte z) {
        this.blockData = blockData;
        this.blockState = blockState;
        this.chunk = chunk;
        this.x = x + (chunk.x << CHUNK_SIZE_SHIFT);
        this.y = y + (chunk.y << CHUNK_SIZE_SHIFT);
        this.z = z;
    }

    public abstract void tickUpdate();

    @Override
    public Vec2 getEntityPosition() {
        return new Vec2(x + 0.5f, y + 0.5f);
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
