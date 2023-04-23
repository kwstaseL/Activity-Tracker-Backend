package activity.main;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class Client
{
    // This is the socket that the client is connected to with the master
    private Socket connection;

    // This is the output stream that will be used to send objects to the master
    private ObjectOutputStream out;

    // This is the input stream that will be used to receive objects from the master
    private ObjectInputStream in;

    // This is the file that will be sent to the master
    private File file;

    // initialised: represents the state of Client objects. If false, clients cannot be initialised.
    private static boolean initialised = false;

    // unprocessedDirectory: The directory with all the gpx available for processing
    private static String directory;
    private static String masterIP;
    private static int clientPort;

    // lock: dummy object used for synchronization
    private final Object lock = new Object();

    public Client(File file)
    {
        if (!initialised) {
            clientInitialisation();
        }

        this.file = file;

        // Create a socket that will connect to the master
        try {
            connection = new Socket(masterIP, clientPort);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (Exception e) {
            System.out.println("Could not connect to master");
            shutdown();
            throw new RuntimeException(e);
        }
    }

    /* clientInitialisation: To be called before the first Client object is instantiated, or during the first Client instantiation.
     * Initiates all the necessary attributes a Client object should be aware of.
     */
    public static void clientInitialisation()
    {
        // if the Client class has already been initialised, return
        if (Client.initialised) {
            return;
        }

        Properties config = new Properties();
        try {
            config.load(new FileInputStream("config.properties"));
            masterIP = config.getProperty("master_ip");
            clientPort = Integer.parseInt(config.getProperty("client_port"));
            directory = config.getProperty("unprocessed_directory");
            initialised = true;
        } catch (Exception e) {
            System.out.println("Initialisation of clients failed.");
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void sendFile()
    {
        try
        {
            System.out.println("Sending file " + file.getName() + " to master\n");
            out.writeObject(file);
            out.flush();
        }
        catch (Exception e)
        {
            System.out.println("Could not send file");
            shutdown();
        }
    }

    public void listenForMessages()
    {
        try
        {
            Object routeStats = in.readObject();
            System.out.println("Output for file | " + file.getName() + " | " + routeStats + "\n");
            Object userStats = in.readObject();
            System.out.println(userStats + "\n");
            Object allUsersStats = in.readObject();
            System.out.println(allUsersStats + "\n");
        }
        catch (Exception e)
        {
            System.out.println("Could not receive object");
            shutdown();
        }

    }

    private void shutdown()
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (IOException e)
            {
                System.out.println("Could not close connection");
            }
        }
        if (out != null)
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                System.out.println("Could not close output stream");
            }
        }
        if (in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                System.out.println("Could not close input stream");
            }
        }
    }

    private static void sendRoute() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Available files:");
            File directory = new File(Client.directory);

            // directoryContents: Lists all the files (not folders) included in our directory.
            // (essentially just excluding the segment folder)
            File[] directoryContents = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile();   // file.isFile(): returns false if the file is a directory (like segments)
                }
            });

            // if our directory is empty, there is nothing left to process
            if (directoryContents == null || directoryContents.length == 0) {
                System.out.println("No routes are available for processing!");
                return;
            }

            // list all routes
            for (int i = 0; i < directoryContents.length; i++) {
                System.out.println(i + ": " + directoryContents[i].getName());
            }

            // list all segments
            File segmentsDirectory = new File(Client.directory + "/segments");
            File[] segmentContents = segmentsDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile();
                }
            });
            System.out.println("Available segments:");
            for (int i = 0; i < segmentContents.length; i++) {
                System.out.println((i + directoryContents.length) + ": " + segmentContents[i].getName());
            }

            // Prompt user to select a route or segment
            String input = null;
            Integer choice = null;

            // Acceptable input: all or "all" to send all routes/segments, or anything in the range of 0 to the total number
            // of routes/segments to send a single route/segment.
            while (choice == null || choice < 0 || choice >= directoryContents.length + segmentContents.length) {
                System.out.println("\nEnter \"all\" to send all routes/segments, or enter a file index (0-"
                        + (directoryContents.length + segmentContents.length - 1) +") to send a single route/segment:");

                input = scanner.nextLine();

                if (input.equals("all"))
                {
                    // send all routes/segments
                    for (File file : directoryContents)
                    {
                        new Client(file).sendFile();
                    }
                    for (File file : segmentContents)
                    {
                        new Client(file).sendFile();
                    }
                    return;

                } else
                {
                    try {
                        choice = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a valid file index or \"all\".");
                        continue;
                    }

                    if (choice < 0 || choice >= directoryContents.length + segmentContents.length) {
                        System.out.println("Invalid input. Please enter a valid file index or \"all\".");
                        choice = null;
                        continue;
                    }

                    File file;
                    if (choice < directoryContents.length) {
                        // user selected a route
                        file = directoryContents[choice];
                    } else {
                        // user selected a segment
                        file = segmentContents[choice - directoryContents.length];
                    }

                    // send the selected route/segment
                    Client client = new Client(file);
                    client.sendFile();
                    client.listenForMessages();
                }
            }
        }
    }


    private static void sendAllRoutes(File[] directoryContents)
    {
        for (File file : directoryContents)
        {
            Client client = new Client(file);
            Thread clientThread = new Thread(client::sendFile);
            clientThread.start();
            client.listenForMessages();
        }
    }


    public static void main(String[] args)
    {
        clientInitialisation();
        sendRoute();
    }

}