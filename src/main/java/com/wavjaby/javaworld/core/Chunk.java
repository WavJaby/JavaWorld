package com.wavjaby.javaworld.core;

import com.wavjaby.javaworld.core.block.Block;
import com.wavjaby.javaworld.core.block.BlockData;
import com.wavjaby.javaworld.core.block.BlockState;

import static com.wavjaby.javaworld.adapter.block.Chunk.*;

public class Chunk {
    private final Block[][] blocks = new Block[CHUNK_HEIGHT][CHUNK_SIZE * CHUNK_SIZE];
    private Chunk left, right, up, down;

    private final int x, y;

    public Chunk(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Block createBlock(BlockData blockData, BlockState blockState, byte x, byte y, byte z) {
        // Air block
        if (blockData == null)
            return blocks[z][(y << CHUNK_SIZE_SHIFT) + x] = null;

        Block block = BlockData.createBlock(blockData, blockState, this, x, y, z);
        if (block == null) return null;
        return blocks[z][(y << CHUNK_SIZE_SHIFT) + x] = block;
    }

    public Block[][] getBlocks() {
        Block[][] blocks = new Block[CHUNK_HEIGHT][CHUNK_SIZE * CHUNK_SIZE];
        for (int i = 0; i < CHUNK_HEIGHT; i++) {
            System.arraycopy(this.blocks[i], 0, blocks[i], 0, CHUNK_SIZE * CHUNK_SIZE);
        }

        return blocks;
    }

    public Block[] getBlocks(int z) {
        Block[] blocks = new Block[CHUNK_SIZE * CHUNK_SIZE];
        System.arraycopy(this.blocks[z], 0, blocks, 0, CHUNK_SIZE * CHUNK_SIZE);
        return blocks;
    }
}
