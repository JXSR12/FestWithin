package com.jxsr.festwithin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.jxsr.festwithin.models.Event;
import com.jxsr.festwithin.models.EventPricing;
import com.jxsr.festwithin.models.Ticket;
import com.jxsr.festwithin.models.User;
import com.jxsr.festwithin.models.Wallet;
import com.jxsr.festwithin.models.WalletTransaction;
import com.jxsr.festwithin.models.enums.TransactionStatus;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class EventDetailActivity extends AppCompatActivity {
    DBHelper db;
    String title, eventId, eventDesc;
    Event event;
    ImageView ivBanner;
    TextView tvTitle, tvDesc, tvOrganizer, tvDateTime, tvLocation;
    Button showInMaps, messageOrg;
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy / HH:mm z");
    ArrayList<EventPricing> eventPricing;
    LinearLayout eventPricingsHolder;
    User user;
    Wallet userWallet;
    EditText message;

    String orgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        db = new DBHelper(getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(getApplicationContext()));
        userWallet = db.getUserWallet(user.getUserID());
        this.title = (String) getIntent().getSerializableExtra("markertitle");
        this.eventId = (String) getIntent().getSerializableExtra("id");
        event = db.getEventFromID(eventId);
        if(event != null){
            this.eventDesc = db.getEventFromID(eventId).getEventDescription();
            this.eventPricing = db.getEventPricingsFromID(eventId);
            ivBanner = findViewById(R.id.eventBanner);
            tvTitle = findViewById(R.id.eventTitle);
            tvDesc = findViewById(R.id.eventDesc);
            tvOrganizer = findViewById(R.id.eventOrganizer);
            tvDateTime = findViewById(R.id.eventDateTime);
            tvLocation = findViewById(R.id.eventLocation);
            eventPricingsHolder = findViewById(R.id.eventPricingsHolder);
            showInMaps = findViewById(R.id.btnShowInMaps);
            messageOrg = findViewById(R.id.btnMessageOrg);

            tvTitle.setText(event.getEventTitle());
            tvDesc.setText(event.getEventDescription());
            User organizer = db.getUserFromID(event.getEventOrganizerUserID());
            tvOrganizer.setText(organizer.isUserIsOrganizer() ? organizer.getUserOrganizerName() : organizer.getUserDisplayName());
            sdf.setTimeZone(TimeZone.getTimeZone(PreferenceData.getUserTimezone(getApplicationContext())));
            tvDateTime.setText(sdf.format(event.getEventStartDate()*1000L));
            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
            Address address = null;
            try {
                address = geo.getFromLocation(event.getEventLatitude(), event.getEventLongitude(), 1).get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String strLocation = null;
            if(address != null){
                strLocation = !address.getAddressLine(0).isEmpty() ? address.getAddressLine(0) : (address.getLocality() + ", " + address.getAdminArea() + ", " + address.getCountryName());
            }else{
                strLocation = " - ";
            }
            tvLocation.setText(event.getEventLocationDesc()+"\n\nDetailed Address: \n"+strLocation);

            showInMaps.setOnClickListener(x->{
                Uri gmmIntentUri = Uri.parse("geo:"+event.getEventLatitude()+","+event.getEventLongitude()+"?z=20");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            });

            orgName = db.getUserFromID(event.getEventOrganizerUserID()).getUserOrganizerName() == null ? db.getUserFromID(event.getEventOrganizerUserID()).getUserDisplayName() : db.getUserFromID(event.getEventOrganizerUserID()).getUserOrganizerName();

            message = new EditText(this);
            message.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            messageOrg.setOnClickListener(x->{
                AlertDialog.Builder a = new AlertDialog.Builder(this);
                a.setTitle("Message Organizer");
                a.setMessage("Enter your message to " + orgName);
                if(message.getParent() != null){
                    ((ViewGroup)message.getParent()).removeView(message);
                }
                a.setView(message);
                a.setCancelable(true);
                a.setPositiveButton("Send", (arg0, arg1) -> {
                    if(ContextCompat.checkSelfPermission(EventDetailActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                        sendMessage();
                    }else{
                        ActivityCompat.requestPermissions(EventDetailActivity.this, new String[]{Manifest.permission.SEND_SMS}, 100);
                    }

                });
                a.setNegativeButton("Cancel", (arg0, arg1) -> {});
                a.show();
            });

            for(EventPricing ep : eventPricing){
                CardView temp = new CardView(getApplicationContext());
                View.inflate(getApplicationContext(), R.layout.card_pricing, temp);
                TextView epName = temp.findViewById(R.id.ep_classname);
                TextView epPriceText = temp.findViewById(R.id.ep_pricetext);
                Button epBuyBtn = temp.findViewById(R.id.ep_buybtn);

                epName.setText(ep.getEPClassName());
                epPriceText.setText(ep.getEPClassCurrency() + " " + ep.getEPClassPrice());
                epBuyBtn.setText(ep.getEPClassStock() + " left");

                if(ep.getEPClassStock() <= 0){
                    epBuyBtn.setClickable(false);
                    epBuyBtn.setEnabled(false);
                }

                if(userWallet != null){
                    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
                    bottomSheetDialog.setContentView(R.layout.bottom_sheet_payment);
                    bottomSheetDialog.setTitle("Payment options");

                    bottomSheetDialog.findViewById(R.id.buttonPay).setOnClickListener(y->{
                        Toast.makeText(this, "Processing transaction", Toast.LENGTH_SHORT).show();
                        String newTransID = String.format("T%04d", db.getTransactionsCount()+1);

                        db.addTransaction(newTransID,
                                userWallet.getWalletID(),
                                db.getUserWallet(event.getEventOrganizerUserID()).getWalletID(),
                                Instant.now().getEpochSecond(),
                                userWallet.getWalletCurrency(),
                                db.convertCurrency(ep.getEPClassPrice(), ep.getEPClassCurrency(), userWallet.getWalletCurrency()),
                                TransactionStatus.UNPAID
                                );
                        int res = -3;
                        if(db.getEPClassStock(event.getEventID(), ep.getEPClassName()) > 0){
                            res = db.getTransactionFromID(newTransID).pay(db);
                        }

                        if(res == -3){
                            Toast.makeText(this, "Requested ticket is out of stock, transaction cancelled", Toast.LENGTH_SHORT).show();
                        }else if(res == -1){
                            Toast.makeText(this, "Not enough wallet balance, transaction cancelled.", Toast.LENGTH_SHORT).show();
                        }else if (res == -2){
                            Toast.makeText(this, "Transaction is already paid, payment cancelled", Toast.LENGTH_SHORT).show();
                        }else{
                            WalletTransaction confirm = db.getTransactionFromID(newTransID);
                            if(confirm.getTransactionStatus() == TransactionStatus.PAID){
                                Toast.makeText(this, "Transaction paid!", Toast.LENGTH_SHORT).show();
                                addTicketToUser(ep.getEPClassName(), newTransID);
                            }else{
                                Toast.makeText(this, "Unable to complete transaction at this time", Toast.LENGTH_SHORT).show();
                            }
                        }
                        bottomSheetDialog.dismiss();
                    });

                    ((TextView)bottomSheetDialog.findViewById(R.id.textView3)).setText(db.getUserFromID(userWallet.getUserID()).getUserDisplayName());
                    ((TextView)bottomSheetDialog.findViewById(R.id.textView2)).setText("**** **** " + userWallet.getWalletPhone().substring(userWallet.getWalletPhone().length()-4));
                    if(userWallet.getWalletCurrency().equals(ep.getEPClassCurrency())){
                        ((TextView)bottomSheetDialog.findViewById(R.id.textView4)).setText(ep.getEPClassCurrency() + " " + ep.getEPClassPrice());
                    }else{
                        ((TextView)bottomSheetDialog.findViewById(R.id.textView4)).setText(userWallet.getWalletCurrency() + " " + db.convertCurrency(ep.getEPClassPrice(), ep.getEPClassCurrency(), userWallet.getWalletCurrency()));
                    }


                    epBuyBtn.setOnClickListener(x->{
                        bottomSheetDialog.show();
                    });
                }else{
                    epBuyBtn.setOnClickListener(x->{
                        Toast.makeText(getApplicationContext(), "You do not have a FestWallet set up, set one first", Toast.LENGTH_LONG).show();
                    });
                }


                eventPricingsHolder.addView(temp);
            }

            Picasso.get().load(event.getEventBannerSrc()).into(ivBanner);
        }

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(this.title);
    }

    public void sendMessage(){
        String msg = message.getText().toString();
        if(!msg.isEmpty()){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(db.getUserFromID(event.getEventOrganizerUserID()).getUserPhone(),null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message successfully sent to "+ orgName, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void addTicketToUser(String TicketClassName, String TransactionID){
        Ticket newTicket = Ticket.generateTicket(db, event.getEventID(), TicketClassName, user.getUserID(), TransactionID);
        db.updateEPStock(db.getEPClassStock(event.getEventID(), TicketClassName)-1,event.getEventID(), TicketClassName);
        db.addTicket(newTicket.getTicketID(),newTicket.getEventID(),newTicket.getEventPricingClassName(),newTicket.getTicketOwnerUserID(),newTicket.getTicketTransactionID(),newTicket.getTicketUniqueToken());
        Intent toTicket = new Intent(EventDetailActivity.this, TicketDetailActivity.class);
        toTicket.putExtra("ticketId", newTicket.getTicketID());
        startActivity(toTicket);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        this.onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            sendMessage();
        }else{
            Toast.makeText(getApplicationContext(), "Permission Denied !", Toast.LENGTH_SHORT).show();
        }
    }
}