package org.remad.sslserver;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple kept SSL Server.
 */
public class ThreadedSSLServer {

    //private final static Logger logger = Logger.getLogger(SSLServer.class.getName());
    private static final int PORT = 8000;
    // private static final int BACKLOG = 1000;
    // private Socket socket;

    public static void main(String[] args) {
        ServerSocket serverSocket;
        Socket socket;
        try {
            // ToDo refactor that to initServer
            // Reads ServerSettings from file initServerSettings();
            initKeystore();
            serverSocket = createServerSocket(PORT, 1000, "192.168.1.11");
        } catch (IOException e) {
            Logger.getLogger(ThreadedSSLServer.class.getName())
                    .log(Level.SEVERE, e.getLocalizedMessage(), e);
            return;
        }

        // ToDo refactor to MainServerThread.
        while (true) {
            try {
                socket = serverSocket.accept();
                // new thread for a client
                new ServerThread(socket).start();
            } catch (IOException | RuntimeException ex) {
                Logger.getLogger(ThreadedSSLServer.class.getName())
                        .log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    public static ServerSocket createServerSocket(int port, int backlog, String ip) throws IOException {
        ServerSocket serverSocket;
        try {
            InetAddress ifAddress = InetAddress.getByName(ip);
            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocket = sslServerSocketFactory.createServerSocket(port, backlog, ifAddress);
        } catch (IOException e) {
            throw new IOException("Server Socket Error: Cannot bind to " + ip + ".", e);
        }
        return serverSocket;
    }

    public static void initKeystore() throws RuntimeException {
        System.setProperty("javax.net.ssl.keyStore", "/home/rmeier/mykeystore/examplestore");
        System.setProperty("javax.net.ssl.keyStorePassword", "RemAd5619");
    }
}
