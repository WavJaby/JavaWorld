package com.javaworld.client;

import com.javaworld.core.GameManager;
import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.update.ChunkUpdate;
import com.javaworld.data.ServerResponseData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ClientTest implements ClientGameEvent {
    private static final Logger logger = Logger.getLogger(ClientTest.class.getSimpleName());

    public ClientTest() throws IOException {
        LogManager.getLogManager().readConfiguration(ClientTest.class.getResourceAsStream("/logging.properties"));
        // Load game resources
        long startTime = System.currentTimeMillis();
        logger.info("Loading resources...");
        GameManager.loadResources();
        logger.info("Resources done " + (System.currentTimeMillis() - startTime) + "ms");

        ClientGameManager gm = new ClientGameManager("localhost", 25565, this);

        logger.info("Connecting Server...");
        long start = System.currentTimeMillis();
        ServerResponseData response = gm.connect("Player");
        if (response == null) {
            logger.severe("Unknown Error");
            return;
        } else if (!response.success) {
            logger.severe("Server Connect Error: " + response.message);
            return;
        }
        logger.info("Server Connected! " + (System.currentTimeMillis() - start) + "ms");

        response = gm.sendPlayerCode(Files.readString(Path.of("../PlayerExample/src/main/java/com/player/Main.java")));
//        if (response == null) {
//            logger.severe("Unknown Error");
//            return;
//        } else if (!response.success) {
//            logger.severe("Server Connect Error: " + response.message);
//            return;
//        }
//        logger.info("Code Submitted! ");

        gm.join();
    }

    @Override
    public void entityCreate(Entity e) {
        logger.info("Create entity: " + e.getPosition());
    }

    @Override
    public void entityUpdate(Entity e) {
//        logger.info("Update entity: " + e.getPosition());
    }

    @Override
    public void entityRemove(Entity e) {
        logger.info("Remove entity: " + e.getPosition());
    }

    @Override
    public void blockCreate(int blockX, int blockY, int blockZ, BlockData blockData, BlockState blockState) {

    }

    @Override
    public void blockRemove(int blockX, int blockY, int blockZ, BlockData blockData, BlockState blockState) {

    }

    @Override
    public void blockUpdate(int blockX, int blockY, int blockZ, BlockData blockData, BlockState blockState) {
        logger.info("Update block: " + blockData);
    }

    @Override
    public void chunkInit(ChunkUpdate chunkUpdate) {
        logger.info("Update chunk: (" + chunkUpdate.chunkX + ", " + chunkUpdate.chunkY + ")");
    }

    long t = System.currentTimeMillis();

    @Override
    public void playerScoreUpdate(String[] playerNames, int[] playerScore) {
        if (System.currentTimeMillis() - t < 1000)
            return;
        t = System.currentTimeMillis();

        logger.info(Arrays.toString(playerNames));
        logger.info(Arrays.toString(playerScore));
    }

    @Override
    public void playerLog(String log) {

    }

    @Override
    public void playerError(String log) {

    }

    public static void main(String[] args) throws IOException {
        new ClientTest();
    }
}
