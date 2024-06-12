package com.javaworld.client;

import com.javaworld.core.World;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.update.BlockUpdate;
import com.javaworld.core.update.EntityUpdate;
import com.javaworld.data.*;
import com.javaworld.util.TCPDataReader;
import com.javaworld.util.TCPDataWriter;
import com.wavjaby.serializer.Serializable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientGameManager {
    private static final Logger logger = Logger.getLogger(ClientGameManager.class.getSimpleName());
    private final String host;
    private final int port;
    private final Thread reciverThread;
    private final World world;
    private final ClientGameEvent event;

    private boolean closed = false;
    private Socket socket;
    private TCPDataReader in;
    private TCPDataWriter out;

    private final Object codeCompileResultLock = new Object();
    private volatile ServerResponseData codeCompileResult;

    public ClientGameManager(String serverIp, int port, ClientGameEvent event) {
        this.host = serverIp;
        this.port = port;
        this.event = event;
        reciverThread = new Thread(this::receiver, "ServerConnection");
        world = new World(System.currentTimeMillis());
    }

    private void calculateEntityUpdate(WorldEntityUpdateData entityUpdate) {
        for (EntityUpdate update : entityUpdate.toEntityUpdates()) {
            switch (update.type) {
                case CREATE -> {
                    Entity entity = update.toEntity();
                    world.createEntity(update.entitySerial, entity);
                    event.entityCreate(entity);
                }
                case REMOVE -> {
                    Entity entity = world.getEntity(update.entitySerial);
                    event.entityRemove(entity);
                    world.removeEntity(entity);
                }
                case UPDATE -> {
                    Entity entity = world.getEntity(update.entitySerial);
                    world.updateEntity(entity, update);
                    event.entityUpdate(entity);
                }
            }
        }
    }

    private void calculateBlockUpdate(WorldBlockUpdateData blockUpdate) {
        for (BlockUpdate update : blockUpdate.toBlockUpdates()) {
            BlockData blockData = BlockData.getBlockData(update.namespaceId, update.blockId);
            BlockState blockState = null;
            switch (update.type) {
                case CREATE -> {
                    world.setBlock(update.blockX, update.blockY, update.blockZ, blockData, blockState);
                    event.blockCreate(update.blockX, update.blockY, update.blockZ, blockData, blockState);
                }
                case REMOVE -> {
                    world.setBlock(update.blockX, update.blockY, update.blockZ, null, null);
                    event.blockRemove(update.blockX, update.blockY, update.blockZ, null, null);
                }
                case UPDATE -> {
                    world.setBlock(update.blockX, update.blockY, update.blockZ, blockData, null);
                    event.blockUpdate(update.blockX, update.blockY, update.blockZ, blockData, blockState);
                }
            }
        }
    }

    private void calculateChunkInit(WorldChunkInitData worldChunkInitData) {
        event.chunkInit(worldChunkInitData.toChunkUpdates().get(0));
    }

    private void receiver() {
        while (true) {
            try {
                Serializable obj = in.readObject();
                // Connection close
                if (obj == null) break;
                // Process package
                if (obj instanceof PlayerConsoleData playerConsole) {
                    if (playerConsole.log != null) event.playerLog(playerConsole.log);
                    if (playerConsole.error != null) event.playerError(playerConsole.error);
                } else if (obj instanceof ServerResponseData c) {
                    synchronized (codeCompileResultLock) {
                        this.codeCompileResult = c;
                        codeCompileResultLock.notifyAll();
                    }
                } else if (obj instanceof WorldBlockUpdateData blockUpdate) {
                    calculateBlockUpdate(blockUpdate);
                } else if (obj instanceof WorldEntityUpdateData entityUpdate) {
                    calculateEntityUpdate(entityUpdate);
                } else if (obj instanceof WorldChunkInitData worldChunkInitData) {
                    calculateChunkInit(worldChunkInitData);
                } else if (obj instanceof PlayerScoreUpdateData playerScoreUpdateData) {
                    event.playerScoreUpdate(playerScoreUpdateData.playerNames, playerScoreUpdateData.playerScore);
                } else
                    logger.log(Level.SEVERE, "Unknown package: " + obj.getClass().getName());
            } catch (IOException ignore) {
                break;
            }
        }
        disconnect();
    }

    public ServerResponseData sendPlayerCode(String source) {
        PlayerCodeData codeUpload = new PlayerCodeData(source);
        try {
            out.write(codeUpload);
        } catch (IOException e) {
            return new ServerResponseData(false, e.getMessage());
        }
        synchronized (codeCompileResultLock) {
            try {
                while (codeCompileResult == null)
                    codeCompileResultLock.wait();
            } catch (InterruptedException ignore) {
            }
        }
        return codeCompileResult;
    }

    public ServerResponseData connect(String playerName) {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 1000);
            in = new TCPDataReader(socket.getInputStream());
            out = new TCPDataWriter(socket.getOutputStream());
            logger.info("Connection Successful!");
            Serializable serverResponse;
            // Login
            out.write(new PlayerLoginData(1, playerName));
            serverResponse = in.readObject();
            if (serverResponse instanceof ServerResponseData response) {
                reciverThread.start();
                return response;
            } else
                return new ServerResponseData(false, "Unknown response type: " + serverResponse.getClass().getName());
        } catch (IOException e) {
            return new ServerResponseData(false, e.getMessage());
        }
    }

    public void disconnect() {
        if (closed) return;
        closed = true;
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException ignore) {
        }

        logger.info("Server Disconnected!");
    }

    public void join() {
        try {
            reciverThread.join();
        } catch (InterruptedException ignore) {
        }
    }
}
