package dk.au.cs.pervasivepositioninggps;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by peppe_000 on 27-09-2016.
 */

public class GpggaMeasurement {

    public double latitude;
    public double longitude;
    public String time;
    public int statusCode;

    public static GpggaMeasurement parseString(String nmea)
    {
        String[] array = nmea.split(",");
        GpggaMeasurement newGpgga = new GpggaMeasurement();

        try
        {
            newGpgga.latitude = Double.parseDouble(array[2]);
            newGpgga.longitude = Double.parseDouble(array[4]);
            newGpgga.statusCode = Integer.parseInt(array[6]);
            newGpgga.time = array[1];
        }
        catch (Exception ex)
        {
            //Log.e("Nmea Monitor", ex.toString());
        }

        return newGpgga;
    }

    public boolean isValid()
    {
        return statusCode != 0;
    }

    public String toKML()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<Placemark><TimeStamp><when>");
        sb.append(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        sb.append("T");
        sb.append(time.substring(0,2));
        sb.append(":");
        sb.append(time.substring(2,4));
        sb.append(":");
        sb.append(time.substring(4,6));
        sb.append("Z</when></TimeStamp><Point><coordinates>");
        sb.append(longitude);
        sb.append(",");
        sb.append(latitude);
        sb.append("</coordinates></Point></Placemark>");
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.statusCode + " - " + time;
    }
}
