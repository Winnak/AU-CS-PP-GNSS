package dk.au.cs.pervasivepositioninggps;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Created by peppe_000 on 28-09-2016.
 */

public class DistanceTrigger extends Trigger {
    @Override
    public boolean IsTriggered(NmeaMonitor nm, GpggaMeasurement gm) {
        double r = 6364;
        double lat1 = nm.measurements.get(nm.measurements.size()-1).longitude;
        double lat2 = gm.longitude;
        double dlat = lat2 - lat1;
        double dlon = gm.latitude - nm.measurements.get(nm.measurements.size()-1).latitude;
        double a = Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2);
        double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a) );
        double d = r * c;
        if (d > gm.distanceDifference){
            return true;
        }
        return false;
    }
}
public class TimeTrigger extends Trigger {
    @Override
    public boolean IsTriggered(NmeaMonitor nm, GpggaMeasurement gm) {

        double startTime = Double.parseDouble(nm.measurements.get(nm.measurements.size()-1).time);
        double endTime = Double.parseDouble(gm.time);

        if (endTime - startTime > gm.timeDifference){
            return true;
        }
        return false;
    }
}
public class MaximumSpeedTrigger extends Trigger {
    @Override
    public boolean IsTriggered(NmeaMonitor nm, GpggaMeasurement gm) {
        double startTime = Double.parseDouble(nm.measurements.get(nm.measurements.size()-1).time);
        double endTime = Double.parseDouble(gm.time);

        double r = 6364;
        double lat1 = nm.measurements.get(nm.measurements.size()-1).longitude;
        double lat2 = gm.longitude;
        double dlat = lat2 - lat1;
        double dlon = gm.latitude - nm.measurements.get(nm.measurements.size()-1).latitude;
        double a = Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2);
        double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a) );
        double d = r * c;
        double speed = (d / (endTime - startTime));
        gm.reFix = speed / gm.maxSpeed;
        if (gm.reFix < 1){
            return true;
        }
        return false;
    }
}
