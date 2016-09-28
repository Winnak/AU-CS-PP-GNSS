package dk.au.cs.pervasivepositioninggps;

import java.util.ArrayList;

/**
 * Created by peppe_000 on 28-09-2016.
 */

public abstract class Trigger {
    public ArrayList<GpggaMeasurement> savedPositions = new ArrayList<>();
    public abstract boolean isTriggered(GpggaMeasurement newMeasurement);
}
