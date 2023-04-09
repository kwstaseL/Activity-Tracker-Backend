import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Master
{
    // This will be the port that the client will connect to
    public static final int CLIENT_PORT = 4321;
    // This will be the port that the worker will connect to
    public static final int WORKER_PORT = 4322;
    private Queue<File> filesFromClient;
    private ServerSocket clientSocket;
    private ServerSocket workerSocket;

    public Master()
    {
        try
        {
            clientSocket = new ServerSocket(CLIENT_PORT);
            workerSocket = new ServerSocket(WORKER_PORT);
            filesFromClient = new LinkedList<>();
        }
        catch (Exception e)
        {
            System.out.println("Could not create sockets");
            System.out.println("Error: " + e.getMessage());
        }
    }

    // This method will start the master server and listen for connections
    private void start()
    {
        Thread handleClient = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!clientSocket.isClosed())
                {
                    try
                    {
                        System.out.println("Waiting for client connection");
                        // Accept a client connection
                        Socket client = clientSocket.accept();
                        System.out.println("Client connected");
                        // Create a new thread to handle the client
                        ClientHandler clientHandler = new ClientHandler(client, filesFromClient);
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
                        // e.printStackTrace();
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }
        });

        Thread handleWorker = new Thread(new Runnable()
        {
            public void run()
            {
                while (!workerSocket.isClosed())
                {
                    try
                    {
                        System.out.println("Waiting for worker connection");
                        // Accept a worker connection
                        Socket worker = workerSocket.accept();
                        System.out.println("Worker connected");
                        // Create a new thread to handle the worker
                        WorkerHandler workerHandler = new WorkerHandler(worker);
                        Thread workerThread = new Thread(workerHandler);
                        workerThread.start();

                    } catch (Exception e)
                    {
                        System.out.println("Could not accept worker connection");
                        e.printStackTrace();
                        try
                        {

                            workerSocket.close();

                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        System.out.println("Worker connection closed");
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }
        });

        Thread handleFiles = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!clientSocket.isClosed())
                {
                    synchronized (filesFromClient)
                    {
                        // Check if there are any files in the list
                        while (filesFromClient.isEmpty())
                        {
                            System.out.println("Waiting for files from client");
                            try
                            {
                                // Wait for a file to be added to the list
                                filesFromClient.wait();
                            }
                            catch (InterruptedException e)
                            {
                                Thread.currentThread().interrupt();
                                System.out.println("Interrupted while waiting for files from client");
                                return;
                            }
                        }

                        // Get the first file from the list
                        File file = filesFromClient.poll();

                        // Send the file to the worker
                        System.out.println("Sending file to worker: " + file.getName());
                    }
                }

            }

        });

        handleClient.start();
        handleWorker.start();
        handleFiles.start();
    }

    private void closeConnection()
    {

    }

    public static void main(String[] args)
    {
        Master master = new Master();
        master.start();
    }

}