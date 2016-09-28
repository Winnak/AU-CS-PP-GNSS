package dk.au.cs.pervasivepositioninggps;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    LocationManager m_LocMan;
    NmeaMonitor m_Monitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_LocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Initialize", "Failed to acquire the proper permissions");
            //return; // we (currently) want the stack trace if this fails.
        }
        m_Monitor = new NmeaMonitor(m_LocMan, getApplicationContext());
        if(m_LocMan.addNmeaListener(m_Monitor))
        {
            Log.i("Initialize", "Successfully started listener for NMEA");
        }
        else
        {
            Log.e("Initialize", "Failed started listener for NMEA");
        }
    }
}
