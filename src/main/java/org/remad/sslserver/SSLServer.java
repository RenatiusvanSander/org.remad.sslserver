package org.remad.sslserver;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple kept SSL Server.
 */
public class SSLServer {

    //private final static Logger logger = Logger.getLogger(SSLServer.class.getName());
    private static final int PORT = 8000;
    // private static final int BACKLOG = 1000;
    // private Socket socket;

    public static void main(String[] args) {
        try {
            // ToDo refactor that to initServer
            // Reads ServerSettings from file initServerSettings();
            initKeystore();
            ServerSocket serverSocket = createServerSocket(PORT, 1000, "192.168.1.11");
            Socket socket = serverSocket.accept();

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            try (BufferedReader bufferedReader =
                         new BufferedReader(
                                 new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                    out.println(line);
                }
            }
            System.out.println("Closed");
        } catch (IOException | RuntimeException ex) {
            Logger.getLogger(SSLServer.class.getName())
                    .log(Level.SEVERE, ex.getLocalizedMessage(), ex);
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
