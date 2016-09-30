package dk.au.cs.pervasivepositioninggps;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    public ArrayList<GpggaMeasurement> measurements = new ArrayList<GpggaMeasurement>();
    private int m_ReadingsCount = 0;

    private LocationManager m_LocMan;
    private Context m_AppContext;
    private ImageView m_StatusIcon;
    private TextView m_MeasurementCountLabel;
    private TextView m_ReadingsCountLabel;
    private PendingIntent m_Intent;

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
        measurements.clear();
    }

    public void getDistanceFix(int meters) {
        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.addNmeaListener(this);
        m_LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.MAX_VALUE, meters, m_Intent);
        m_Mode = Mode.DISTANCE;
        measurements.clear();
    }

    public void getMaxSpeedFix(int meterspersec) {
        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.addNmeaListener(this);
        m_LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.MAX_VALUE, 0, m_Intent);
        m_Mode = Mode.MAXSPEED;
        measurements.clear();
    }

    public void getMovementFix(int meterspersec) {
        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.addNmeaListener(this);
        m_LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.MAX_VALUE, 0, m_Intent);
        m_Mode = Mode.MOVEMENT;
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
        if (!nmea.startsWith("$GPGGA")) {
            return;
        }

        String currentTime = new SimpleDateFormat("hh:mm:ss").format(new Date()).toString();

        m_ReadingsCount++;
        m_ReadingsCountLabel.setText("Readings count (as of " + currentTime +"): " + m_ReadingsCount);

        switch (m_Mode){
            case SINGLE:
            case TIME:
            case DISTANCE:
            case MAXSPEED:
            case MOVEMENT:
                break;
            default:
                return;
        }

        GpggaMeasurement fix = GpggaMeasurement.parseString(nmea);

        if (fix.isValid()) {
            Log.i("Localization", timestamp + ": " + nmea);
            measurements.add(fix);

            m_StatusIcon.setColorFilter(kFoundColor);
            m_MeasurementCountLabel.setText("Measurements (as of " + currentTime  + "): " + measurements.size());
            Toast.makeText(m_AppContext, "Found fix. " + fix.toString(), Toast.LENGTH_SHORT).show();
            Log.i("Nmea Monitor", fix.toString());
        } else {
            m_StatusIcon.setColorFilter(kSearchingColor);
            //Log.v("Localization", "Package received" + fix.toString());
        }
    }
}
