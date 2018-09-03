package com.example.asus.final_two.asynctasks;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AsyncGeocoder extends AsyncTaskLoader<Bundle> {
    String address;
    LatLng latLng;

    public AsyncGeocoder(Context context,String address,LatLng latLng) {
        super(context);
        this.address=address;
        this.latLng=latLng;
    }

    @Override
    public Bundle loadInBackground() {
        Bundle bundle=new Bundle();
        Geocoder geocoder = new Geocoder(getContext());
        double latitude=0.0,longitude=0.0;
        String state=null;
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(address, 1);

        if(addresses.size() > 0) {
             latitude= addresses.get(0).getLatitude();
             longitude= addresses.get(0).getLongitude();
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Geocoder revgeocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                state=address.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bundle.putString("state",state);
        bundle.putDouble("destLat",latitude);
        bundle.putDouble("destLong",longitude);
        return bundle;
    }
}
