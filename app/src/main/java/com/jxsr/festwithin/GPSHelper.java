package com.jxsr.festwithin;

import static com.jxsr.festwithin.MainMapView.bitmapDescriptorFromVector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Random;

public class GPSHelper {

    private Context context;
    // flag for GPS Status
    private boolean isGPSEnabled = false;
    // flag for network status
    private boolean isNetworkEnabled = false;
    private LocationManager locationManager;
    private Location location;
    private double latitude;
    private double longitude;
    private MainMapView associatedMmv;
    private EventsFragment associatedEf;

    @SuppressLint("MissingPermission")
    public GPSHelper(Context context, MainMapView mmv) {
        this.associatedMmv = mmv;
        this.associatedEf = null;
        this.context = context;
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        FWLocationListener ll = new FWLocationListener(associatedMmv);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, ll);
    }

    @SuppressLint("MissingPermission")
    public GPSHelper(Context context, EventsFragment ef) {
        this.associatedMmv = null;
        this.associatedEf = ef;
        this.context = context;
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        FWEvLocationListener ll = new FWEvLocationListener(associatedEf);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, ll);
    }

    @SuppressLint("MissingPermission")
    public Location getMyLocation() {
        List<String> providers = locationManager.getProviders(true);

        Location l = null;
        for (int i = 0; i < providers.size(); i++) {
            l = locationManager.getLastKnownLocation(providers.get(i));
            if (l != null)
                break;
        }
        if (l != null) {
            latitude = l.getLatitude();
            longitude = l.getLongitude();
        }

        return l;
    }

    public boolean isGPSEnabled() {
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return (isGPSEnabled || isNetworkEnabled);
    }


    public double getLatitude() {
        return latitude;
    }


    public double getLongitude() {
        return longitude;
    }
}

class FWLocationListener implements LocationListener {
    MainMapView mmv;

    public FWLocationListener(MainMapView mmv){
        this.mmv = mmv;
    }

    @Override
    public void onLocationChanged(Location loc) {
        notifyMmv();
    }

    public void notifyMmv(){
        mmv.updateLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}

class FWEvLocationListener implements LocationListener {
    EventsFragment ef;

    public FWEvLocationListener(EventsFragment ef){
        this.ef = ef;
    }

    @Override
    public void onLocationChanged(Location loc) {
        notifyMmv();
    }

    public void notifyMmv(){
        ef.updateEvents();
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
