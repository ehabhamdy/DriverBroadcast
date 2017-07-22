package com.ehab.driverbroadcast.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Ehab on 10/22/2016.
 */


@IgnoreExtraProperties
public class Driver {

    public String username;
    public String email;
    public String busNumber;
    public String line;
    public int freePlaces = 12;

    public Driver() {
        // Default constructor required for calls to DataSnapshot.getValue(Driver.class)
    }

    public Driver(String username) {
        this.username = username;
    }
    public Driver(String username, String email, String busNumber, String line) {
        this.username = username;
        this.email = email;
        this.line = line;
        this.busNumber = busNumber;
    }
}