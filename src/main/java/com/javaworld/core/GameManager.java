package com.javaworld.core;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.PlayerApplication;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.core.block.Block;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.entity.EntityData;
import com.javaworld.core.jwblocks.GrassBlock;
import com.javaworld.core.jwblocks.Stone;
import com.javaworld.core.jwentities.Player;
import com.javaworld.core.jwentities.Self;
import com.javaworld.core.jwentities.Tree;
import com.javaworld.core.update.BlockUpdate;
import com.javaworld.core.update.EntityUpdate;
import com.javaworld.core.update.UpdateType;
import com.javaworld.data.PlayerConsoleOutput;
import com.javaworld.data.WorldBlockUpdate;
import com.javaworld.data.WorldChunkInit;
import com.javaworld.data.WorldEntityUpdate;
import com.javaworld.server.ClientHandler;
import com.javaworld.util.CustomThreadFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.javaworld.util.ExceptionToString.exceptionToErrorPrintStream;

public class GameManager {
    private static final Logger logger = Logger.getLogger(GameManager.class.getSimpleName());
    private static final DecimalFormat percentFormat = new DecimalFormat("0.0");
    private static final int TICK = 20;
    private static final int WALK_SPEED = 3;
    private final ScheduledThreadPoolExecutor gameLoop;
    private final Map<String, ClientHandler> clients;
    private final ThreadPoolExecutor executor;
    private final ScheduledExecutorService scheduler;
    private final Semaphore semaphore;

    private final BlockData dirt, grass_block;
    private final EntityData tree;

    private int tickCount = 0;
    private long lastStateLog;
    private final ServerWorld serverWorld;

