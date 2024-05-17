package com.jxsr.festwithin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jxsr.festwithin.models.Event;
import com.jxsr.festwithin.models.EventPricing;
import com.jxsr.festwithin.models.Ticket;
import com.jxsr.festwithin.models.User;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class EventsFragment extends Fragment {
    LinearLayout eventsHolder;
    DBHelper db;
    User user;
    GPSHelper gps;
    SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy");
    TextView rangeIndicator;

    int kmRange;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DBHelper(requireActivity().getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(requireActivity().getApplicationContext()));
        kmRange = PreferenceData.getMaxRangeKM(requireActivity().getApplicationContext());
        eventsHolder = view.findViewById(R.id.eventsHolder);
        gps = new GPSHelper(requireActivity().getApplicationContext(), this);
        rangeIndicator = view.findViewById(R.id.eventsRangeIndicator);
        updateEvents();
    }

    public void updateEvents(){
        rangeIndicator.setText("Showing events in " + kmRange + " KM range");
        if(gps != null && gps.getMyLocation() != null){
            if(getActivity() != null){
                ArrayList<Event> allEvents = db.getAllEvents(kmRange, gps.getLatitude(), gps.getLongitude());
                eventsHolder.removeAllViews();
                if(allEvents.size() == 0){
                    TextView noEv = new TextView(requireActivity().getApplicationContext());
                    noEv.setGravity(Gravity.CENTER);
                    noEv.setPadding(10,10,10,10);
                    noEv.setTextColor(Color.DKGRAY);
                    noEv.setText("No events in range");
                    noEv.setTextSize(20);
                    eventsHolder.addView(noEv);
                }else{
                    for(Event e : allEvents){
                        CardView view1 = new CardView(requireActivity().getApplicationContext());
                        View.inflate(requireActivity().getApplicationContext(), R.layout.event_item, view1);
                        TextView eventDate, eventTitle, eventCombinedSlots;
                        Button viewDetails;
                        eventDate = view1.findViewById(R.id.event_Date);
                        eventTitle = view1.findViewById(R.id.event_Title);
                        eventCombinedSlots = view1.findViewById(R.id.event_combinedSlots);
                        viewDetails = view1.findViewById(R.id.event_viewDetailsBtn);

                        viewDetails.setText(getEventDistanceText(e, gps.getLatitude(), gps.getLongitude()));

                        eventDate.setText(sdf.format(e.getEventStartDate()*1000L));
                        eventTitle.setText(e.getEventTitle());
                        int combinedSlots = 0;
                        for(EventPricing ep : e.getEventPricing()){
                            combinedSlots+=ep.getEPClassStock();
                        }
                        eventCombinedSlots.setText(""+combinedSlots+" tickets left");
                        view1.setOnClickListener(x->{
                            Intent intent = new Intent(requireActivity().getApplicationContext(), EventDetailActivity.class);
                            intent.putExtra("id", e.getEventID());
                            intent.putExtra("markertitle", e.getEventTitle());
                            startActivity(intent);
                        });
                        eventsHolder.addView(view1);
                    }
                }
            }
        }else{
            Toast.makeText(getActivity().getApplicationContext(), "Unable to retrieve current location, showing last fetched events", Toast.LENGTH_LONG).show();
        }
    }

    public String getEventDistanceText(Event e, double lat, double lng){
        //in KM
        double distanceLat = e.getEventLatitude() - lat;
        double distanceLong = e.getEventLongitude() - lng;
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(Math.sqrt(Math.pow(latToKM(distanceLat), 2)+ Math.pow(lngToKM(distanceLong), 2))) + " KM\nfrom you";
    }

    public static double latToKM(double lat){
        return (double)lat/0.00902;
    }

    public static double lngToKM(double lng){
        return (double)lng/0.00898;
    }



}