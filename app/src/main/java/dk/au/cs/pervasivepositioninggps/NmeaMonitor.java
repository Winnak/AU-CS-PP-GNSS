package dk.au.cs.pervasivepositioninggps;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
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
    private final float kMovementThreshold = 6; // note: distance should be squared.
    private final long kMovementTimeThreshold = 3500000000L; // note: distance should be squared.

    public ArrayList<GpggaMeasurement> measurements = new ArrayList<GpggaMeasurement>();

    private int m_ReadingsCount = 0;
    private LocationManager m_LocMan;

    private Context m_AppContext;
    private ImageView m_StatusIcon;
    private TextView m_MeasurementCountLabel;
    private TextView m_ReadingsCountLabel;
    private PendingIntent m_Intent;
    private double m_DistanceThreshold = 0;

    private float m_lastMoveX = 0;
    private float m_lastMoveY = 0;
    private float m_lastMoveZ = 0;
    private long m_lastMoveStartTimestamp = 0;
    private long m_lastMoveTimestamp = 0;
    private float m_MovementTime = 0;

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
        m_DistanceThreshold = 0;
        m_TimeThreshold  = 0;
        measurements.clear();
    }

    public void getTimeFix(int time) {
        m_Mode = Mode.TIME;
        m_DistanceThreshold = 0;
        m_TimeThreshold = time;
        startListening();
    }

    public void getDistanceFix(int meters) {
        m_Mode = Mode.DISTANCE;
        m_DistanceThreshold = meters;
        m_TimeThreshold = 0;
        startListening();
    }

    public void getMaxSpeedFix(int meterspersec, int meters) {
        m_Mode = Mode.MAXSPEED;
        m_DistanceThreshold = meters;
        m_TimeThreshold =  meters / meterspersec;
        startListening();
    }

    public void getMovementFix(int meterspersec, int meters) {
        m_Mode = Mode.MOVEMENT;
        m_DistanceThreshold = meters;
        m_TimeThreshold = meters / meterspersec;
        startListening();
    }

    private void startListening() {
        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.addNmeaListener(this);
        m_LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, m_Intent);
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
                if (timeDifference(previous, fix) >= m_TimeThreshold) {
                    addFix(currentTime, fix);
                    break;
                }
                break;
            case DISTANCE:
                if (distance(previous, fix) < m_DistanceThreshold) {
                    addFix(currentTime, fix);
                    break;
                }
                break;
            case MAXSPEED:
                if (timeDifference(previous, fix) >= m_TimeThreshold) {
                    addFix(currentTime, fix);
                    break;
                }

                if (distance(previous, fix) < m_DistanceThreshold) {
                    addFix(currentTime, fix);
                    break;
                }
            case MOVEMENT:
                if (m_MovementTime >= m_TimeThreshold) {
                    addFix(currentTime, fix);
                    m_MovementTime = 0;
                    break;
                }

                if (distance(previous, fix) < m_DistanceThreshold) {
                    addFix(currentTime, fix);
                    break;
                }
                break;
        }

        Log.i("Nmea Monitor", fix.toString());
    }

    private void addFix(String currentTime, GpggaMeasurement fix) {
        measurements.add(fix);
        m_MeasurementCountLabel.setText("Measurements (as of " + currentTime  + "): " + measurements.size());
        m_StatusIcon.setColorFilter(kFoundColor);
    }

    public void reportMovement(SensorEvent event) {
        final float diffMovement = (m_lastMoveX * m_lastMoveX) - (event.values[0] * event.values[0])
                + (m_lastMoveY * m_lastMoveY) - (event.values[1] * event.values[1])
                + (m_lastMoveZ * m_lastMoveZ) - (event.values[2] * event.values[2]);

        if (diffMovement > kMovementThreshold) {
            if (event.timestamp - m_lastMoveTimestamp > kMovementTimeThreshold) {
                // Started moving after a break.
                m_MovementTime += m_lastMoveStartTimestamp - m_lastMoveTimestamp;
                m_lastMoveStartTimestamp = event.timestamp;
            }
            m_lastMoveTimestamp = event.timestamp;
        }

        m_lastMoveX = event.values[0];
        m_lastMoveY = event.values[1];
        m_lastMoveZ = event.values[2];
    }

    private static double distance(GpggaMeasurement a, GpggaMeasurement b) {
        // TODO: functionality here.
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
