package org.remad.sslserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple kept SSL Server.
 */
public class ThreadPooledSSLServer {

    /**
     * Main class of ssl server.
     *
     * @param args The arguments of shell to process.
     */
    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = ServerSocketFactory.initServerSocket();
        } catch (IOException e) {
            Logger.getLogger(ThreadPooledSSLServer.class.getName())
                    .log(Level.SEVERE, e.getLocalizedMessage(), e);
            return;
        }

        Thread serverThread;
        ThreadPooledServerRunnable server = new ThreadPooledServerRunnable(serverSocket, 100);
        serverThread = new Thread(server);
        serverThread.start();

        if (server.isStopped) {
            server.stop();
        }
    }
}
