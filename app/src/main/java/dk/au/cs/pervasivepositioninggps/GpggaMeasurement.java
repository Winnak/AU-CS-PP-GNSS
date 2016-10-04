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
    public int hours = -1;
    public int minutes = -1;
    public int seconds = -1;
    public int statusCode;

    public static GpggaMeasurement parseString(String nmea)
    {
        String[] array = nmea.split(",");
        GpggaMeasurement newGpgga = new GpggaMeasurement();

        try
        {
            String time = array[1];
            newGpgga.hours = Integer.parseInt(time.substring(0,2));
            newGpgga.minutes = Integer.parseInt(time.substring(2,4));
            newGpgga.seconds = Integer.parseInt(time.substring(4,6));

            // Muligvis en faktor 10 fejl
            int latDeg = Integer.parseInt(array[2].substring(0, 2));
            double latMin = Double.parseDouble(array[2].substring(2, array[2].length()));
            newGpgga.latitude = latDeg + latMin / 60.0;

            // Muligvis en faktor 10 fejl
            int longDeg = Integer.parseInt(array[4].substring(0, 3));
            double longMin = Double.parseDouble(array[4].substring(3, array[4].length()));
            newGpgga.longitude = longDeg + longMin / 60.0;

            newGpgga.statusCode = Integer.parseInt(array[6]);
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
        sb.append("<Placemark>");
        sb.append("\n\t\t<TimeStamp><when>");
        sb.append(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        sb.append("T");
        sb.append(hours);
        sb.append(":");
        sb.append(minutes);
        sb.append(":");
        sb.append(seconds);
        sb.append("Z</when></TimeStamp>\n\t\t<Point>\n\t\t\t<coordinates>\n\t\t\t\t");
        sb.append(longitude);
        sb.append(",");
        sb.append(latitude);
        sb.append(",0");
        sb.append("\n\t\t\t</coordinates>\n\t\t</Point>\n\t</Placemark>");
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.statusCode + " - " + hours + ":" + minutes + ":" + seconds;
    }
}
