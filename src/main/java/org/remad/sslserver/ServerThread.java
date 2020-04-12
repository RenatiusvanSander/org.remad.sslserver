package org.remad.sslserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 */
public class ServerThread extends Thread {

    protected Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

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
            try {
                String remoteSocketAddress = socket.getRemoteSocketAddress().toString();
                socket.close();
                System.out.println("Closed remote socket: " + remoteSocketAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Terminated thread.");
        stop();
    }
}
