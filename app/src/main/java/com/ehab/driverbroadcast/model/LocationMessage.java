package com.ehab.driverbroadcast.model;

import android.location.Location;

/**
 * Created by ehabhamdy on 7/15/17.
 */

public class LocationMessage {
    String channel;
    Location location;

    public LocationMessage(String channel, Location location) {
        this.channel = channel;
        this.location = location;
    }

    public String getChannel() {
        return channel;
    }

    public Location getLocation() {
        return location;
    }
}
