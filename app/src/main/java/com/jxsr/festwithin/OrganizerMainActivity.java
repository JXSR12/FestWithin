package com.jxsr.festwithin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jxsr.festwithin.models.Event;
import com.jxsr.festwithin.models.EventPricing;
import com.jxsr.festwithin.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class OrganizerMainActivity extends AppCompatActivity {
    DBHelper db;
    User user;

    TextView organizerName;
    LinearLayout myEvents;
    Button addEventBtn, changeOrgName;

    SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy");
    ArrayList<Event> myEventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_main);

        organizerName = findViewById(R.id.org_name);
        myEvents = findViewById(R.id.myEventsHolder);
        addEventBtn = findViewById(R.id.buttonAddNewEvent);
        changeOrgName = findViewById(R.id.buttonChangeOrgName);

        db = new DBHelper(getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(getApplicationContext()));

        addEventBtn.setOnClickListener(x->{
            Intent addEvent = new Intent(OrganizerMainActivity.this, AddEventActivity.class);
            startActivity(addEvent);
            finish();
        });

        changeOrgName.setOnClickListener(x->{
            EditText orgNameEditor = new EditText(this);
            AlertDialog.Builder a = new AlertDialog.Builder(this);
            a.setTitle("Change Organizer Name");
            a.setMessage("Enter your new organizer name");
            if(orgNameEditor.getParent() != null){
                ((ViewGroup)orgNameEditor.getParent()).removeView(orgNameEditor);
            }
            a.setView(orgNameEditor);
            a.setCancelable(true);
            a.setPositiveButton("Set", (arg0, arg1) -> {
                String organizerNameStr = "";
                if(orgNameEditor.getText().toString().isEmpty()){
                    organizerNameStr = user.getUserOrganizerName();
                }else{
                    organizerNameStr = orgNameEditor.getText().toString();
                }
                db.changeOrganizerName(user.getUserID(),organizerNameStr);
                user = db.getUserFromID(PreferenceData.getLoggedInUserId(getApplicationContext()));
                organizerName.setText(user.getUserOrganizerName());

            });
            a.setNegativeButton("Cancel", (arg0, arg1) ->{});
            a.show();
        });
        organizerName.setText(user.getUserOrganizerName());
        myEventsList = db.getAllCreatedEvents(user.getUserID());

        if(myEventsList.size() == 0){
            TextView noEv = new TextView(this);
            noEv.setGravity(Gravity.CENTER);
            noEv.setPadding(10,10,10,10);
            noEv.setTextColor(Color.DKGRAY);
            noEv.setText("You have not created any event");
            noEv.setTextSize(20);
            myEvents.addView(noEv);
        }else{
            for(Event e : myEventsList){
                CardView view1 = new CardView(this);
                View.inflate(this, R.layout.event_item, view1);
                TextView eventDate, eventTitle, eventCombinedSlots;
                Button viewDetails;
                eventDate = view1.findViewById(R.id.event_Date);
                eventTitle = view1.findViewById(R.id.event_Title);
                eventCombinedSlots = view1.findViewById(R.id.event_combinedSlots);
                viewDetails = view1.findViewById(R.id.event_viewDetailsBtn);

                eventDate.setText(sdf.format(e.getEventStartDate()*1000L));
                eventTitle.setText(e.getEventTitle());
                int combinedSlots = 0;
                for(EventPricing ep : e.getEventPricing()){
                    combinedSlots+=ep.getEPClassStock();
                }
                eventCombinedSlots.setText(""+combinedSlots+" tickets left");
                viewDetails.setOnClickListener(x->{
                    Intent intent = new Intent(OrganizerMainActivity.this, EventDetailActivity.class);
                    intent.putExtra("id", e.getEventID());
                    intent.putExtra("markertitle", e.getEventTitle());
                    startActivity(intent);
                });
                myEvents.addView(view1);
            }
        }
    }
}