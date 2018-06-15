package net.ictcampus.sutern.nfcreader.models;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NFC_Location {

    public String name_karte;
    public LatLng location;
    public String time;

    public NFC_Location() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public NFC_Location(String name, LatLng location) {
        this.location = location;
        this.name_karte = name;
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        this.time =  format.format(today);
    }

}
