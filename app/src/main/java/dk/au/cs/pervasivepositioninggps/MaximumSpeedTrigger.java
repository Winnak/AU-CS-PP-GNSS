package dk.au.cs.pervasivepositioninggps;

/**
 * Created by Erik on 28-09-2016.
 */

public class MaximumSpeedTrigger extends Trigger {
    private int m_TriggerSpeed;
    private NmeaMonitor nm;

    public MaximumSpeedTrigger(int speed)
    {
        m_TriggerSpeed = speed;
    }

    @Override
    public boolean isTriggered(GpggaMeasurement newMeasurement) {
        GpggaMeasurement previousMeasurement = nm.measurements.get(nm.measurements.size()-1);

        double startTime = Double.parseDouble(previousMeasurement.time);
        double endTime = Double.parseDouble(newMeasurement.time);

        double r = 6364;
        double lat1 = previousMeasurement.longitude;
        double lat2 = newMeasurement.longitude;
        double dlat = lat2 - lat1;
        double dlon = newMeasurement.latitude - previousMeasurement.latitude;
        double a = Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2);
        double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a) );
        double d = r * c;
        double speed = (d / (endTime - startTime));
        double reFix = speed / m_TriggerSpeed;
        if (reFix < 1){
            savedPositions.add(newMeasurement);
            return true;
        }
        return false;
    }
}
