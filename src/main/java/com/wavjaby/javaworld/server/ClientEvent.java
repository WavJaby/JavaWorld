package com.wavjaby.javaworld.server;

public interface ClientEvent {
    boolean clientConnect(ClientHandler clientHandler);

    void clientDisconnect(ClientHandler clientHandler);
}
