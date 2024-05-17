package com.jxsr.festwithin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jxsr.festwithin.models.Event;
import com.jxsr.festwithin.models.EventPricing;
import com.jxsr.festwithin.models.User;
import com.jxsr.festwithin.util.MyOnMapReadyCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainMapView extends Fragment {

    GPSHelper gps;
    public GoogleMap map;
    FloatingActionButton fabRefresh;
    int updateTries = 0;
    DBHelper db;
    User user;

    private OnMapReadyCallback callback = new MyOnMapReadyCallback(this) {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            map = googleMap;
            db = new DBHelper(requireActivity().getApplicationContext());
            gps = new GPSHelper(requireActivity().getApplicationContext(), this.mmv);
            user = db.getUserFromID(PreferenceData.getLoggedInUserId(requireActivity().getApplicationContext()));
            updateLocation();
            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    LinearLayout info = new LinearLayout(getActivity().getApplicationContext());
                    info.setOrientation(LinearLayout.VERTICAL);
                    info.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.cardshape, null));

                    TextView title = new TextView(getActivity().getApplicationContext());
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    if(!marker.getSnippet().startsWith("00UMRK")){
                        TextView eventId = new TextView(getActivity().getApplicationContext());
                        eventId.setTextColor(Color.LTGRAY);
                        eventId.setTextSize(5);
                        eventId.setGravity(Gravity.LEFT);
                        eventId.setText(marker.getSnippet().substring(0, 5));

                        TextView location = new TextView(getActivity().getApplicationContext());
                        Geocoder geo = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
                        Address address = null;
                        try {
                            address = geo.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1).get(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String strLocation = "";
                        if(address != null){
                            strLocation = address.getLocality() + ", " + address.getCountryName();
                        }else{
                            strLocation = "Unknown Location";
                        }
                        location.setTextColor(Color.DKGRAY);
                        location.setText(getString(R.string.infowindow_strutil_in) + " " + strLocation);


                        TextView price = new TextView(getActivity().getApplicationContext());
                        if(marker.getSnippet() != null){
                            price.setTextColor(Color.GRAY);
                            price.setText("\n" + getString(R.string.infowindow_strutil_pricestarts) + " " + marker.getSnippet().substring(5, marker.getSnippet().length() - 1));
                        }


                        TextView status = new TextView(getActivity().getApplicationContext());
                        if(marker.getSnippet() != null) {
                            if(marker.getSnippet().charAt(marker.getSnippet().length()-1) == 'A'){
                                status.setTextColor(getActivity().getResources().getColor(R.color.available, getActivity().getTheme()));
                                status.setText(R.string.status_available);
                            }else if(marker.getSnippet().charAt(marker.getSnippet().length()-1) == 'F'){
                                status.setTextColor(getActivity().getResources().getColor(R.color.fullbooked, getActivity().getTheme()));
                                status.setText(R.string.status_full);
                            }else{
                                status.setTextColor(getActivity().getResources().getColor(R.color.unknown, getActivity().getTheme()));
                                status.setText(R.string.status_unknown);
                            }
                        }
                        status.setGravity(Gravity.CENTER);

                        Button viewDetailsBtn = new Button(getActivity().getApplicationContext());
                        viewDetailsBtn.setText(R.string.btn_view_details);
                        viewDetailsBtn.setBackgroundColor(Color.BLACK);
                        viewDetailsBtn.setTypeface(null, Typeface.BOLD);
                        viewDetailsBtn.setTextColor(Color.WHITE);
                        viewDetailsBtn.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                        viewDetailsBtn.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

                        info.addView(title);
                        info.addView(eventId);
                        info.addView(location);
                        info.addView(price);
                        info.addView(status);
                        info.addView(viewDetailsBtn);
                    }else{
                        title.setText(marker.getTitle());
                        TextView email = new TextView(getActivity().getApplicationContext());
                        email.setTextColor(Color.GRAY);
                        email.setText(marker.getSnippet().substring(6));

                        info.addView(title);
                        info.addView(email);
                    }

                    return info;
                }
            });

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    if(!marker.getSnippet().startsWith("00UMRK")){
                        Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                        String title = marker.getTitle();
                        String eventID = marker.getSnippet().substring(0,5);
                        intent.putExtra("id", eventID);
                        intent.putExtra("markertitle", title);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        startActivity(intent);
                    }

                }
            });
        }
    };

    public void updateLocation(){
        map.clear();
        if(gps != null && gps.getMyLocation() != null){
            if(getActivity() != null){
                LatLng curLoc = new LatLng(gps.getLatitude(), gps.getLongitude());
                map.addMarker(new MarkerOptions().position(curLoc).title(user.getUserDisplayName() + " (You)").snippet("00UMRK" + user.getUserEmail()).icon(bitmapDescriptorFromVector(requireActivity(), R.drawable.ic_fw_userindicator)));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 16.0f));
                updateNearbyMarkers(getActivity());
            }
        }else{
            Toast.makeText(getActivity().getApplicationContext(), "Unable to retrieve current location! ("+updateTries+")", Toast.LENGTH_LONG).show();
        }
        updateTries++;
    }

    public void updateNearbyMarkers(FragmentActivity fa){
        try{
            ArrayList<Event> allEventsInRange;
            allEventsInRange = db.getAllEvents();
            allEventsInRange.removeIf(x -> !isInRange(x));

            for (Event e: allEventsInRange) {
                map.addMarker(new MarkerOptions().position(new LatLng(e.getEventLatitude(),e.getEventLongitude())).title(e.getEventTitle()).icon(bitmapDescriptorFromVector(fa, R.drawable.ic_fw_eventpoint)).snippet(e.getEventID() + eventLowestPriceCurrency(e) + " " + eventLowestPrice(e) + (isEventAvailable(e) ? "A" : "F")));
            }
        }catch(Exception ignored){
        }
    }

    public String eventLowestPriceCurrency(Event event){
        ArrayList<EventPricing> allPricing = db.getEventPricingsFromID(event.getEventID());
        double lowest = -10;
        String cur = "";
        for(EventPricing ep : allPricing){
            if(lowest == -10){
                lowest = ep.getEPClassPrice();
                cur = ep.getEPClassCurrency();
            }else{
                if(ep.getEPClassPrice() < lowest){
                    lowest = ep.getEPClassPrice();
                    cur = ep.getEPClassCurrency();
                }
            }
        }
        return cur;
    }

    public double eventLowestPrice(Event event){
        ArrayList<EventPricing> allPricing = db.getEventPricingsFromID(event.getEventID());
        double lowest = -10;
        for(EventPricing ep : allPricing){
            if(lowest == -10){
                lowest = ep.getEPClassPrice();
            }else{
                if(ep.getEPClassPrice() < lowest){
                    lowest = ep.getEPClassPrice();
                }
            }
        }
        return lowest;
    }

    public boolean isEventAvailable(Event event){
        ArrayList<EventPricing> allPricing = db.getEventPricingsFromID(event.getEventID());
        for(EventPricing ep : allPricing){
            if(ep.getEPClassStock() > 0) return true;
        }

        return false;
    }

    public boolean isInRange(Event event){
        return ((event.getEventLatitude() - gps.getLatitude()) <= shiftLatFromKm(PreferenceData.getMaxRangeKM(requireActivity().getApplicationContext()))
        && (event.getEventLongitude() - gps.getLongitude()) <= shiftLongFromKm(PreferenceData.getMaxRangeKM(requireActivity().getApplicationContext()))
        );
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_map_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        fabRefresh = view.findViewById(R.id.fabRefreshMap);
        fabRefresh.setOnClickListener(x->{
            updateLocation();
        });
    }

    public static double shiftLatFromKm(int kmDistance){
        return (0.00902) * (double)kmDistance;
    }

    public static double shiftLongFromKm(int kmDistance){
        return (0.00898) * (double)kmDistance;
    }
}