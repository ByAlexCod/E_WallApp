package com.andit.e_wall;

import android.graphics.Point;

import com.andit.e_wall.data_model.Coord;
import com.google.android.gms.maps.model.LatLng;

public class MapHelper {

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public static Coord TranslatePlan(double currentCompass, LatLng currentLocation, LatLng pointLocation){
        if(currentCompass > 180) {
            currentCompass = -1*(360-currentCompass);
        }
        double xm = distance(currentLocation.latitude, pointLocation.latitude, currentLocation.longitude, currentLocation.longitude, 1, 1);
        double ym = distance(currentLocation.latitude, currentLocation.latitude, currentLocation.longitude, pointLocation.longitude, 1, 1);

        double xp = xm*(Math.cos(Math.toRadians(currentCompass))) - ym*(Math.sin(Math.toRadians(currentCompass)));
        double yp = ym*(Math.cos(Math.toRadians(currentCompass))) + xm*(Math.sin(Math.toRadians(currentCompass)));

        return new Coord(xp, yp);

    }
}

