package com.wavjaby.javaworld.adapter.block;

public interface Chunk {
    int CHUNK_SIZE_SHIFT = 5;
    int CHUNK_SIZE = 1 << CHUNK_SIZE_SHIFT, CHUNK_HEIGHT = 1 << 2;
}
