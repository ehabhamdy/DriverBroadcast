package com.ehab.driverbroadcast.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ehabhamdy on 7/10/17.
 */

public class Ticket {
    public String username;
    public com.ehab.driverbroadcast.model.LatLng userLocation;
    public String ticketNumber;
    public String userID;
    public boolean valid = true;

    public Ticket() {
    }

    public Ticket(String username, String ticketNumber, com.ehab.driverbroadcast.model.LatLng userLocation) {
        this.username = username;
        this.ticketNumber = ticketNumber;
        this.userLocation = userLocation;
    }
}
