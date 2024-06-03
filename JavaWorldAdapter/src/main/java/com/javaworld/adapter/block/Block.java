package com.javaworld.adapter.block;

import com.almasb.fxgl.core.math.Vec2;

public interface Block {
    Vec2 getEntityPosition();

    BlockID getBlockID();
}
