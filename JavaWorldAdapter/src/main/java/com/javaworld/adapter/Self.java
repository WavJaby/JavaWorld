package com.javaworld.adapter;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.block.Block;
import com.javaworld.adapter.block.BlockID;
import com.javaworld.adapter.entity.Entity;
import com.javaworld.adapter.entity.Player;

public interface Self extends Player {
    void moveTo(Vec2 position);

    Entity[] getEntities();

    Block[][] getBlocks();

    Block[] getBlocks(int z);

    BlockID[] getBlockIDs();
}
