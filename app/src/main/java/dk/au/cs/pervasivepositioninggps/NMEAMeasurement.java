package dk.au.cs.pervasivepositioninggps;

/**
 * Created by peppe_000 on 27-09-2016.
 */

public class NMEAMeasurement {

    public double latitude;
    public double longitude;
    public String time;
    public boolean isValid;

    NMEAMeasurement parseString(String nmea)
    {
        String[] array = nmea.split(",");
        NMEAMeasurement nmeaNew = new NMEAMeasurement();

        nmeaNew.latitude = Double.parseDouble(array[2]);
        nmeaNew.longitude = Double.parseDouble(array[4]);
        nmeaNew.isValid = Integer.parseInt(array[6]) != 0;
        nmeaNew.time = array[1];

        return nmeaNew;
    }

    boolean isValid()
    {
        return isValid;
    }

    String toKML()
    {
        return "<Placemark><TimeStamp><when>2016-09-27T" + time.substring(0,2) + ":" + time.substring(2,4) + ":" + time.substring(4,6) + "Z</when></TimeStamp>" +
                "</description><Point><coordinates>" + longitude + "," + latitude + "</coordinates></Point></Placemark>";
    }

}
