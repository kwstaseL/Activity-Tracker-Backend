package activity.mapreduce;

import java.util.ArrayList;

import activity.calculations.ActivityCalculator;
import activity.calculations.ActivityStats;

import activity.misc.Pair;
import activity.parser.Waypoint;
import activity.parser.Chunk;

public class Map
{
    // This is the method that will get called by the worker to map a chunk to a pair of clientID and activity stats
    // The clientID is used to identify the client that requested the activity stats
    // The activity stats are the result of the map operation for that chunk.
    // TODO: This method does not need to be synchronized? because its not accessing any shared data
    public static Pair<Integer, Pair<Chunk, ActivityStats>> map(int clientID, Chunk chunk)
    {
        ArrayList<Waypoint> waypoints = chunk.getWaypoints();

        Waypoint w1 = waypoints.get(0);
        double totalDistance = 0.0;
        double totalElevation = 0.0;
        double totalTime = 0.0;
        double averageSpeed = 0.0;

        ActivityStats stats;

        for (int i = 1; i < waypoints.size(); ++i)
        {
            Waypoint w2 = waypoints.get(i);
            stats = ActivityCalculator.calculateStats(w1, w2);
            totalDistance += stats.getDistance();
            averageSpeed += stats.getSpeed();
            totalTime += stats.getTime();
            totalElevation += stats.getElevation();
            w1 = waypoints.get(i);
        }

        averageSpeed = (totalTime > 0) ? totalDistance / (totalTime / 60.0) : 0.0;
        ActivityStats finalStats = new ActivityStats(totalDistance, averageSpeed, totalElevation, totalTime);

        // statsPair: Represents a pair of chunk and the activity stats calculated for it
        Pair<Chunk, ActivityStats> statsPair = new Pair<>(chunk, finalStats);

        return new Pair<>(clientID, statsPair);
    }

}