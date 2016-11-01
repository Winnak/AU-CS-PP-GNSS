""" Python 3 program to calculate the cumulative error """

#pylint: disable=C

import sys
import math
import datetime
import os.path
import xml.etree.ElementTree as ET

METERSPERSEC = 2.0
# paused 3*60 = 180 -> 5*60 + 4 = 304
# paused 9*60 + 40 = 580 -> 11*60 + 44 = 704
T_TIME = [0,
          13,
          13 + 15,
          28 + 100,
          128 + 171,
          299 + 43,
          342 + 18,
          360 + 42,
          402 + 40,
          442 + 12,
          454 + 25,
          479 + 42,
          521 + 178,
          787,
          868,
          876]

class Placemark(object):
    """docstring for Placemark."""
    _NAMESPACE = "{http://www.opengis.net/kml/2.2}"

    def __init__(self, lat, lon, time):
        super(Placemark, self).__init__()
        self.latitude = lat
        self.longitude = lon
        self.time = time

    @staticmethod
    def parse_file(filepath):
        root = ET.parse(filepath).getroot()
        tags = root.findall(Placemark.xpath("Document", "Placemark"))
        placemarks = list()
        init_time = Placemark.parse_time(root.find(Placemark.xpath("Document", "Placemark", "TimeStamp", "when")).text)
        for tag in tags:
            coords = tag.find(Placemark.xpath("Point", "coordinates")).text.strip().split(",")[:-1]
            time = tag.find(Placemark.xpath("TimeStamp", "when")).text
            placemarks.append(Placemark(lat=float(coords[0]),
                                        lon=float(coords[1]),
                                        time=(Placemark.parse_time(time) - init_time)))
        return placemarks

    @staticmethod
    def parse_time(timestring):
        """ Converts kml time string to a datetime """ #%I
        return datetime.datetime.strptime(timestring, "%Y-%m-%dT%H:%M:%SZ")

    @staticmethod
    def xpath(*paths):
        """ Creates an xpath string with namespace """
        _xpath = ""
        for path in paths:
            _xpath += Placemark._NAMESPACE
            _xpath += path
            _xpath += "/"
        return _xpath[:-1]

    @staticmethod
    def distance(a, b):
        """ Calculates the distance between two placemarks """
        a_lat = math.radians(a.latitude)
        b_lat = math.radians(b.latitude)
        a_lon = math.radians(a.longitude)
        b_lon = math.radians(b.longitude)
        delta_lat = b_lat - a_lat
        delta_lon = b_lon - a_lon
        haversine_A = (math.sin(delta_lat * 0.5) * math.sin(delta_lat * 0.5) +
                       math.sin(delta_lon * 0.5) * math.sin(delta_lon * 0.5) *
                       math.cos(a_lat) * math.cos(b_lat))
        haversine_B = 2 * math.atan2(math.sqrt(haversine_A), math.sqrt(1 - haversine_A))
        return 6378137 * haversine_B

def truth_distance(time, time2):
    if time >= 180 and time <= 304: #4
        if time2 >= 180 and time <= 304:
            return 0
        else:
            time = 304
    elif time >= 580 and time <= 704: #12
        if time2 >= 580 and time <= 704:
            return 0
        else:
            time = 704
    return (time2 - time) * METERSPERSEC

def truth_point(time, sec, places, truthTimeList):
    if time >= 180 and time <= 304:
        return places[4]
    if time >= 580 and time <= 704:
        return places[12]
    timeoffset = time + sec
    for aTruth in range(1, len(truthTimeList)):
        if timeoffset < truthTimeList[aTruth]:
            if aTruth == 4 or aTruth == 12:
                extraTime = 124
            else:
                extraTime = 0
            return create_placemark(places[aTruth - 1],
                                    places[aTruth],
                                    timeoffset,
                                    ((timeoffset - truthTimeList[aTruth - 1]) /
                                     (truthTimeList[aTruth] - truthTimeList[aTruth - 1] - extraTime)))

    return create_placemark(places[14],
                            places[15],
                            timeoffset,
                            (timeoffset - truthTimeList[14]) / (truthTimeList[15] - truthTimeList[14]))

def create_placemark(place, place2, time, dif):
    lat = place.latitude + (place.latitude - place2.latitude) * dif
    lon = place.longitude + (place.longitude - place2.longitude) * dif
    return Placemark(lat, lon, time)

def main(arg):
    """ Main entry point of the program, takes 1 argument: the file path to the tested pos """
    errors = list()
    placemarks = Placemark.parse_file(filepath=arg)

    print(placemarks[0].time.seconds)
    print(placemarks[len(placemarks) - 1].time.seconds)
    print(T_TIME[0])
    print(T_TIME[len(T_TIME) - 1])

    for xPlace in range(0, len(placemarks) - 1):
        speed = 2.0
        dist = truth_distance(placemarks[xPlace].time.seconds, placemarks[xPlace + 1].time.seconds)
        fakePoints = int(dist / speed)
        if dist / speed == fakePoints and dist != 0:
            fakePoints = fakePoints - 1
        for jError in range(0, fakePoints):
            truth = truth_point(placemarks[xPlace].time.seconds, jError, placemarks, T_TIME)
            errors.append(Placemark.distance(placemarks[xPlace], truth))
    errors.sort()
    f = open(arg[:-4] + ".error.csv", "w")
    for error in errors:
        #print(error)
        f.write(str(error).split(".")[0] + "," + str(error).split(".")[1])
        f.write("\n")

if __name__ == '__main__':
    if len(sys.argv) is 2:
        if os.path.isfile(sys.argv[1]):
            main(sys.argv[1])
        else:
            print("Invalid filepath")
    else:
        print("1 argument required")
