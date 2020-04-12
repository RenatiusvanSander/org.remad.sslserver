package org.remad.sslserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Server thread processes all data of a connection.
 */
public class ServerThread extends Thread {

    protected Socket socket;

    /**
     * Creates a new instance of Server Thread.
     * @param socket The socket to this client
     */
    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    /**
     * Processes client data of this socket.
     */
    public void run() {
        PrintWriter out = null;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader bufferedReader =
                     new BufferedReader(
                             new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null && out != null) {
                System.out.println(line);
                out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            // Closes socket anyway.
            try {
                String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
                socket.close();
                System.out.println("Closed remote socket: " + remoteSocketAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Terminated thread: " + this.getId() + ".");
        stop();
    }
}
