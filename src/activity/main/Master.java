package activity.main;

import activity.parser.Route;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

public class Master
{
    // This is the socket that the client will connect to
    private ServerSocket clientSocket;
    // This is the socket that the worker will connect to
    private ServerSocket workerSocket;
    // Queue containing the routes that will be sent to the workers
    private Queue<Route> routes;
    // Queue containing all the worker handlers
    private Queue<WorkerHandler> workerHandlers;
    // Lookup table that will map the client id to the appropriate client handler
    private HashMap<Integer,ClientHandler> clientMap;
    private int numOfWorkers;
    public Master()
    {
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream("config.properties"));

            final int WORKER_PORT = Integer.parseInt(config.getProperty("worker_port"));
            final int CLIENT_PORT = Integer.parseInt(config.getProperty("client_port"));
            this.numOfWorkers = Integer.parseInt(config.getProperty("number_of_workers"));

            clientSocket = new ServerSocket(CLIENT_PORT);
            workerSocket = new ServerSocket(WORKER_PORT);
            workerHandlers = new LinkedList<>();
            clientMap = new HashMap<>();
            routes = new LinkedList<>();
        }
        catch (Exception e)
        {
            System.out.println("Could not create sockets");
            System.out.println("Error: " + e.getMessage());
        }
    }

    // This method will start the master and all the threads
    private void start()
    {
        // Thread that will create and start the workers
        Thread init = new Thread(() ->
        {
            for (int i = 0; i < numOfWorkers; i++)
            {
                Worker worker = new Worker();
                worker.start();
            }
        });

        // Thread that will handle the clients
        Thread handleClient = new Thread(() ->
        {
            while (!clientSocket.isClosed())
            {
                try
                {
                    System.out.println("MASTER: Waiting for client connection");
                    // Accept a client connection
                    Socket client = clientSocket.accept();
                    System.out.println("MASTER: Client connected");
                    // Create a new thread to handle the client
                    ClientHandler clientHandler = new ClientHandler(client,routes);
                    // Add the client handler to the lookup table
                    int clientID = clientHandler.getClientID();
                    clientMap.put(clientID,clientHandler);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();

                } catch (Exception e)
                {
                    System.out.println("Could not accept client connection");
                    try
                    {
                        clientSocket.close();

                    } catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("Client connection closed");
                }
            }
        });
        // Thread that will handle the workers
        Thread handleWorker = new Thread(() ->
        {
            while (!workerSocket.isClosed())
            {
                try
                {
                    System.out.println("MASTER: Waiting for worker connection");
                    // Accept a worker connection
                    Socket worker = workerSocket.accept();
                    System.out.println("MASTER: Worker connected");
                    // Create a new thread to handle the worker also passing the client map
                    // so that the worker can send the results to the appropriate client
                    WorkerHandler workerHandler = new WorkerHandler(worker,clientMap);
                    workerHandlers.add(workerHandler);

                    Thread workerThread = new Thread(workerHandler);
                    workerThread.start();

                } catch (Exception e)
                {
                    System.out.println("Could not accept worker connection");
                    e.printStackTrace();
                    try
                    {
                        workerSocket.close();
                    } catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("Worker connection closed");
                    System.out.println("Error: " + e.getMessage());
                }

            }
        });

        // Thread that will start dispatching work to the workers
        // We are passing the worker handler so that the work dispatcher can send work to the workers
        // We are also passing the routes, which is a shared memory between the client handler and the work dispatcher
        // The client-handler will upload the routes to the work dispatcher and the work dispatcher will send the routes to the workers
        Thread dispatchWork = new Thread(() ->
        {
            WorkDispatcher workDispatcher = new WorkDispatcher(workerHandlers, routes);
            Thread workDispatcherThread = new Thread(workDispatcher);
            workDispatcherThread.start();
        });

        handleWorker.start();
        dispatchWork.start();
        handleClient.start();
        init.start();
    }

    public static void main(String[] args)
    {
        Master master = new Master();
        master.start();
    }

}