package activity.main;

import activity.parser.Waypoint;
import activity.parser.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class WorkDispatcher implements Runnable
{
    private final Queue<WorkerHandler> workers;
    private final Queue<Route> filesToWorker;

    public WorkDispatcher(Queue<WorkerHandler> workers, Queue<Route> filesToWorker)
    {
        this.workers = workers;
        this.filesToWorker = filesToWorker;
    }

    public void run()
    {
        synchronized (filesToWorker)
        {
            while (true)
            {
                while (filesToWorker.isEmpty())
                {
                    try
                    {
                        filesToWorker.wait();
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                Route route = filesToWorker.poll();
                ArrayList<Waypoint> waypoints = route.waypoints();

                int routeID = route.getRouteID();
                String clientID = route.getClientID();

                // n will represent the chunk size
                int n;

                // if there's more waypoints than workers provided, make n equal to the size of waypoints / the size of workers
                if (waypoints.size() >= workers.size()) {
                    n = waypoints.size() / workers.size();
                } else {
                    // making the assumption that if workers are more than the waypoints provided
                    n = workers.size() / waypoints.size();
                }

                int chunks = 0;
                ArrayList<Waypoint> chunk = new ArrayList<>();
                for (int i = 0; i < waypoints.size(); i++)
                {
                    chunk.add(waypoints.get(i));

                    if (chunk.size() == n || i == waypoints.size() - 1)
                    {
                        WorkerHandler worker = workers.poll();
                        assert worker != null;
                        Route chunkedRoute = new Route(chunk, routeID, clientID);
                        worker.processJob(chunkedRoute);
                        // add the worker to the end of the queue
                        workers.add(worker);
                        // clear the chunk for the next set of waypoints
                        chunk = new ArrayList<>();
                        chunks++;
                        chunk.add(waypoints.get(i));    // adding the last waypoint from the previous chunk, to not miss the connection
                    }
                }

                System.out.println("*********");
                System.out.println("*********");
                System.out.println("Finished chunking up route:" + routeID + ". Total chunks: " + chunks);
                System.out.println("*********");
                System.out.println("*********");


            }
        }
    }

}
