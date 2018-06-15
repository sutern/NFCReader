package net.ictcampus.sutern.nfcreader.models;

import com.google.android.gms.maps.model.LatLng;

public class NFC_Location {

    public String name_karte;
    public LatLng location;

    public NFC_Location() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public NFC_Location(String name, LatLng location) {
        this.location = location;
        this.name_karte = name;
    }

}
