package dk.au.cs.pervasivepositioninggps;

/**
 * Created by peppe_000 on 28-09-2016.
 */

public abstract class Trigger {
    public abstract boolean IsTriggered(NmeaMonitor nm, GpggaMeasurement gm);
}
