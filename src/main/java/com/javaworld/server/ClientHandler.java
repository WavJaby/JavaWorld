package com.javaworld.server;

import com.javaworld.adapter.PlayerApplication;
import com.javaworld.core.jwentities.Self;
import com.javaworld.data.PlayerCodeData;
import com.javaworld.data.PlayerLoginData;
import com.javaworld.data.ServerResponseData;
import com.javaworld.util.TCPDataReader;
import com.javaworld.util.TCPDataWriter;
import com.wavjaby.serializer.Serializable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.javaworld.server.Server.compiler;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(Server.class.getSimpleName());
    private static final Field playerOutField, playerErrField;
    private final ClientEvent clientEvent;
    private final Socket socket;
    private final TCPDataReader in;
    public final TCPDataWriter out;

    private boolean closed = false;
    public String name;
    public Self player;
    public boolean playerInit;
    public CompiledResult compiled;
    public boolean playerApplicationInit;
    public ByteArrayOutputStream playerOut, playerErr;

    static {
        Field outField_ = null, errField_ = null;
        try {
            outField_ = PlayerApplication.class.getDeclaredField("out");
            outField_.setAccessible(true);
            errField_ = PlayerApplication.class.getDeclaredField("err");
            errField_.setAccessible(true);
        } catch (NoSuchFieldException e) {
            logger.log(Level.SEVERE, "Could not find PlayerApplication console out", e);
        }
        playerOutField = outField_;
        playerErrField = errField_;
    }

    public ClientHandler(Socket socket, ClientEvent clientEvent) throws IOException {
        this.clientEvent = clientEvent;
        this.socket = socket;
        this.in = new TCPDataReader(socket.getInputStream());
        this.out = new TCPDataWriter(socket.getOutputStream());
    }

    private Serializable onReceive(Serializable data) {
        // Compile player code
        if (data instanceof PlayerCodeData playerCode) {
            closeCompiled();
            playerApplicationInit = false;
            // Player stop code
            if (playerCode.sourceCode == null)
                return new ServerResponseData(true, null);
            // Compile new code
            CompiledResult result = compiler.compileCode(playerCode.sourceCode);
            logger.info("[" + name + "] Code compiled");
            if (result.success) {
                compiled = result;
                try {
                    playerOut = (ByteArrayOutputStream) playerOutField.get(result.playerApplication);
                    playerErr = (ByteArrayOutputStream) playerErrField.get(result.playerApplication);
                } catch (IllegalAccessException e) {
                    playerOut = null;
                    playerErr = null;
                    return new ServerResponseData(false, "Can not get player output stream");
                }
            } else {
                // Compile failed
                compiled = null;
                playerOut = null;
                playerErr = null;
            }
            return new ServerResponseData(result.success, result.message);
        }

        return new ServerResponseData(false, "Unsupported package: " + data.getClass().getName());
    }

    @Override
    public void run() {
        try {
            Serializable login = in.readObject();
            if (login instanceof PlayerLoginData playerLoginData) {
                name = playerLoginData.name;
                if (name == null || name.isBlank())
                    out.write(new ServerResponseData(false, "Login failed, Illegal player name: '" + name + "'"));
                else if (!clientEvent.clientConnect(this))
                    out.write(new ServerResponseData(false, "Login failed, Player name: '" + name + "' already exists"));
                else {
                    // Login success
                    out.write(new ServerResponseData(true, null));
                    receiverLoop();
                }
            } else
                out.write(new ServerResponseData(false, "Login failed, wrong package type '" + login.getClass().getName() + "'"));
        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Error", e);
        } finally {
            disconnect();
        }
    }

    private void receiverLoop() throws IOException {
        while (true) {
            Serializable data = in.readObject();
            // Connection close
            if (data == null) break;

            out.write(onReceive(data));
        }
    }

    public void disconnect() {
        if (closed) return;
        closed = true;
        try {
            if (playerOut != null) playerOut.close();
            if (playerErr != null) playerErr.close();
            in.close();
            out.close();
            socket.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        closeCompiled();
        clientEvent.clientDisconnect(this);
    }

    public void closeCompiled() {
        if (compiled != null) {
            try {
                compiled.close();
            } catch (IOException ignore) {
            }
            compiled = null;
        }
    }
}
