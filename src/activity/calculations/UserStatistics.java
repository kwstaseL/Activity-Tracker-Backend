package activity.calculations;

import java.io.Serializable;

// UserStatistics: A class that represents the statistics for a specific user
public class UserStatistics implements Serializable
{
    private double totalDistance;
    private double totalElevation;
    private double totalActivityTime;

    // routesRecorded: A counter for the amount of routes a user has registered.
    // Eager approach to avoid repetitive activityArchive.length calls
    private int routesRecorded;

    // user: Represents the username as entered on the original gpx file
    private final String user;

    public UserStatistics(String user, int routesRecorded, double totalDistance, double totalElevation, double totalActivityTime)
    {
        this.totalDistance = totalDistance;
        this.totalElevation = totalElevation;
        this.totalActivityTime = totalActivityTime;
        this.user = user;
        this.routesRecorded = routesRecorded;
    }

    public UserStatistics(String user)
    {
        this(user,0, 0, 0, 0);
    }

    public void registerRoute(ActivityStats stats)
    {
        totalDistance += stats.getDistance();
        totalElevation += stats.getElevation();
        totalActivityTime += stats.getTime();
        ++routesRecorded;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getTotalElevation() {
        return totalElevation;
    }

    public double getTotalActivityTime() {
        return totalActivityTime;
    }

    public int getRoutesRecorded()
    {
        return routesRecorded;
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

    @Override
    public String toString()
    {
        return String.format("%s: \nRoutes recorded: %d\nAverage Distance: %.2f km\nAverage Elevation: %.2f m\nAverage Workout Time: %.2f minutes",
                user, routesRecorded, getAverageDistance(), getAverageElevation(), getAverageActivityTime());
    }

}
