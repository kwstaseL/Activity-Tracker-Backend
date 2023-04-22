package activity.calculations;

// UserStatistics: Contains the total distance, elevation and activity time recorded for a specific user. Also keeps track of how many routesRecorded they have registered.

import java.util.ArrayList;

public class UserStatistics
{
    private double totalDistance;
    private double totalElevation;
    private double totalActivityTime;
    private ArrayList<ActivityStats> activityArchive;      // TODO: Possibly unnecessary
    private int routesRecorded;
    private String user;

    public UserStatistics(double totalDistance, double totalElevation, double totalActivityTime, String user)
    {
        this.totalDistance = totalDistance;
        this.totalElevation = totalElevation;
        this.totalActivityTime = totalActivityTime;
        this.activityArchive = new ArrayList<>();
        this.user = user;
        routesRecorded = 0;
    }

    public UserStatistics(String user)
    {
        this(0, 0, 0, user);
    }

    public void registerRoute(ActivityStats stats)
    {
        totalDistance += stats.getDistance();
        totalElevation += stats.getElevation();
        totalActivityTime += stats.getTime();
        activityArchive.add(stats);
        ++routesRecorded;
    }

    public double getAverageDistance()
    {
        assert routesRecorded >= 1;
        return totalDistance / routesRecorded;
    }

    public double getAverageElevation()
    {
        assert routesRecorded >= 1;
        return totalElevation / routesRecorded;
    }

    public double getAverageActivityTime()
    {
        assert routesRecorded >= 1;
        return totalActivityTime / routesRecorded;
    }

    public String toString()
    {
        return "routesRecorded recorded for " + user + ": " + routesRecorded + ".\n Total Distance: " +
                totalDistance + " Total Elevation: " + totalElevation + " Total Activity Time: " + totalActivityTime;
    }

}