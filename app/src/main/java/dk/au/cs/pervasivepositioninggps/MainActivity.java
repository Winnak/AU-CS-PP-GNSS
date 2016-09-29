package dk.au.cs.pervasivepositioninggps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.security.AccessControlException;

public class MainActivity extends AppCompatActivity {

    boolean m_Started = false;

    LocationManager m_LocMan;
    NmeaMonitor m_Monitor;

    RadioGroup m_RadioGroup;
    EditText m_ValueField;
    Button m_StartButton;
    ImageView m_StatusIcon;
    TextView m_MesaurementCountLabel;

    public void onStartBtnClicked(View view)
    {
        switch (m_RadioGroup.getCheckedRadioButtonId())
        {
            case R.id.trigger1:
                break;
            case R.id.trigger2:
                break;
            case R.id.trigger3:
                break;
            case R.id.trigger4:
                break;
            default:
                Toast.makeText(getApplicationContext(), "Error: No trigger type was selected", Toast.LENGTH_SHORT).show();
                return;
        }
        m_Started = true;
        setEnableRadioButtons(false);
        m_StartButton.setEnabled(false);
    }

    public void onSaveBtnClicked(View view)
    {
        m_Started = false;
        Toast.makeText(getApplicationContext(), "File saved", Toast.LENGTH_SHORT).show();
        m_StartButton.setEnabled(true);
        setEnableRadioButtons(true);
        m_RadioGroup.clearCheck();

        m_MesaurementCountLabel.setText("Measurements: " + m_Monitor.measurements.size());
    }

    public void onTestClicked(View view)
    {
        m_Monitor.getFix();
    }

    private void setEnableRadioButtons(boolean enable)
    {
        for(int i = 0; i < m_RadioGroup.getChildCount(); i++){
            m_RadioGroup.getChildAt(i).setEnabled(enable);
        }
        m_RadioGroup.setEnabled(enable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_LocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Initialize", "Failed to acquire the proper permissions. " + Log.getStackTraceString(new AccessControlException("Failed to aquire proper permissions")));
            return; // we (currently) want the stack trace if this fails.
        }

        m_RadioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        m_ValueField = (EditText) findViewById(R.id.inputField);
        m_StartButton = (Button) findViewById(R.id.buttonStart);
        m_StatusIcon = (ImageView) findViewById(R.id.statusIcon);
        m_MesaurementCountLabel = (TextView)findViewById(R.id.labelMeasurementCount);

        m_Monitor = new NmeaMonitor(m_LocMan, getApplicationContext(), m_StatusIcon, m_MesaurementCountLabel);
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