package com.javaworld.core.block;

import com.javaworld.core.Chunk;
import com.javaworld.core.Namespace;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockData extends BlockID {
    private static final Logger logger = Logger.getLogger(BlockData.class.getSimpleName());

    // Block namespaceId->blockId
    private static final List<List<BlockData>> blocks = new ArrayList<>();
    // Block namespaceId->blockName
    private static final List<Map<String, BlockData>> blockNames = new ArrayList<>();

    private final Constructor<? extends Block> blockHandler;

    public BlockData(final Namespace namespace, final String name, final int id, final Class<? extends Block> blockHandler) {
        super(namespace, name, id);
        // Air block
        if (id == 0) {
            this.blockHandler = null;
            return;
        }
        // Unknown block
        if (blockHandler == null) {
            this.blockHandler = null;
            logger.log(Level.SEVERE, "Could not find constructor for block: " + this);
            return;
        }

        Constructor<? extends Block> blockHandlerConstructor = null;
        try {
            blockHandlerConstructor = blockHandler.getDeclaredConstructor(
                    BlockData.class, BlockState.class, Chunk.class, byte.class, byte.class, byte.class);
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Could not find constructor for block: " + this + " -> " + blockHandler.getName(), e);
        }
        this.blockHandler = blockHandlerConstructor;
    }

    public static synchronized void createNamespaceBlockList() {
        blocks.add(new ArrayList<>());
        blockNames.add(new HashMap<>());
    }

    public static synchronized BlockData createBlockData(final Namespace namespace, final String blockName,
                                                         final Class<? extends Block> blockHandler) {
        List<BlockData> namespaceBlocks = blocks.get(namespace.id);
        BlockData blockData = new BlockData(namespace, blockName, namespaceBlocks.size(), blockHandler);
        namespaceBlocks.add(blockData);
        blockNames.get(namespace.id).put(blockName, blockData);
        return blockData;
    }

    public static BlockData getBlockData(final Namespace namespace, final String blockName) {
        return blockNames.get(namespace.id).get(blockName);
    }

    public static BlockData getBlockData(final int namespaceId, final String blockName) {
        return blockNames.get(namespaceId).get(blockName);
    }

    public static Block createBlock(BlockData blockData, BlockState state, Chunk chunk, byte x, byte y, byte z) {
        if (blockData.blockHandler == null)
            return null;
        try {
            return blockData.blockHandler.newInstance(blockData, state, chunk, x, y, z);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public static BlockID[] getBlockIDs() {
        List<BlockID> out = new ArrayList<>(blockNames.size());
        for (Map<String, BlockData> blockID : blockNames) {
            out.addAll(blockID.values());
        }
        return out.toArray(new BlockID[0]);
    }
}
