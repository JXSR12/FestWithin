package com.jxsr.festwithin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jxsr.festwithin.models.Event;
import com.jxsr.festwithin.models.EventPricing;
import com.jxsr.festwithin.models.User;
import com.jxsr.festwithin.models.Wallet;
import com.vanillaplacepicker.data.VanillaAddress;
import com.vanillaplacepicker.presentation.builder.VanillaPlacePicker;
import com.vanillaplacepicker.utils.KeyUtils;
import com.vanillaplacepicker.utils.MapType;
import com.vanillaplacepicker.utils.PickerLanguage;
import com.vanillaplacepicker.utils.PickerType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddEventActivity extends AppCompatActivity {
    DBHelper db;
    User user;
    Wallet userWallet;

    EditText etEventTitle, etEventDesc, etLocDet;
    TextView addressDisplay, dateTimeDisplay;

    Button pickLocation, pickDateTime, finalizeBtn;

    LinearLayout pricingsHolder;

    boolean isLocationSet = false;
    int classPricingCount = 0;

    SimpleDateFormat sdf;
    DatePickerDialog startDatePicker;
    TimePickerDialog startTimePicker;

    String datetimeString;
    double pickLat = -1000, pickLng = -1000;
    String eventTitle, eventDesc, eventLocDet;
    ArrayList<EventPricing> eventPricings = new ArrayList<>();

    final long[] unixStartDate = new long[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        db = new DBHelper(getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(getApplicationContext()));
        userWallet = db.getUserWallet(user.getUserID());
        sdf = new SimpleDateFormat("dd MMMM yyyy / HH:mm z");
        sdf.setTimeZone(TimeZone.getTimeZone(PreferenceData.getUserTimezone(getApplicationContext())));

        etEventTitle = findViewById(R.id.addEventTitle);
        etEventDesc = findViewById(R.id.addEventDesc);
        etLocDet = findViewById(R.id.addEventLocDesc);
        addressDisplay = findViewById(R.id.addEventGeocodeDisplay);
        dateTimeDisplay = findViewById(R.id.addEventDateTimeDisplay);
        pickLocation = findViewById(R.id.addEventPickLoc);
        pickDateTime = findViewById(R.id.addEventChangeDateTime);
        finalizeBtn = findViewById(R.id.addEventFinishBtn);

        pricingsHolder = findViewById(R.id.addEventPricingsHolder);

        pickDateTime.setOnClickListener(x->{
            startDatePicker.show();
        });

        dateTimeDisplay.setText("Starting date and time not set");

        TimeZone def = TimeZone.getDefault();
        TimeZone tz = TimeZone.getTimeZone(PreferenceData.getUserTimezone(getApplicationContext()));

        startDatePicker = new DatePickerDialog(this);
        startDatePicker.setOnDateSetListener((datePicker, year, month, dayOfMonth) ->
        {
            Instant temp = Instant.parse(""+String.format("%04d",year)+"-"+String.format("%02d",month+1)+"-"+String.format("%02d",dayOfMonth)+"T00:00:00Z");
            unixStartDate[0] = temp.getEpochSecond()-(def.getRawOffset()/1000L);
            startTimePicker.show();
        });
        Calendar cur = Calendar.getInstance();
        cur.setTimeZone(tz);
        int hour = cur.get(Calendar.HOUR_OF_DAY);
        int minute = cur.get(Calendar.MINUTE);
        startTimePicker = new TimePickerDialog(this, (timePicker, selHour, selMinute) -> {
            unixStartDate[0]+= (selHour*3600);
            unixStartDate[0]+= (selMinute*60);
            datetimeString = sdf.format(unixStartDate[0]*1000L);
            dateTimeDisplay.setText(datetimeString);
        },hour, minute, true);

        pickLocation.setOnClickListener(x->{
            openLocationPicker();
        });

        CardView initial = new CardView(this);
        View.inflate(this, R.layout.add_pricing_item, initial);
        EditText epInputName = initial.findViewById(R.id.addEPClassName);
        EditText epInputPrice = initial.findViewById(R.id.addEPPrice);
        EditText epInputStock = initial.findViewById(R.id.addEPStock);
        epInputPrice.setHint("Price ("+ userWallet.getWalletCurrency() +")");
        Button addMore1 = initial.findViewById(R.id.addEPAddMore);
        addMore1.setOnClickListener(x->{
            String epName = epInputName.getText().toString();
            Double epPrice;
            Integer epStock;
            try {
                epPrice = Double.valueOf(epInputPrice.getText().toString());
                epStock = Integer.valueOf(epInputStock.getText().toString());
            }catch(Exception e){
                epPrice = -1.0;
                epStock = -1;
            }
            if(epName.length() > 3 && epPrice >= 0.0 && validateEPUnique(epName)){
                CardView nextEP = new CardView(this);
                View.inflate(this, R.layout.card_pricing_nobtn, nextEP);
                TextView epClassName, epClassPrice;
                Button epClassStock;
                epClassName = nextEP.findViewById(R.id.ep_nb_classname);
                epClassPrice = nextEP.findViewById(R.id.ep_nb_pricetext);
                epClassStock = nextEP.findViewById(R.id.ep_nb_slots);
                epClassName.setText(epName);
                epClassPrice.setText(userWallet.getWalletCurrency() + " " + epPrice);
                epClassStock.setText(epStock + " slots");
                pricingsHolder.addView(nextEP);
                eventPricings.add(new EventPricing("NOTSET", epName, userWallet.getWalletCurrency(), epPrice, epStock));
                classPricingCount++;
                epInputName.setText("");
                epInputPrice.setText("");
                epInputStock.setText("");
            }else{
                Toast.makeText(this, "Class name must be > 3 chars and unique!\nPrice cannot be negative!", Toast.LENGTH_SHORT).show();
            }
        });
        pricingsHolder.addView(initial);

        finalizeBtn.setOnClickListener(x->{
            eventTitle = etEventTitle.getText().toString();
            eventDesc = etEventDesc.getText().toString();
            eventLocDet = etLocDet.getText().toString();

           if(validateInputs()){
               if(etEventDesc.getText().toString().isEmpty()){
                   eventDesc = "No description available";
               }
               if(etLocDet.getText().toString().isEmpty()){
                   eventLocDet = "";
               }
               Event newEvent = new Event(String.format("E%04d", db.getEventsCount()+1), user.getUserID(), eventTitle, unixStartDate[0], eventDesc, pickLat, pickLng, eventLocDet);
               db.addEvent(newEvent.getEventID(),
                       newEvent.getEventOrganizerUserID(),
                       newEvent.getEventTitle(),
                       newEvent.getEventStartDate(),
                       newEvent.getEventDescription(),
                       newEvent.getEventLatitude(),
                       newEvent.getEventLongitude(),
                       newEvent.getEventLocationDesc()
                       );
               for(EventPricing ep : eventPricings){
                   ep.setEventID(newEvent.getEventID());
                   db.addEventPricing(ep.getEventID(), ep.getEPClassName(), ep.getEPClassCurrency(), ep.getEPClassPrice(), ep.getEPClassStock());
               }
               Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show();
               Intent intent = new Intent(AddEventActivity.this, EventDetailActivity.class);
               intent.putExtra("id", newEvent.getEventID());
               intent.putExtra("markertitle", newEvent.getEventTitle());
               startActivity(intent);
               finish();
           }else{
               Toast.makeText(this, "Fill all required (*) fields and starting time cannot be in the past", Toast.LENGTH_SHORT).show();
           }
        });

    }

    public void openLocationPicker(){
//        Intent intent = new Intent(this, NiboPlacePickerActivity.class);
//        NiboPlacePickerActivity.NiboPlacePickerBuilder config = new NiboPlacePickerActivity.NiboPlacePickerBuilder()
//                .setSearchBarTitle("Search for a place")
//                .setConfirmButtonTitle("Use this location")
//                .setMarkerPinIconRes(R.drawable.ic_fw_eventpoint)
//                .setStyleEnum(NiboStyle.SUBTLE_GREY_SCALE)
//         .setStyleFileID(com.alium.nibo.R.raw.retro);
//        NiboPlacePickerActivity.setBuilder(config);
//        startActivityForResult(intent, 101);
        Intent intent = new VanillaPlacePicker.Builder(this)
                .with(PickerType.MAP_WITH_AUTO_COMPLETE)
                .setPickerLanguage(PickerLanguage.ENGLISH)
                .enableShowMapAfterSearchResult(true)
                .setMapType(MapType.SATELLITE)
                .setMapPinDrawable(R.drawable.ic_fw_eventpoint)
                .build();
        startActivityForResult(intent, KeyUtils.REQUEST_PLACE_PICKER);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == KeyUtils.REQUEST_PLACE_PICKER) {
                VanillaAddress rawAddress = (VanillaAddress) data.getSerializableExtra(KeyUtils.SELECTED_PLACE);
                Double latitude = rawAddress.getLatitude();
                Double longitude = rawAddress.getLongitude();
                Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                Address address = null;
                try {
                    address = geo.getFromLocation(latitude, longitude, 1).get(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String strLocation;
                if(address != null){
                    strLocation = !address.getAddressLine(0).isEmpty() ? address.getAddressLine(0) : (address.getLocality() + ", " + address.getAdminArea() + ", " + address.getCountryName());
                }else{
                    strLocation = " - ";
                }
                pickLat = latitude;
                pickLng = longitude;
                addressDisplay.setText(strLocation);
                Toast.makeText(getApplicationContext(), "Location set!" + address, Toast.LENGTH_SHORT).show();
                isLocationSet = true;
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Could not set location", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validateInputs(){
        return (etEventTitle.getText().toString().length() > 3) &&
                (unixStartDate[0] > Instant.now().getEpochSecond()) &&
                isLocationSet && (classPricingCount >= 1);
    }

    public boolean validateEPUnique(String epClassName){
        for(EventPricing ep : eventPricings){
            if(ep.getEPClassName().equals(epClassName)){
                return false;
            }
        }
        return true;
    }
}