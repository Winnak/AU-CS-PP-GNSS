package dk.au.cs.pervasivepositioninggps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssClock;
import android.location.GnssMeasurementsEvent;
import android.location.GnssMeasurementsEvent.Callback;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.GnssMeasurement;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    LocationManager m_LocMan;
    NmeaMonitor m_Monitor;

    public void onGetFixClicked(View view) {
        //m_LocMan.getAllProviders();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Location", "Could not shit");
            return;
        }

        Intent intent = new Intent(getApplicationContext(), this.getClass());

        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        m_LocMan.requestSingleUpdate(LocationManager.GPS_PROVIDER, pi);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_LocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Initialize", "Failed to acquire the proper permissions");
            //return;
        }
        m_Monitor = new NmeaMonitor();
        if(m_LocMan.addNmeaListener(m_Monitor))
        {
            Log.i("Initialize", "Successfully started listener for NMEA");
        }
        else
        {
            Log.e("Initialize", "Failed started listener for NMEA");
        }
    }

//    @SuppressLint("NewApi")
//    private class GnssCallback extends GnssMeasurementsEvent.Callback {
//        @Override
//        public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs)
//        {
//            Toast.makeText(getApplicationContext(), eventArgs.toString(), Toast.LENGTH_SHORT);
//            Log.i("Location", eventArgs.toString());
//        }
//    }
}
