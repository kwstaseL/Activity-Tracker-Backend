package activity.mapreduce;

import activity.calculations.ActivityStats;
import activity.calculations.SegmentStats;
import activity.misc.Pair;

import java.util.ArrayList;

public class Reduce
{
    // This is the method that will reduce all the intermediate results received from the workers
    // into a single result that will be sent to the client
    public static ActivityStats reduce(Pair<Integer, ArrayList<ActivityStats>> intermediateResults)
    {
        // ensuring the arraylist is not null, and contains at least 1 element
        assert intermediateResults.getValue() != null && intermediateResults.getValue().size()>0;
        ArrayList<ActivityStats> activityStatsList = intermediateResults.getValue();

        double elevation = 0;
        double time = 0;
        double distance = 0;
        double totalSpeed = 0;
        int routeID = activityStatsList.get(0).getRouteID();
        ArrayList<SegmentStats> finalSegmentStats = new ArrayList<>();

        // iterating over the stats returned by each chunk of our route
        for (ActivityStats stats : activityStatsList)
        {
            elevation += stats.getElevation();
            time += stats.getTime();
            distance += stats.getDistance();
            totalSpeed += stats.getSpeed();

            // get the list of segment stats that this chunk contained
            ArrayList<SegmentStats> chunkSegmentStats = stats.getSegmentStatsList();

            // for each segment stats in the list above:
            for (SegmentStats segmentStats : chunkSegmentStats)
            {
                // check if these segment stats are already included in our final stats (by comparing their segmentID)
                int segmentStatsIndex = finalSegmentStats.indexOf(segmentStats);
                // if these segment stats are not included in our final stats yet, add them
                if (segmentStatsIndex == -1)
                {
                    finalSegmentStats.add(segmentStats);
                    continue;
                }
                // else (meaning we have already found other stats about this segment), update the total time
                SegmentStats currentSegmentStats = finalSegmentStats.get(segmentStatsIndex);
                currentSegmentStats.timeUpdate(segmentStats.getTime());
            }
        }

        return new ActivityStats(routeID, distance, totalSpeed / activityStatsList.size(), elevation, time, finalSegmentStats);
    }
}
