package com.wavjaby.javaworld.core.jwblocks;

import com.wavjaby.javaworld.core.Chunk;
import com.wavjaby.javaworld.core.block.Block;
import com.wavjaby.javaworld.core.block.BlockData;
import com.wavjaby.javaworld.core.block.BlockState;

public class Stone extends Block {
    public Stone(BlockData blockData, BlockState blockState, Chunk chunk, byte x, byte y, byte z) {
        super(blockData, blockState, chunk, x, y, z);
    }

    @Override
    public void tickUpdate() {

    }

    @Override
    public void render() {

    }
}
