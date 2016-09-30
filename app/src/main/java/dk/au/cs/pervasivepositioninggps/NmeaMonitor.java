package dk.au.cs.pervasivepositioninggps;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssMeasurement;
import android.location.GpsStatus.NmeaListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Erik on 27-09-2016.
 */

public class NmeaMonitor implements NmeaListener {
    private enum Mode {
        NONE, SINGLE, TIME, DISTANCE, MAXSPEED, MOVEMENT
    }

    private Mode m_Mode = Mode.NONE;

    private final int kFoundColor = 0xAA00FF00;
    private final int kSearchingColor = 0xAAFFFF00;
    private final double kMpsDistance = 50;

    public ArrayList<GpggaMeasurement> measurements = new ArrayList<GpggaMeasurement>();
    private int m_ReadingsCount = 0;

    private LocationManager m_LocMan;
    private Context m_AppContext;
    private ImageView m_StatusIcon;
    private TextView m_MeasurementCountLabel;
    private TextView m_ReadingsCountLabel;
    private PendingIntent m_Intent;

    private double m_DisatnceThreshold = 0;
    private double m_TimeThreshold = 0;

    public NmeaMonitor(LocationManager locman, Context appContext, ImageView statusIcon, TextView countLabel, TextView readingscountLabel) {
        this.m_LocMan = locman;
        this.m_AppContext = appContext;
        this.m_StatusIcon = statusIcon;
        this.m_MeasurementCountLabel = countLabel;
        this.m_ReadingsCountLabel = readingscountLabel;

        m_StatusIcon.setColorFilter(kFoundColor);

        Intent intent = new Intent(m_AppContext, this.getClass());

        m_Intent = PendingIntent.getBroadcast(m_AppContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void getSingleFix() {
        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.addNmeaListener(this);
        m_LocMan.requestSingleUpdate(LocationManager.GPS_PROVIDER, m_Intent);
        m_Mode = Mode.SINGLE;
        m_DisatnceThreshold = 0;
        m_TimeThreshold  = 0;
        measurements.clear();
    }

    public void getTimeFix(int time) {
        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.addNmeaListener(this);
        m_LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, time * 1000, 0, m_Intent);
        m_Mode = Mode.TIME;
        m_DisatnceThreshold = 0;
        m_TimeThreshold = time;
        measurements.clear();
    }

    public void getDistanceFix(int meters) {
        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.addNmeaListener(this);
        m_LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, m_Intent);
        m_Mode = Mode.DISTANCE;
        m_DisatnceThreshold = meters;
        m_TimeThreshold = 0;
        measurements.clear();
    }

    public void getMaxSpeedFix(int meterspersec) {
        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.addNmeaListener(this);
        m_LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, m_Intent);
        m_Mode = Mode.MAXSPEED;
        m_DisatnceThreshold = kMpsDistance;
        m_TimeThreshold = (kMpsDistance * 1000) / (meterspersec * 1000);

        Log.i("Test", "" + m_TimeThreshold);

        measurements.clear();
    }

    public void getMovementFix(int meterspersec) {
        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.addNmeaListener(this);
        m_LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, m_Intent);
        m_Mode = Mode.MOVEMENT;
        m_DisatnceThreshold = kMpsDistance;
        m_TimeThreshold = kMpsDistance / meterspersec;
        measurements.clear();
    }

    public void stop() {
        m_LocMan.removeNmeaListener(this);
        m_StatusIcon.setColorFilter(kFoundColor);
        Toast.makeText(m_AppContext, "Stopping search", Toast.LENGTH_SHORT).show();
        m_ReadingsCount = 0;
        m_Mode = Mode.NONE;
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        if (m_Mode == Mode.NONE) {
            return;
        }

        if (!nmea.startsWith("$GPGGA")) {
            return;
        }

        String currentTime = new SimpleDateFormat("hh:mm:ss").format(new Date()).toString();

        m_ReadingsCount++;
        m_ReadingsCountLabel.setText("Readings count (as of " + currentTime +"): " + m_ReadingsCount);


        GpggaMeasurement fix = GpggaMeasurement.parseString(nmea);

        if (!fix.isValid()) {
            m_StatusIcon.setColorFilter(kSearchingColor);
            return;
        }

        if (measurements.size() == 0) {
            // we don't have anything to compare to yet, so we can early out.
            addFix(currentTime, fix);
            return;
        }
        GpggaMeasurement previous = measurements.get(measurements.size() - 1);
        
        switch (m_Mode){
            case SINGLE:
            case TIME:
                measurements.add(fix);
                break;
            case DISTANCE:
                if (distance(previous, fix) < m_DisatnceThreshold) {
                    addFix(currentTime, fix);
                }
                break;
            case MAXSPEED:
                if (timeDifference(previous, fix) >= m_TimeThreshold) {
                    addFix(currentTime, fix);
                }

                if (distance(previous, fix) < m_DisatnceThreshold) {
                    addFix(currentTime, fix);
                }
            case MOVEMENT:
                break;
        }

        Log.i("Nmea Monitor", fix.toString());
    }

    private void addFix(String currentTime, GpggaMeasurement fix) {
        measurements.add(fix);
        m_MeasurementCountLabel.setText("Measurements (as of " + currentTime  + "): " + measurements.size());
        m_StatusIcon.setColorFilter(kFoundColor);
    }

    private static double distance(GpggaMeasurement a, GpggaMeasurement b) {
        return Double.MAX_VALUE;
    }

    private static int timeDifference(GpggaMeasurement a, GpggaMeasurement b) {
        int timeDiff = (a.hours - b.hours)*3600+(a.minutes - b.minutes)*60+(a.seconds - b.seconds);
        if (timeDiff < 0) {
            timeDiff *= -1;
        }

        return timeDiff;
    }
}
