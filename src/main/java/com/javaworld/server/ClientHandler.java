package com.javaworld.server;

import com.javaworld.core.jwentities.Self;
import com.javaworld.data.PlayerCodeUpload;
import com.javaworld.data.PlayerLogin;
import com.javaworld.data.ServerResponse;
import com.javaworld.util.TCPDataReader;
import com.javaworld.util.TCPDataWriter;
import com.wavjaby.serializer.Serializable;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.javaworld.server.Server.compiler;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(Server.class.getSimpleName());
    private final ClientEvent clientEvent;
    private final Socket socket;
    private final TCPDataReader in;
    public final TCPDataWriter out;

    private boolean closed = false;
    public String name;
    public CompiledResult compiled;
    public Self player;
    public boolean illegal;

    public ClientHandler(Socket socket, ClientEvent clientEvent) throws IOException {
        this.clientEvent = clientEvent;
        this.socket = socket;
        this.in = new TCPDataReader(socket.getInputStream());
        this.out = new TCPDataWriter(socket.getOutputStream());
    }

    private Serializable onReceive(Serializable data) {
        if (data instanceof PlayerCodeUpload playerCode) {
            CompiledResult result = compiler.compileCode(playerCode.sourceCode);
            if (result.success) compiled = result;
            else compiled = null;
            logger.info("[" + name + "] Code compiled");
            return new ServerResponse(result.success, result.message);
        }

        return new ServerResponse(false, "Unsupported package: " + data.getClass().getName());
    }

    @Override
    public void run() {
        try {
            Serializable login = in.readObject();
            if (login instanceof PlayerLogin playerLogin) {
                name = playerLogin.name;
                if (name == null || name.isBlank())
                    out.write(new ServerResponse(false, "Login failed, Illegal player name: '" + name + "'"));
                else if (!clientEvent.clientConnect(this))
                    out.write(new ServerResponse(false, "Login failed, Player name: '" + name + "' already exists"));
                else {
                    // Login success
                    out.write(new ServerResponse(true, null));
                    receiverLoop();
                }
            } else
                out.write(new ServerResponse(false, "Login failed, wrong package type '" + login.getClass().getName() + "'"));
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
            in.close();
            out.close();
            socket.close();
            if (compiled != null) compiled.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        clientEvent.clientDisconnect(this);
    }
}
