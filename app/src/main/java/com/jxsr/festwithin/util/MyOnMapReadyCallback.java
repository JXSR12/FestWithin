package com.jxsr.festwithin.util;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.jxsr.festwithin.MainMapView;

public abstract class MyOnMapReadyCallback implements OnMapReadyCallback {
    public MainMapView mmv;

    public MyOnMapReadyCallback(MainMapView mmv){
        this.mmv = mmv;
    }

    public abstract void onMapReady(@NonNull GoogleMap googleMap);
}
