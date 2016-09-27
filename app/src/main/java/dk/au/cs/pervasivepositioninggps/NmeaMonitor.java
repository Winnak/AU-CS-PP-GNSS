package dk.au.cs.pervasivepositioninggps;

import android.location.GpsStatus.NmeaListener;
import android.util.Log;

/**
 * Created by Erik on 27-09-2016.
 */

public class NmeaMonitor implements NmeaListener {
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        Log.i("Localization", timestamp + ": " + nmea);
    }
}
