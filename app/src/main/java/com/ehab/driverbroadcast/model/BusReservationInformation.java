package com.ehab.driverbroadcast.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ehabhamdy on 2/16/18.
 */

public class BusReservationInformation {
    String name;
    String from;
    String to;
    String date;
    String time;
    String seats;

    public BusReservationInformation() {
    }

    public BusReservationInformation(String name, String from, String to, String date, String time, String numberOfSeats) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.date = date;
        this.time = time;
        this.seats = numberOfSeats;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("from", from);
        result.put("to", to);
        result.put("date", date);
        result.put("time", time);
        result.put("seats", seats);

        return result;
    }

    public String getName() {
        return name;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getSeats() {
        return seats;
    }
}
