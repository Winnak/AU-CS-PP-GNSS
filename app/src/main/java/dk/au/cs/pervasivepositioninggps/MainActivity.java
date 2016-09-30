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
    EditText m_ValueField1;
    EditText m_ValueField2;
    Button m_StartButton;
    Button m_FirstFixButton;
    ImageView m_StatusIcon;
    TextView m_MesaurementCountLabel;
    TextView m_ReadingsCountLabel;

    public void onStartBtnClicked(View view)
    {
        int value1 = -1;
        try {
            value1 = Integer.parseInt(m_ValueField1.getText().toString());
        } catch (Exception ex) { }

        int value2 = -1;
        try {
            value2 = Integer.parseInt(m_ValueField2.getText().toString());
        } catch (Exception ex) { }

        if (value1 == -1) {
            Log.e("Parsing", "Invalid input " + m_ValueField1.getText().toString());
            Toast.makeText(getApplicationContext(), "Please enter a valid value", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (m_RadioGroup.getCheckedRadioButtonId())
        {
            case R.id.triggerTime:
                m_Monitor.getTimeFix(value1);
                break;
            case R.id.triggerDistance:
                m_Monitor.getDistanceFix(value1);
                break;
            case R.id.triggerMaxSpeed:
                if (value2 == -1) {
                    Log.e("Parsing", "Invalid input " + m_ValueField2.getText().toString());
                    Toast.makeText(getApplicationContext(), "Please enter a valid value", Toast.LENGTH_SHORT).show();
                    return;
                }
                m_Monitor.getMaxSpeedFix(value1, value2);
                break;
            case R.id.triggerMovement:
                if (value2 == -1) {
                    Log.e("Parsing", "Invalid input " + m_ValueField2.getText().toString());
                    Toast.makeText(getApplicationContext(), "Please enter a valid value", Toast.LENGTH_SHORT).show();
                    return;
                }
                m_Monitor.getMovementFix(value1, value2);
                break;
            default:
                Toast.makeText(getApplicationContext(), "Error: No trigger type was selected", Toast.LENGTH_SHORT).show();
                return;
        }

        setWidgetEnables(false);
    }

    public void onSaveBtnClicked(View view)
    {
        // Write file

        // If successful run the following, otherwise; return before this.
        Toast.makeText(getApplicationContext(), "File saved", Toast.LENGTH_SHORT).show();
        setWidgetEnables(true);
        m_Monitor.measurements.clear();
        m_MesaurementCountLabel.setText("Measurements: " + m_Monitor.measurements.size());
    }

    public void onTestClicked(View view)
    {
        m_Monitor.getSingleFix();
        setWidgetEnables(false);
    }

    public void onStopBtnClicked(View view)
    {
        m_Monitor.stop();
        setWidgetEnables(true);
    }

    private void setWidgetEnables(boolean enabled) {
        m_Started = !enabled;
        m_StartButton.setEnabled(enabled);
        setEnableRadioButtons(enabled);
        m_ValueField1.setEnabled(enabled);
        m_ValueField2.setEnabled(enabled);
        m_FirstFixButton.setEnabled(enabled);
        if (enabled) {
            m_RadioGroup.clearCheck();
        }
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
            return;
        }

        m_RadioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        m_ValueField1 = (EditText) findViewById(R.id.inputField1);
        m_ValueField2 = (EditText) findViewById(R.id.inputField2);
        m_StartButton = (Button) findViewById(R.id.buttonStart);
        m_FirstFixButton = (Button) findViewById(R.id.buttonTest);
        m_StatusIcon = (ImageView) findViewById(R.id.statusIcon);
        m_MesaurementCountLabel = (TextView)findViewById(R.id.labelMeasurementCount);
        m_ReadingsCountLabel = (TextView)findViewById(R.id.labelReadingsCount);

        m_Monitor = new NmeaMonitor(m_LocMan, getApplicationContext(), m_StatusIcon, m_MesaurementCountLabel, m_ReadingsCountLabel);

        m_RadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.triggerTime:
                        m_ValueField1.setHint("Time in seconds");
                        m_ValueField2.setHint(" - ");
                        m_ValueField1.setEnabled(true);
                        m_ValueField2.setEnabled(false);
                        break;
                    case R.id.triggerDistance:
                        m_ValueField1.setHint("Distance in meters");
                        m_ValueField2.setHint(" - ");
                        m_ValueField1.setEnabled(true);
                        m_ValueField2.setEnabled(false);
                        break;
                    case R.id.triggerMaxSpeed:
                        m_ValueField1.setHint("Speed in meters per second");
                        m_ValueField2.setHint("Distance in meters");
                        m_ValueField1.setEnabled(true);
                        m_ValueField2.setEnabled(true);
                        break;
                    case R.id.triggerMovement:
                        m_ValueField1.setHint("Speed in meters per second");
                        m_ValueField2.setHint("Distance in meters");
                        m_ValueField1.setEnabled(true);
                        m_ValueField2.setEnabled(true);
                        break;
                }
            }
        });
    }
}