""" Python 3 program to calculate the interpolated positions """

#pylint: disable=C
import math
import xml.etree.ElementTree as ET


METERSPERSEC = 2.0
NAMESPACE = "{http://www.opengis.net/kml/2.2}"
TRUE_ROOT = ET.fromstring("""<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2" xmlns:gx="http://www.google.com/kml/ext/2.2" xmlns:kml="http://www.opengis.net/kml/2.2" xmlns:atom="http://www.w3.org/2005/Atom">
<Document>
	<name>TEST route.kml</name>
	<Style id="s_ylw-pushpin_hl">
		<IconStyle>
			<scale>1.3</scale>
			<Icon>
				<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>
			</Icon>
			<hotSpot x="20" y="2" xunits="pixels" yunits="pixels"/>
		</IconStyle>
		<LineStyle>
			<color>ffff0000</color>
		</LineStyle>
	</Style>
	<Style id="s_ylw-pushpin">
		<IconStyle>
			<scale>1.1</scale>
			<Icon>
				<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>
			</Icon>
			<hotSpot x="20" y="2" xunits="pixels" yunits="pixels"/>
		</IconStyle>
		<LineStyle>
			<color>ffff0000</color>
		</LineStyle>
	</Style>
	<StyleMap id="m_ylw-pushpin">
		<Pair>
			<key>normal</key>
			<styleUrl>#s_ylw-pushpin</styleUrl>
		</Pair>
		<Pair>
			<key>highlight</key>
			<styleUrl>#s_ylw-pushpin_hl</styleUrl>
		</Pair>
	</StyleMap>
	<Placemark>
		<name>TEST router</name>
		<styleUrl>#m_ylw-pushpin</styleUrl>
		<LineString>
			<tessellate>1</tessellate>
			<coordinates>
				10.18863528395544,56.1718167368093,0 10.18843868032121,56.17161397146886,0 10.18825312495371,56.17161690159397,0 10.18785876852806,56.17067413379924,0 10.18867122638363,56.17054152073421,0 10.1884718758678,56.17008266958371,0 10.18882969229213,56.17003002550649,0 10.18865430467,56.16955002244181,0 10.18885826270572,56.16939494824196,0 10.18886926162166,56.16931732635131,0 10.18934629802868,56.16928490818662,0 10.18953700018306,56.16972535344797,0 10.18984427564552,56.17040330830756,0 10.1903055433096,56.17146720503291,0 10.18862542434161,56.17174364044325,0 10.18867376461784,56.17181347215714,0
			</coordinates>
		</LineString>
	</Placemark>
</Document>
</kml>""")

def main():
    """ Main entry point of the program """
    # parsing the TRUE_ROOT into a list of placemarks
    t_coordinates_tag = TRUE_ROOT.find(xpath("Document", "Placemark", "LineString", "coordinates"))
    t_coordinates_text = t_coordinates_tag.text.strip()
    t_route = list()
    for coordset in t_coordinates_text.split(" "):
        coordinate = list()
        for coord in coordset.split(","):
            coordinate.append(float(coord))
        t_route.append(coordinate)

    for point in t_route:
        print(point)

    
    for i in range(len(t_route) - 1):
        a = t_route[i]
        b = t_route[i+1]
        magnitude = distance(a, b)
        print(a)
        for p in range(int(magnitude / METERSPERSEC) - 1):
            d = list()
            d.append(a[0] + (p + 1) * METERSPERSEC * (b[0] - a[0]) / magnitude)
            d.append(a[1] + (p + 1) * METERSPERSEC * (b[1] - a[1]) / magnitude)
            d.append(a[2] + (p + 1) * METERSPERSEC * (b[2] - a[2]) / magnitude)
            print(d)


def xpath(*paths):
    """ Creates an xpath string with namespace """
    _xpath = ""
    for path in paths:
        _xpath += NAMESPACE
        _xpath += path
        _xpath += "/"
    return _xpath[:-1]

def distance(a, b):
    a_lat = math.radians(a[0])
    b_lat = math.radians(b[0])
    a_lon = math.radians(a[1])
    b_lon = math.radians(b[1])
    delta_lat = b_lat - a_lat
    delta_lon = b_lon - a_lon
    haversine_A = (math.sin(delta_lat * 0.5) * math.sin(delta_lat * 0.5) +
                   math.sin(delta_lon * 0.5) * math.sin(delta_lon * 0.5) *
                   math.cos(a_lat) * math.cos(b_lat))
    haversine_B = 2 * math.atan2(math.sqrt(haversine_A), math.sqrt(1 - haversine_A))
    return 6378137 * haversine_B

if __name__ == '__main__':
    main()
