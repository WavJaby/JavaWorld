package com.javaworld.server;

import com.javaworld.client.ClientTest;
import com.javaworld.core.GameManager;
import com.javaworld.util.CustomThreadFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Server implements ClientEvent {
    private static final Logger logger = Logger.getLogger(Server.class.getSimpleName());
    public static final int port = 25565;
    public static final LiveCompiler compiler = new LiveCompiler();
    private final ThreadPoolExecutor clientPool = (ThreadPoolExecutor)
            Executors.newFixedThreadPool(100, new CustomThreadFactory("Clients"));
    private final Map<String, ClientHandler> clients = new HashMap<>();
    private final ServerSocket serverSocket;
    private final Thread serverThread;
    private final GameManager gameManager;

    public Server() throws IOException {
        serverSocket = new ServerSocket(port);

        serverThread = new Thread(this::serverHandle);
        serverThread.start();
        logger.info("Server started on port: " + port);

        long startTime = System.currentTimeMillis();
        logger.info("Loading resources...");
        GameManager.loadResources();
        logger.info("Resources done " + (System.currentTimeMillis() - startTime) + "ms");

        gameManager = new GameManager(clients);

        startTime = System.currentTimeMillis();
        logger.info("Creating world...");
        gameManager.createWorld();
        logger.info("ServerWorld done " + (System.currentTimeMillis() - startTime) + "ms");

        gameManager.startGameLoop();

        try {
            serverThread.join();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void serverHandle() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                String clientSocketIP = clientSocket.getInetAddress().toString();
                int clientSocketPort = clientSocket.getPort();
                clientPool.execute(new ClientHandler(clientSocket, this));

                int available = clientPool.getMaximumPoolSize() - clientPool.getActiveCount();
                logger.info("[" + clientSocketIP + ":" + clientSocketPort + "] Client connected, " + available);
            } catch (IOException e) {
//                logger.warning(e.toString());
            }
        }
    }

    @Override
    public boolean clientConnect(ClientHandler clientHandler) {
        if (clients.containsKey(clientHandler.name))
            return false;

        clients.put(clientHandler.name, clientHandler);
        logger.info("[" + clientHandler.name + "] Player login success");
        return true;
    }

    @Override
    public void clientDisconnect(ClientHandler clientHandler) {
        if (clientHandler.name == null)
            logger.warning("Client login failed " + clientHandler);
        // Player is init
        if (clientHandler.player != null)
            gameManager.playerLeave(clientHandler.player);
        clients.remove(clientHandler.name);
        logger.info("[" + clientHandler.name + "] Disconnected");
    }

    public static void main(String[] args) throws IOException {
        LogManager.getLogManager().readConfiguration(ClientTest.class.getResourceAsStream("/logging.properties"));
        new Server();
    }
}