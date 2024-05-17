package com.jxsr.festwithin.models;

import com.jxsr.festwithin.DBHelper;

import java.util.UUID;

public class Ticket {
    private String TicketID;
    private String EventID;
    private String EventPricingClassName;
    private String TicketUniqueToken;
    private String TicketOwnerUserID;
    private String TicketTransactionID;

    public Ticket(String ticketID, String eventID, String eventPricingClassName, String ticketOwnerUserID, String ticketTransactionID, String ticketUniqueToken) {
        TicketID = ticketID;
        EventID = eventID;
        EventPricingClassName = eventPricingClassName;
        TicketOwnerUserID = ticketOwnerUserID;
        TicketTransactionID = ticketTransactionID;
        TicketUniqueToken = ticketUniqueToken;
    }

    public void invalidate(DBHelper db){
        this.TicketUniqueToken = "Invalid/Expired";
        db.invalidateTicket(this.TicketID);
    }

    public static Ticket generateTicket(DBHelper db, String eventID, String eventPricingClassName, String ticketOwnerUserID, String ticketTransactionID){
        return new Ticket(String.format("C%04d", db.getTicketsCount()+1), eventID, eventPricingClassName, ticketOwnerUserID, ticketTransactionID, UUID.randomUUID().toString());
    }

    public String getTicketID() {
        return TicketID;
    }

    public void setTicketID(String ticketID) {
        TicketID = ticketID;
    }

    public String getEventID() {
        return EventID;
    }

    public void setEventID(String eventID) {
        EventID = eventID;
    }

    public String getEventPricingClassName() {
        return EventPricingClassName;
    }

    public void setEventPricingClassName(String eventPricingClassName) {
        EventPricingClassName = eventPricingClassName;
    }

    public String getTicketUniqueToken() {
        return TicketUniqueToken;
    }

    public String getTicketOwnerUserID() {
        return TicketOwnerUserID;
    }

    public void setTicketOwnerUserID(String ticketOwnerUserID) {
        TicketOwnerUserID = ticketOwnerUserID;
    }

    public String getTicketTransactionID() {
        return TicketTransactionID;
    }
}
