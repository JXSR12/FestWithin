package com.jxsr.festwithin.models;

import java.util.ArrayList;

public class Event {
    private String EventID;
    private String EventOrganizerUserID;
    private String EventTitle;
    private long EventStartDate;
    private String EventDescription;
    private String EventBannerSrc;
    private double EventLatitude, EventLongitude;
    private String EventLocationDesc;
    private final ArrayList<EventPricing> eventPricing = new ArrayList<>();

    public Event(String eventID, String eventOrganizerUserID, String eventTitle, long eventStartDate, String eventDescription, double eventLatitude, double eventLongitude, String eventLocationDesc) {
        EventID = eventID;
        EventOrganizerUserID = eventOrganizerUserID;
        EventTitle = eventTitle;
        EventStartDate = eventStartDate;
        EventDescription = eventDescription;
        EventLatitude = eventLatitude;
        EventLongitude = eventLongitude;
        EventLocationDesc = eventLocationDesc;
    }

    public String getEventLocationDesc() {
        return EventLocationDesc;
    }

    public void setEventLocationDesc(String eventLocationDesc) {
        EventLocationDesc = eventLocationDesc;
    }

    public String getEventBannerSrc() {
        return EventBannerSrc;
    }

    public void setEventBannerSrc(String eventBannerSrc) {
        EventBannerSrc = eventBannerSrc;
    }

    public ArrayList<EventPricing> getEventPricing() {
        return eventPricing;
    }

    public double getEventLatitude() {
        return EventLatitude;
    }

    public void setEventLatitude(double eventLatitude) {
        EventLatitude = eventLatitude;
    }

    public double getEventLongitude() {
        return EventLongitude;
    }

    public void setEventLongitude(double eventLongitude) {
        EventLongitude = eventLongitude;
    }

    public String getEventID() {
        return EventID;
    }

    public void setEventID(String eventID) {
        EventID = eventID;
    }

    public String getEventOrganizerUserID() {
        return EventOrganizerUserID;
    }

    public void setEventOrganizerUserID(String eventOrganizerUserID) {
        EventOrganizerUserID = eventOrganizerUserID;
    }

    public String getEventTitle() {
        return EventTitle;
    }

    public void setEventTitle(String eventTitle) {
        EventTitle = eventTitle;
    }

    public long getEventStartDate() {
        return EventStartDate;
    }

    public void setEventStartDate(long eventStartDate) {
        EventStartDate = eventStartDate;
    }

    public String getEventDescription() {
        return EventDescription;
    }

    public void setEventDescription(String eventDescription) {
        EventDescription = eventDescription;
    }
}
