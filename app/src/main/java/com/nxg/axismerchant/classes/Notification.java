package com.nxg.axismerchant.classes;

/**
 * Created by Dell on 23-07-2016.
 */
public class Notification {

    String UID, message, date, readStatus;

    public Notification(String UID, String message, String date, String readStatus) {
        this.UID = UID;
        this.message = message;
        this.date = date;
        this.readStatus = readStatus;
    }

    public String getUID() {
        return UID;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getReadStatus() {
        return readStatus;
    }
}
