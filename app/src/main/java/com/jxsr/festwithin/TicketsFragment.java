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

import com.jxsr.festwithin.models.Ticket;
import com.jxsr.festwithin.models.User;

import java.util.ArrayList;

public class TicketsFragment extends Fragment {
    LinearLayout ticketsHolder;
    DBHelper db;
    User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tickets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DBHelper(requireActivity().getApplicationContext());
        user = db.getUserFromID(PreferenceData.getLoggedInUserId(requireActivity().getApplicationContext()));
        ticketsHolder = view.findViewById(R.id.ticketsHolder);

        ArrayList<Ticket> userTickets = db.getUserTickets(user.getUserID());

        if(userTickets.size() == 0){
            TextView noTx = new TextView(requireActivity().getApplicationContext());
            noTx.setGravity(Gravity.CENTER);
            noTx.setPadding(10,10,10,10);
            noTx.setTextColor(Color.DKGRAY);
            noTx.setText("No ticket found");
            noTx.setTextSize(20);
            ticketsHolder.addView(noTx);
        }else{
            for(Ticket t : userTickets){
                CardView view1 = new CardView(requireActivity().getApplicationContext());
                View.inflate(requireActivity().getApplicationContext(), R.layout.ticket_item, view1);
                TextView ticketEvent, ticketClass, ticketIdentifier;
                Button viewDetails;
                ticketEvent = view1.findViewById(R.id.ticket_eventName);
                ticketClass = view1.findViewById(R.id.ticket_className);
                ticketIdentifier = view1.findViewById(R.id.ticket_Identifier);
                viewDetails = view1.findViewById(R.id.ticket_viewDetailsBtn);

                ticketEvent.setText(db.getEventFromID(t.getEventID()).getEventTitle());
                ticketClass.setText(t.getEventPricingClassName());
                ticketIdentifier.setText(t.getTicketUniqueToken().substring(t.getTicketUniqueToken().length()-7));
                view1.setOnClickListener(x->{
                    Intent ticketDetail = new Intent(requireActivity().getApplicationContext(), TicketDetailActivity.class);
                    ticketDetail.putExtra("ticketId", t.getTicketID());
                    startActivity(ticketDetail);
                });
                ticketsHolder.addView(view1);
            }
        }

    }
}