    public GameManager(Map<String, ClientHandler> clients) {
        this.clients = clients;
        serverWorld = new ServerWorld();
        gameLoop = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new CustomThreadFactory("GameLoop"));
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4, new CustomThreadFactory("CodeExecute"));
        scheduler = Executors.newScheduledThreadPool(executor.getMaximumPoolSize(), new CustomThreadFactory("CodeTimeout"));
        semaphore = new Semaphore(executor.getMaximumPoolSize());

        dirt = BlockData.getBlockData(0, "dirt");
        grass_block = BlockData.getBlockData(0, "grass_block");
        Player.entityData = EntityData.getEntityData(0, "player");
        tree = EntityData.getEntityData(0, "tree");
    }

    public void startGameLoop() {
        lastStateLog = System.currentTimeMillis();
        gameLoop.scheduleAtFixedRate(this::gameLoop, 0, 1000 / TICK, TimeUnit.MILLISECONDS);
    }

    private int skipped = 0;
    private long usageMillis = 0;

    private void gameLoop() {
        long start = System.currentTimeMillis();
        if (start - lastStateLog <= 1) {
            skipped += 1;
            lastStateLog = start;
            return;
        } else if (skipped > 0) {
            logger.warning("Skip " + skipped + " ticks");
            skipped = 0;
        }
        tickCount++;
        if (start - lastStateLog >= 1000) {
            logger.info("ticks: " + tickCount + ", usage: " + percentFormat.format(usageMillis / 1000d * 100) + "%");
            usageMillis = 0;
            tickCount = 0;
            lastStateLog = start;
        }

        // Player game update
        CountDownLatch count = new CountDownLatch(clients.size());
        for (ClientHandler client : clients.values()) {
            if (client.compiled == null || client.player == null) {
                count.countDown();
                continue;
            }

            try {
                semaphore.acquire();
            } catch (InterruptedException ignore) {
                return;
            }
            Future<?> future = executor.submit(() -> runPlayerCode(client, count));
            // Execution time limit
            scheduler.schedule(() -> {
                if (future.isDone()) return;
                future.cancel(true);
                semaphore.release();
                count.countDown();
            }, 5, TimeUnit.MILLISECONDS);
        }
        try {
            count.await();
        } catch (InterruptedException e) {
            return;
        }

        // Game update
        byte[] entityUpdate = null;
        byte[] blockUpdate = null;
        Set<Integer> entityRemoved = new HashSet<>();
        try {
            Map<Integer, EntityUpdate> entitiesUpdate = new HashMap<>();
            List<BlockUpdate> blocksUpdate = new ArrayList<>();
            for (ClientHandler client : clients.values()) {
                if (client.player == null) continue;
                Self player = client.player;

                // Player move command
                if (player.getDest() != null && !player.isMoving()) {
                    Vec2 dest = player.getDest();
                    dest.x = Math.max(0.5f, Math.min(dest.x, 31.5f));
                    dest.y = Math.max(0.5f, Math.min(dest.y, 31.5f));
                    // Set new destination
                    player.setCurrentDest(dest);
                    player.setVelocity(dest.sub(player.getPosition()).normalize().mul(WALK_SPEED));
                }
                if (player.noAction()) {
                    if (player.getHoeingBlockTarget() != null && ((Block) player.getHoeingBlockTarget()).blockData == grass_block) {
                        // Player hoeing block command
                        player.setHoeingBlockTime(serverWorld.getWorldTime() + TICK);
                    } else if (player.getPlantTreeTarget() != null && ((Block) player.getPlantTreeTarget()).blockData == dirt) {
                        // Player plant tree command
                        player.setPlantTreeTime(serverWorld.getWorldTime() + TICK * 2);
                    }
                }

                // Move player
                if (player.isMoving()) {
                    float distanceLeft = player.getCurrentDest().sub(player.getPosition()).length();
                    Vec2 vec = player.getVelocity().mul(1d / TICK);
                    if (distanceLeft < vec.length()) {
                        // Moving finish
                        entitySetPosition(player, player.getCurrentDest(), entitiesUpdate);
                        player.setVelocityZero();
                        player.setCurrentDest(null);
                    } else
                        entitySetPosition(player, player.getPosition().add(vec), entitiesUpdate);
                }
                // Check hoeing state
                if (player.isHoeingBlock()) {
                    if (player.getHoeingBlockTarget().getEntityPosition().distance(player.getPosition()) > 1) {
                        // Player leave current block, cancel
                        player.stopHoeingBlock();
                    } else if (serverWorld.getWorldTime() >= player.getHoeingBlockTime()) {
                        // Hoeing done
                        setBlock((Block) player.getHoeingBlockTarget(), dirt, blocksUpdate);
                        player.stopHoeingBlock();
                    }
                }
                // Check plant state
                if (player.isPlanting()) {
                    if (player.getPlantTreeTarget().getEntityPosition().distance(player.getPosition()) > 1) {
                        // Player leave current block, cancel
                        player.stopPlanting();
                    } else if (serverWorld.getWorldTime() >= player.getPlantTreeTime()) {
                        // Plant done
                        Vec2 target = player.getPlantTreeTarget().getEntityPosition();
                        Entity entity = EntityData.createEntity(tree, target, 0);
                        assert entity != null;
                        serverWorld.createEntity(entity);
                        player.stopPlanting();
                    }
                }
            }

            // Process player join
            for (ClientHandler client : clients.values()) {
                if (client.player != null) continue;
                // Player init
                Random random = new Random();
//                client.player = new Self(client.name, new Vec2(random.nextInt(16) + 0.5f, random.nextInt(16) + 0.5f));
                client.player = new Self(client.name, new Vec2(0.5f, 0.5f));
                playerJoin(client.player);
            }

            // Create entity update output
            List<EntityUpdate> entityUpdates = new ArrayList<>(serverWorld.getEntityUpdateCount() + entitiesUpdate.size());
            serverWorld.getEntityUpdates(entityUpdates);
            entityUpdates.addAll(entitiesUpdate.values());
            // Get entity removed in this tick
            for (EntityUpdate update : entityUpdates)
                if (update.type == UpdateType.REMOVE) entityRemoved.add(update.entitySerial);
            entityUpdate = entityUpdates.isEmpty() ? null : new WorldEntityUpdate(entityUpdates).serialize();
            blockUpdate = blocksUpdate.isEmpty() ? null : new WorldBlockUpdate(blocksUpdate).serialize();

            serverWorld.addWorldTime();

            // Send player console output, game update
            for (ClientHandler client : clients.values()) {
                try {
                    // Init player
                    if (!client.playerInit) {
                        client.playerInit = true;

                        // Init player entity
                        if (!serverWorld.entities.isEmpty()) {
                            List<EntityUpdate> initEntityUpdates = new ArrayList<>(serverWorld.entities.size());
                            for (Entity entity : serverWorld.entities.values()) {
                                if (entityRemoved.contains(entity.getSerial())) continue;
                                initEntityUpdates.add(new EntityUpdate(UpdateType.CREATE, entity));
                            }
                            client.out.write(new WorldEntityUpdate(initEntityUpdates));
                        }
                        client.out.write(new WorldChunkInit(serverWorld.getChunks()));
                    } else {
                        // Send entity update
                        if (entityUpdate != null)
                            client.out.write(entityUpdate);
                        // Send block update
                        if (blockUpdate != null)
                            client.out.write(blockUpdate);
                    }

                    // Send player console out
                    if (client.playerOut != null && client.playerErr != null) {
                        String out = client.playerOut.toString(StandardCharsets.UTF_8);
                        client.playerOut.reset();
                        String err = client.playerErr.toString(StandardCharsets.UTF_8);
                        client.playerErr.reset();
                        client.out.write(new PlayerConsoleOutput(out.isBlank() ? null : out, err.isBlank() ? null : err));
                    }
                } catch (IOException e) {
//                logger.warning(e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error", e);
        }
        usageMillis += System.currentTimeMillis() - start;
    }

    private void setBlock(Block hoeingBlock, BlockData newBlockData, List<BlockUpdate> blocksUpdate) {
        serverWorld.setBlock(hoeingBlock.x, hoeingBlock.y, hoeingBlock.z, newBlockData, null);
        blocksUpdate.add(new BlockUpdate(UpdateType.UPDATE, hoeingBlock, newBlockData));
    }

    private void entitySetPosition(Entity entity, Vec2 position, Map<Integer, EntityUpdate> movedEntities) {
        if (entity.getPosition().equals(position))
            return;
        entity.setPosition(position);
        movedEntities.computeIfAbsent(entity.getSerial(), (k) -> new EntityUpdate(UpdateType.UPDATE, entity));
    }

    private void runPlayerCode(ClientHandler client, CountDownLatch count) {
        final PlayerApplication playerApplication = client.compiled.playerApplication;
        try {
            if (!client.playerApplicationInit) {
                client.playerApplicationInit = true;
                playerApplication.init(client.player);
            } else playerApplication.gameUpdate(client.player);
        } catch (IllegalAccessError e) {
            exceptionToErrorPrintStream(client.compiled, e);
            client.closeCompiled();
        } catch (Throwable e) {
            exceptionToErrorPrintStream(client.compiled, e);
        } finally {
            semaphore.release();
            count.countDown();
        }
    }

    public void playerJoin(Self player) {
        serverWorld.createEntity(player);
    }

    public void playerLeave(Self player) {
        ((ServerWorld) player.getWorld()).removeEntity(player);
    }

    public void createWorld() {
        Chunk chunk = serverWorld.loadChunk(0, 0);
        Namespace javaWorld = Namespace.getNamespace("javaworld");
        assert javaWorld != null;
        BlockData grassBlock = BlockData.getBlockData(javaWorld, "grass_block");
        BlockData dirt = BlockData.getBlockData(javaWorld, "dirt");
        BlockData stone = BlockData.getBlockData(javaWorld, "stone");

        for (byte x = 0; x < com.javaworld.adapter.block.Chunk.CHUNK_SIZE; x++) {
            for (byte y = 0; y < com.javaworld.adapter.block.Chunk.CHUNK_SIZE; y++) {
                chunk.setBlock(null, null, x, y, (byte) 3);
                chunk.setBlock(grassBlock, new BlockState(), x, y, (byte) 2);
                chunk.setBlock(dirt, new BlockState(), x, y, (byte) 1);
                chunk.setBlock(stone, new BlockState(), x, y, (byte) 0);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadResources() {
        // Add default blocks
        Namespace javaWorld = Namespace.addNamespace("javaworld");
        Object[][] blocks = {
                {"air", null},
                {"grass_block", GrassBlock.class},
                {"dirt", GrassBlock.class},
                {"stone", Stone.class},
        };
        Object[][] entities = {
                {"player", EntityType.PLAYER, null},
                {"tree", EntityType.PLANT, Tree.class},
        };

        for (Object[] block : blocks) {
            BlockData.createBlockData(javaWorld, (String) block[0], (Class<? extends Block>) block[1]);
        }

        for (Object[] entity : entities) {
            EntityData.createEntityData(javaWorld, (String) entity[0], (EntityType) entity[1], (Class<? extends Entity>) entity[2]);
        }
    }
}
