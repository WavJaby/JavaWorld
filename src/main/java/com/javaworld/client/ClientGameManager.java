package com.javaworld.client;

import com.javaworld.adapter.entity.EntityType;
import com.javaworld.core.EntityUpdate;
import com.javaworld.core.World;
import com.javaworld.core.jwentities.Player;
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
    private volatile ServerResponse codeCompileResult;

    public ClientGameManager(String serverIp, int port, ClientGameEvent event) {
        this.host = serverIp;
        this.port = port;
        this.event = event;
        reciverThread = new Thread(this::receiver, "ServerConnection");
        world = new World(System.currentTimeMillis());
    }

    private void calculateEntityUpdate(WorldEntityUpdate entityUpdate) {
        for (EntityUpdate update : entityUpdate.toEntityUpdates()) {
            if (update.entityType == EntityType.PLAYER) {
                switch (update.type) {
                    case CREATE -> {
                        Player player = new Player(update.playerName, update.entityPosition, update.entityDirection);
                        world.createEntity(update.entitySerial, player);
                        event.entityCreate(player);
                    }
                    case REMOVE -> {
                        Player player = (Player) world.getEntity(update.entitySerial);
                        event.entityRemove(player);
                        world.removeEntity(player);
                    }
                    case UPDATE -> {
                        Player player = (Player) world.getEntity(update.entitySerial);
                        world.updateEntity(player, update);
                        event.entityUpdate(player);
                    }
                }
            } else
                logger.warning("Unsupported entity");
        }
    }

    private void receiver() {
        while (true) {
            try {
                Serializable obj = in.readObject();
                // Connection close
                if (obj == null) break;
                // Process package
                if (obj instanceof PlayerConsoleOutput playerConsole) {
                    if (playerConsole.log != null) logger.info(playerConsole.log.trim());
                    if (playerConsole.error != null) logger.info(playerConsole.error.trim());
                } else if (obj instanceof ServerResponse c) {
                    synchronized (codeCompileResultLock) {
                        this.codeCompileResult = c;
                        codeCompileResultLock.notifyAll();
                    }
                } else if (obj instanceof WorldEntityUpdate entityUpdate) {
                    calculateEntityUpdate(entityUpdate);
                } else
                    logger.log(Level.SEVERE, "Unknown package: " + obj.getClass().getName());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error", e);
                break;
            }
        }
        disconnect();
    }

    public ServerResponse sendPlayerCode(String source) {
        PlayerCodeUpload codeUpload = new PlayerCodeUpload(source);
        try {
            out.write(codeUpload);
        } catch (IOException e) {
            return new ServerResponse(false, e.getMessage());
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

    public ServerResponse connect(String playerName) {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 1000);
            in = new TCPDataReader(socket.getInputStream());
            out = new TCPDataWriter(socket.getOutputStream());
            logger.info("Connection Successful!");
            Serializable serverResponse;
            // Login
            out.write(new PlayerLogin(1, playerName));
            serverResponse = in.readObject();
            if (serverResponse instanceof ServerResponse response) {
                reciverThread.start();
                return response;
            }
        } catch (IOException e) {
            return new ServerResponse(false, e.getMessage());
        }
        return null;
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
