package org.remad.sslserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This handles a client socket connection in a thread.
 */
public class WorkerRunnable implements Runnable {

    protected Socket clientSocket; // Required.
    protected String name; // Required.
    protected boolean isStopped = false;

    /**
     * Creates a new instance of WorkerRunnable.
     *
     * @param clientSocket The client socket
     * @param name         The name of this WorkerRunnable
     */
    public WorkerRunnable(Socket clientSocket, String name) {
        this.clientSocket = clientSocket;
        this.name = name;
    }

    /**
     * Runs an echo to the client.
     */
    @Override
    public void run() {
        PrintWriter out = null;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null && out != null) {
                System.out.println(line);
                out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stop();
    }

    /**
     * Stops this WorkerRunnable instance.
     */
    public void stop() {
        isStopped = true;
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing client socket.", e);
        }
    }

    /**
     * @return Returns in case not stopped false and in case of stopped true.
     */
    private boolean isStopped() {
        return isStopped;
    }
}
