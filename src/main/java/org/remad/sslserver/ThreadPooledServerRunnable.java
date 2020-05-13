package org.remad.sslserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implements the Runnable and handles ssl connections in a thread pool, which runs Worker.
 * ToDo refactor to Singleton to avoid and deny parallel thread pools.
 * @author Remy Meier
 */
public class ThreadPooledServerRunnable implements Runnable {

    /**
     * Creates new instance of ThreadPooledServerRunnable
     * @param serverSocket The socket of the server.
     * @param numberOfWorkers The amount of {@link Worker} for the thread pool.
     */
    public ThreadPooledServerRunnable(ServerSocket serverSocket, int numberOfWorkers) {
        this.serverSocket = serverSocket;
        limitedThreadPooledWorkers = numberOfWorkers;
        threadPool = Executors.newFixedThreadPool(limitedThreadPooledWorkers);
    }

    /**
     * Runs ThreadPooled client connections in WorkerRunnables.
     */
    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        while (!isStopped()) {
            // Accepts incoming client socket connection.
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
            Worker worker = new Worker(clientSocket, clientSocket.toString(), this);
            getWorkers().add(worker);
            threadPool.execute(worker);
        }
    }

    /**
     * Stops ThreadPooledServerRunnable.
     */
    public synchronized void stop() {
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    /**
     * @return Returns a list of all Workers.
     */
    public List<Worker> getWorkers() {
        return workers;
    }

    /**
     * @return In case this thread stopped {@code true} or in case of run it is {@code false}.
     */
    public boolean isStopped() {
        return isStopped;
    }

    private final ServerSocket serverSocket;
    private final int limitedThreadPooledWorkers;
    private ExecutorService threadPool;
    private boolean isStopped = false;
    private Thread runningThread = null;
    private List<Worker> workers = new ArrayList<>();
}
