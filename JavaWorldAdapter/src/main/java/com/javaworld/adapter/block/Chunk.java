package com.javaworld.adapter.block;

public interface Chunk {
    int CHUNK_SIZE_SHIFT = 5;
    int CHUNK_SIZE_MASK = (1 << CHUNK_SIZE_SHIFT) - 1;
    int CHUNK_HEIGHT = 1 << 2;
    int CHUNK_SIZE = 1 << CHUNK_SIZE_SHIFT;
}
