package com.javaworld.data;

import com.javaworld.core.Chunk;
import com.javaworld.core.block.Block;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.update.ChunkUpdate;
import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.javaworld.adapter.block.Chunk.CHUNK_HEIGHT;
import static com.javaworld.adapter.block.Chunk.CHUNK_SIZE;

@Serializable
@AllArgsConstructor
public class WorldChunkInitData extends WorldChunkInitSerializer {
    public static final int chunkDataLength = 2 + Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * Chunk.CHUNK_HEIGHT * 2;
    public final int[] chunkData;

    public WorldChunkInitData(List<Chunk> chunks) {
        chunkData = new int[chunkDataLength * chunks.size()];

        int dataOffset = 0;
        for (Chunk chunk : chunks) {
            chunkData[dataOffset] = chunk.x;
            chunkData[dataOffset + 1] = chunk.y;
            // chunk blocks
            Block[][] blocks = chunk.getBlocks();
            for (int z = 0; z < blocks.length; z++) {
                int offset = dataOffset + 2 + (z << (Chunk.CHUNK_SIZE_SHIFT + Chunk.CHUNK_SIZE_SHIFT + 1));
                for (int j = 0; j < blocks[z].length; j++) {
                    if (blocks[z][j] == null) {
                        chunkData[offset + j * 2] = 0;
                        chunkData[offset + j * 2 + 1] = 0;
                    } else {
                        chunkData[offset + j * 2] = blocks[z][j].blockData.namespace.id;
                        chunkData[offset + j * 2 + 1] = blocks[z][j].blockData.id;
                    }
                }
            }

            dataOffset += chunkDataLength;
        }
    }

    public List<ChunkUpdate> toChunkUpdates() {
        int count = chunkData.length / chunkDataLength;
        List<ChunkUpdate> chunkUpdates = new ArrayList<>();
        int dataOffset = 0;
        for (int i = 0; i < count; i++) {
            int chunkX = chunkData[dataOffset];
            int chunkY = chunkData[dataOffset + 1];

            BlockData[][] blocks = new BlockData[CHUNK_HEIGHT][CHUNK_SIZE * CHUNK_SIZE];
            for (int z = 0; z < blocks.length; z++) {
                int offset = dataOffset + 2 + (z << (Chunk.CHUNK_SIZE_SHIFT + Chunk.CHUNK_SIZE_SHIFT + 1));
                for (int j = 0; j < blocks[z].length; j++) {
                    blocks[z][j] = BlockData.getBlockData(chunkData[offset + j * 2], chunkData[offset + j * 2 + 1]);
                }
            }

            chunkUpdates.add(new ChunkUpdate(
                    chunkX,
                    chunkY,
                    blocks
            ));
            dataOffset += chunkDataLength;
        }

        return chunkUpdates;
    }
}
