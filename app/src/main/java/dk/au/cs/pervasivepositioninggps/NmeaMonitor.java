package dk.au.cs.pervasivepositioninggps;

import android.location.GpsStatus.NmeaListener;
import android.util.Log;
import java.util.ArrayList;

/**
 * Created by Erik on 27-09-2016.
 */

public class NmeaMonitor implements NmeaListener {
    public ArrayList<GpggaMeasurement> measurements = new ArrayList<GpggaMeasurement>();
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        if (!nmea.startsWith("$GPGGA")) {
            return;
        }

        GpggaMeasurement fix = GpggaMeasurement.parseString(nmea);

        if (fix.isValid()) {
            Log.i("Localization", timestamp + ": " + nmea);
            measurements.add(fix);
        }
    }
}
