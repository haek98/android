package com.example.asus.final_two.helperclasses;

import android.location.Location;
import android.util.Log;

public class MapMarker {
    public double distC(double lat1, double long1, double lat2, double long2)
    {
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(long1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(long2);
        double dist=loc1.distanceTo(loc2);
        Log.e("TAG","Distance is "+String.valueOf(dist));
        return dist;
    }
}
