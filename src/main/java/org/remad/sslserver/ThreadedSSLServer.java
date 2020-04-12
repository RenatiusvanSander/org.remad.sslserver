package org.remad.sslserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple kept SSL Server.
 */
public class ThreadedSSLServer {

    private static final int PORT = 8000;

    /**
     * Main class of ssl server.
     * @param args The arguments of shell to process.
     */
    public static void main(String[] args) {
        ServerSocket serverSocket;
        Socket socket;

        try {
            // ToDo refactor that to initServer
            ServerSocketInit.initKeystore();
            serverSocket = ServerSocketInit.createServerSocket(PORT, 1000, "192.168.1.11");
        } catch (IOException e) {
            Logger.getLogger(ThreadedSSLServer.class.getName())
                    .log(Level.SEVERE, e.getLocalizedMessage(), e);
            return;
        }

        while (serverSocket != null) {
            // main loop to accept ssl client connections.
            try {
                socket = serverSocket.accept();
                new ServerThread(socket).start();
            } catch (IOException | RuntimeException ex) {
                Logger.getLogger(ThreadedSSLServer.class.getName())
                        .log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
