package com.javaworld.core;

import com.javaworld.core.block.Block;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;
import lombok.Getter;

public class Chunk implements com.javaworld.adapter.block.Chunk {
    @Getter
    private final Block[][] blocks = new Block[CHUNK_HEIGHT][CHUNK_SIZE * CHUNK_SIZE];
    private Chunk left, right, up, down;

    public final int x, y;

    public Chunk(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public void setBlock(BlockData blockData, BlockState blockState, byte x, byte y, byte z) {
        // Air block
        if (blockData == null) {
            blocks[z][(y << CHUNK_SIZE_SHIFT) + x] = null;
            return;
        }

        Block block = BlockData.createBlock(blockData, blockState, this, x, y, z);
        if (block == null) return;
        blocks[z][(y << CHUNK_SIZE_SHIFT) + x] = block;
    }

    public Block[][] getBlocksCopy() {
        Block[][] blocks = new Block[CHUNK_HEIGHT][CHUNK_SIZE * CHUNK_SIZE];
        for (int i = 0; i < CHUNK_HEIGHT; i++) {
            System.arraycopy(this.blocks[i], 0, blocks[i], 0, CHUNK_SIZE * CHUNK_SIZE);
        }

        return blocks;
    }

    public Block[] getBlocksCopy(int z) {
        Block[] blocks = new Block[CHUNK_SIZE * CHUNK_SIZE];
        System.arraycopy(this.blocks[z], 0, blocks, 0, CHUNK_SIZE * CHUNK_SIZE);
        return blocks;
    }

    public Block getBlock(byte x, byte y, byte z) {
        return blocks[z][(y << CHUNK_SIZE_SHIFT) + x];
    }

    public Block getTopBlock(byte x, byte y) {
        int xyOff = (y << CHUNK_SIZE_SHIFT) + x;
        for (int z = CHUNK_HEIGHT - 1; z > -1; z--) {
            if (blocks[z][xyOff] != null)
                return blocks[z][xyOff];
        }
        return null;
    }
}
