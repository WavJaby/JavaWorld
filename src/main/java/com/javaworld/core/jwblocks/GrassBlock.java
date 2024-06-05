package com.javaworld.core.jwblocks;

import com.javaworld.core.Chunk;
import com.javaworld.core.block.Block;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;

public class GrassBlock extends Block {

    public GrassBlock(BlockData blockData, BlockState blockState, Chunk chunk, byte x, byte y, byte z) {
        super(blockData, blockState, chunk, x, y, z);
    }

    @Override
    public void tickUpdate() {

    }
}
