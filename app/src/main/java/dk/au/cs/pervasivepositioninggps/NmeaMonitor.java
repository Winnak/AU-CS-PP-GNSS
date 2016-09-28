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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Erik on 27-09-2016.
 */

public class NmeaMonitor implements NmeaListener {
    private final int kFoundColor = 0xAA00FF00;
    private final int kSearchingColor = 0xAAFFFF00;

    public ArrayList<GpggaMeasurement> measurements = new ArrayList<GpggaMeasurement>();

    private LocationManager m_LocMan;
    private Context m_AppContext;
    private ImageView m_StatusIcon;
    TextView m_MesaurementCountLabel;

    public NmeaMonitor(LocationManager locman, Context appContext, ImageView statusIcon, TextView countLabel) {
        this.m_LocMan = locman;
        this.m_AppContext = appContext;
        this.m_StatusIcon = statusIcon;
        this.m_MesaurementCountLabel = countLabel;

        m_StatusIcon.setColorFilter(kFoundColor);
    }

    public void getFix() {
        Intent intent = new Intent(m_AppContext, this.getClass());

        PendingIntent pi = PendingIntent.getBroadcast(m_AppContext, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        if (ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_AppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("NMEA Monitor", "Could not find shit");
            return;
        }
        m_LocMan.requestSingleUpdate(LocationManager.GPS_PROVIDER, pi);
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        if (!nmea.startsWith("$GPGGA")) {
            return;
        }

        GpggaMeasurement fix = GpggaMeasurement.parseString(nmea);

        if (fix.isValid()) {
            Log.i("Localization", timestamp + ": " + nmea);
            measurements.add(fix);

            m_StatusIcon.setColorFilter(kFoundColor);
            m_MesaurementCountLabel.setText("Measurements: " + measurements.size());
        }
        else
        {
            m_StatusIcon.setColorFilter(kSearchingColor);
        }
    }
}
