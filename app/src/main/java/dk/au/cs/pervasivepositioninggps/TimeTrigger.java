package dk.au.cs.pervasivepositioninggps;

/**
 * Created by Erik on 28-09-2016.
 */

public class TimeTrigger extends Trigger {
    private int m_TriggerTime;
    private NmeaMonitor nm;

    public TimeTrigger(int time)
    {
        m_TriggerTime = time;
    }

    @Override
    public boolean isTriggered(GpggaMeasurement gm) {
        GpggaMeasurement previousMeasurement = nm.measurements.get(nm.measurements.size()-1);
        double startTime = Double.parseDouble(previousMeasurement.time);
        double endTime = Double.parseDouble(gm.time);

        if (endTime - startTime > m_TriggerTime){
            savedPositions.add(gm);
            return true;
        }
        return false;
    }
}
