package com.javaworld.core;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.core.block.Block;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.entity.EntityData;
import com.javaworld.core.jwblocks.Grass;
import com.javaworld.core.jwblocks.Stone;
import com.javaworld.core.jwentities.Self;
import com.javaworld.core.jwentities.Tree;
import com.javaworld.data.PlayerConsoleOutput;
import com.javaworld.data.WorldEntityUpdate;
import com.javaworld.server.ClientHandler;
import com.javaworld.server.CompiledResult;
import com.javaworld.util.CustomThreadFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.javaworld.util.ExceptionToString.exceptionToErrorPrintStream;

public class GameManager {
    private static final Logger logger = Logger.getLogger(GameManager.class.getSimpleName());
    private static final DecimalFormat percentFormat = new DecimalFormat("0.0");
    private static final int TICK = 20;
    private static final int WALK_SPEED = 1;
    private final ScheduledThreadPoolExecutor gameLoop;
    private final Map<String, ClientHandler> clients;
    private final ThreadPoolExecutor executor;
    private final ScheduledExecutorService scheduler;
    private final Semaphore semaphore;

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
    }

    public void startGameLoop() {
        gameLoop.scheduleAtFixedRate(this::gameLoop, 0, 1000 / TICK, TimeUnit.MILLISECONDS);
        lastStateLog = System.currentTimeMillis();
    }

    private int skipped = 0;
    private boolean extraTick = false;
    private long usageMillis = 0;

    private void gameLoop() {
        // Skip extra tick
        if (extraTick) {
            extraTick = false;
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastStateLog <= 1) {
            skipped += 1;
            lastStateLog = now;
            return;
        } else if (skipped > 0) {
            logger.warning("Skip " + skipped + " ticks");
            skipped = 0;
        }
        tickCount++;
        if (now - lastStateLog >= 1000) {
            if (tickCount > TICK) extraTick = true;
            logger.info("ticks: " + tickCount + ", usage: " + percentFormat.format(usageMillis / 1000d * 100) + "%");
            usageMillis = 0;
            tickCount = 0;
            lastStateLog = now;
        }

        // Player game update
        CountDownLatch count = new CountDownLatch(clients.size());
        for (ClientHandler client : clients.values()) {
            if (client.compiled == null) {
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
        Map<Integer, EntityUpdate> movedEntities = new HashMap<>();
        try {
            for (ClientHandler client : clients.values()) {
                if (client.player == null) continue;
                Self player = client.player;

                if (player.getDest() != null && !player.isMoving()) {
                    // Go to new destination
                    player.setCurrentDest(player.getDest());
                    player.setVelocity(player.getDest().sub(player.getPosition()).normalize().mul(WALK_SPEED));
                }
                // Move player
                if (player.isMoving()) {
                    float distanceLeft = player.getCurrentDest().sub(player.getPosition()).length();
                    Vec2 vec = player.getVelocity().mul(1d / TICK);
                    if (distanceLeft < vec.length()) {
                        // Moving finish
                        entitySetPosition(player, player.getCurrentDest(), movedEntities);
                        player.setVelocityZero();
                        player.setCurrentDest(null);
                    } else
                        entitySetPosition(player, player.getPosition().add(vec), movedEntities);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error", e);
        }
        // Create entity update output
        List<EntityUpdate> entityUpdates = new ArrayList<>(serverWorld.getEntityUpdateCount() + movedEntities.size());
        serverWorld.getEntityUpdates(entityUpdates);
        for (EntityUpdate update : entityUpdates)
            movedEntities.remove(update.entitySerial);
        entityUpdates.addAll(movedEntities.values());
        WorldEntityUpdate entityUpdate = entityUpdates.isEmpty() ? null : new WorldEntityUpdate(entityUpdates);

        // Send player console output, game update
        for (ClientHandler client : clients.values()) {
            if (client.compiled == null) continue;
            final CompiledResult compiled = client.compiled;

            try {
                // Get user output
                String out = compiled.out.toString(StandardCharsets.UTF_8);
                compiled.out.reset();

                String err = compiled.err.toString(StandardCharsets.UTF_8);
                compiled.err.reset();

                client.out.write(new PlayerConsoleOutput(out.isBlank() ? null : out, err.isBlank() ? null : err));
                if (entityUpdate != null)
                    client.out.write(entityUpdate);

                // Client use illegal class, disconnect it
                if (client.illegal)
                    client.disconnect();
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
        usageMillis += System.currentTimeMillis() - now;
    }

    private void entitySetPosition(Entity entity, Vec2 position, Map<Integer, EntityUpdate> movedEntities) {
        if (entity.getPosition().equals(position))
            return;
        entity.setPosition(position);
        movedEntities.computeIfAbsent(entity.getSerial(), (k) -> new EntityUpdate(EntityUpdate.UpdateType.UPDATE, entity));
    }

    private void runPlayerCode(ClientHandler client, CountDownLatch count) {
        final CompiledResult compiled = client.compiled;
        try {
            // Player not init
            if (client.player == null) {
                client.player = new Self(client.name);
                playerJoin(client.player);
                client.compiled.playerApplication.init(client.player);
            } else compiled.playerApplication.gameUpdate(client.player);
        } catch (IllegalAccessError e) {
            exceptionToErrorPrintStream(compiled, e);
            client.illegal = true;
        } catch (Throwable e) {
            exceptionToErrorPrintStream(compiled, e);
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
        BlockData grass = BlockData.getBlockData(BlockData.getBlockID(javaWorld, "grass"));
        BlockData stone = BlockData.getBlockData(BlockData.getBlockID(javaWorld, "stone"));

        for (byte x = 0; x < com.javaworld.adapter.block.Chunk.CHUNK_SIZE; x++) {
            for (byte y = 0; y < com.javaworld.adapter.block.Chunk.CHUNK_SIZE; y++) {
                chunk.createBlock(null, null, x, y, (byte) 3);
                chunk.createBlock(grass, new BlockState(), x, y, (byte) 2);
                chunk.createBlock(grass, new BlockState(), x, y, (byte) 1);
                chunk.createBlock(stone, new BlockState(), x, y, (byte) 0);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadResources() {
        // Add default blocks
        Namespace javaWorld = Namespace.addNamespace("javaworld");
        Object[][] blocks = {
                {"air", Grass.class},
                {"grass", Grass.class},
                {"stone", Stone.class},
        };
        Object[][] entities = {
                {"tree", Tree.class},
        };

        for (Object[] block : blocks) {
            BlockData.createBlockData(javaWorld, (String) block[0], (Class<? extends Block>) block[1]);
        }

        for (Object[] entity : entities) {
            EntityData.createEntityData(javaWorld, (String) entity[0], (Class<? extends Entity>) entity[1]);
        }
    }
}
