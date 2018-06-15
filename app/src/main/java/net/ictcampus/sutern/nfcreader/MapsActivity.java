package net.ictcampus.sutern.nfcreader;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.ictcampus.sutern.nfcreader.models.NFC_Location;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference mDatabase;

    private DatabaseReference mNFCReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mNFCReference = mDatabase.child(getUid()).child("History");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mNFCReference
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot iterablesnapshot : dataSnapshot.getChildren()) {
                            double lat =Double.parseDouble(iterablesnapshot.child("location").child("latitude").getValue().toString());
                            double lng = Double.parseDouble(iterablesnapshot.child("location").child("longitude").getValue().toString());
                            String name = iterablesnapshot.child("name_karte").getValue().toString();
                            if(lat != 0 && lng !=0) {
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat, lng))
                                        .title(name));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
