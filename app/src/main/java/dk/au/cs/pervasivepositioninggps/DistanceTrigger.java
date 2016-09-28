package dk.au.cs.pervasivepositioninggps;

/**
 * Created by peppe_000 on 28-09-2016.
 */

public class DistanceTrigger extends Trigger {
    private int m_TriggerDistance;
    private NmeaMonitor nm;

    public DistanceTrigger(int distance)
    {
        m_TriggerDistance = distance;
    }

    @Override
    public boolean isTriggered(GpggaMeasurement gm) {
        GpggaMeasurement previousMeasurement = nm.measurements.get(nm.measurements.size()-1);
        double r = 6364;
        double lat1 = previousMeasurement.longitude;
        double lat2 = gm.longitude;
        double dlat = lat2 - lat1;
        double dlon = gm.latitude - nm.measurements.get(nm.measurements.size()-1).latitude;
        double a = Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2);
        double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a) );
        double d = r * c;
        if (d > m_TriggerDistance){
            savedPositions.add(gm);
            return true;
        }
        return false;
    }
}
