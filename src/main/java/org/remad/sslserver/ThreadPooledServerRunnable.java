package org.remad.sslserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implements the Runnable and handles ssl connections in a thread pool, which runs Worker.
 * // ToDo refactor to Singleton to avoid and deny parallel thread pools.
 */
public class ThreadPooledServerRunnable implements Runnable {

    /**
     * Creates new instance of ThreadPooledServer.
     *
     * @param serverSocket The socket of the server.
     */
    public ThreadPooledServerRunnable(ServerSocket serverSocket, int numberOfWorkers) {
        this.serverSocket = serverSocket;
        limitedhreadPooledWorkers = numberOfWorkers;
        threadPool = Executors.newFixedThreadPool(limitedhreadPooledWorkers);
    }

    /**
     * Runs ThreadPooled client connections in WorkerRunnables.
     */
    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        while (!isStopped) {
            Socket clientSocket;

            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("ChatServer stopped!");
                    break;
                }
                throw new RuntimeException("Error accepting client connection.", e);
            }
            threadPool.execute(new WorkerRunnable(clientSocket, "Thread Pooled Server"));
        }
    }

    public synchronized void stop() {
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private boolean isStopped() {
        return isStopped;
    }

    private final ServerSocket serverSocket;

    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected int limitedhreadPooledWorkers;
    protected ExecutorService threadPool;
}
