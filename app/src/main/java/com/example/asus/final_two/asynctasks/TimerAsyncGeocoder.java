package com.example.asus.final_two.asynctasks;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class TimerAsyncGeocoder extends AsyncTaskLoader<Bundle> {
    String start,end;
    Double lat,lon;
    public TimerAsyncGeocoder(Context context, Bundle bundle) {
        super(context);
        this.start=bundle.getString("start");
        this.end=bundle.getString("end");
        this.lat=bundle.getDouble("lat");
        this.lon=bundle.getDouble("long");
    }

    @Override
    public Bundle loadInBackground() {
        Bundle bundle=new Bundle();
        Geocoder geocoder = new Geocoder(getContext());
        String state=null;
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(start, 1);

            if(addresses.size() > 0) {
                bundle.putDouble("startLat",addresses.get(0).getLatitude());
                bundle.putDouble("startLong",addresses.get(0).getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            addresses = geocoder.getFromLocationName(end, 1);

            if(addresses.size() > 0) {
                bundle.putDouble("destLat",addresses.get(0).getLatitude());
                bundle.putDouble("destLong",addresses.get(0).getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Geocoder revgeocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(
                    lat, lon, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                state=address.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bundle.putString("state",state);
        return bundle;
    }
}