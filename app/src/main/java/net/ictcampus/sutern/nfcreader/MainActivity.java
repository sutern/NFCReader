package net.ictcampus.sutern.nfcreader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.ictcampus.sutern.nfcreader.models.NFC_Location;

import java.util.Objects;

/**
 * @author glausla
 * @author sutern
 */
public class MainActivity extends parentClass implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    String id_db;

    public ProgressDialog mProgressDialog;

    private boolean isUsed = false;

    private static final int MY_PERMISSION_REQUEST_CODE = 11;
    private GoogleMap mMap;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 10;

    private Location mLastLocation;

    private FusedLocationProviderClient mFusedLocationClient;

    double latitude, longitude;
    String name;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTETS_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private LocationCallback mLocationCallback;

    private DatabaseReference mNFCReference;

    NFCForegroundUtil nfcForegroundUtil = null;

    private DatabaseReference mDatabase;

    private String userid = FirebaseAuth.getInstance().getUid();

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
    private Query getInfoFromDB = db;


    Marker myCurrent;

    Context context;

    /**
     * on create methode
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getColor());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        findViewById(R.id.history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });


        mNFCReference = mDatabase.child(getUid()).child("NFC-Tags");


        getInfoFromDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String email = Objects.requireNonNull(dataSnapshot.child("email").getValue()).toString();
                String name = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();

                NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
                View hv = nv.getHeaderView(0);
                TextView txtUsername = (TextView) hv.findViewById(R.id.username);
                TextView txtEmail = (TextView) hv.findViewById(R.id.useremail);

                txtUsername.setText(name);
                txtEmail.setText(email);

                Snackbar.make(findViewById(R.id.drawer_layout), "Welcome " + name, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        context = this;

        nfcForegroundUtil = new NFCForegroundUtil(this);

        //Maps settings
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult.getLastLocation() == null) {
                    return;
                } else {
                    mLastLocation = locationResult.getLastLocation();
                    displayLocation();

                }
            }
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setUpLocation();

    }

    public void onNewIntent(Intent intent) {
        final Tag tag = intent.getParcelableExtra(android.nfc.NfcAdapter.EXTRA_TAG);
        final String uid = getUid();
        final String id = ByteArrayToHexString(tag.getId());
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("History")) {
                            if (dataSnapshot.hasChild("NFC-Tags")) {

                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog);
                                } else {
                                    builder = new AlertDialog.Builder(context);
                                }

                                builder.setTitle(R.string.add_current_location)

                                        .setMessage(R.string.Message_Alert_location)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                mNFCReference
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot iterablesnapshot : dataSnapshot.getChildren()) {
                                                                    String id_db = iterablesnapshot.child("id").getValue().toString();
                                                                    if (id.equals(id_db)) {
                                                                        name = iterablesnapshot.child("name").getValue().toString();
                                                                        isUsed = true;
                                                                    }

                                                                }
                                                                if (isUsed) {


                                                                    NFC_Location location = new NFC_Location(name, new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                                                                    // Push the comment, it will appear in the list
                                                                    db.child("History").push().setValue(location);

                                                                    isUsed = false;
                                                                } else {
                                                                    Toast.makeText(context, R.string.card_notregistered, Toast.LENGTH_LONG).show();

                                                                    isUsed = false;
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                            }
                                                        });
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .setIcon(R.drawable.ic_dialog_add
                                        );
                                builder.show();
                            } else {
                                Toast.makeText(context, R.string.card_notregistered, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            NFC_Location location = new NFC_Location(getString(R.string.first_point), new LatLng(0, 0));

                            // Push the comment, it will appear in the list
                            db.child("History").push().setValue(location);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    //Sidebar
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finishAffinity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_addCard) {
            startActivity(new Intent(MainActivity.this, cardsActivity.class));
            return true;
        } else if (id == R.id.nav_contact) {
            String[] to = {"sugla.consulting@gmail.com"};
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, to);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            intent.setType("message/rfc822");
            Intent chooser = Intent.createChooser(intent, "Send us Feedback");
            startActivity(chooser);

        } else if (id == R.id.nav_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent();
            setResult(1, intent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        startLocationUpdates();
        displayLocation();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }


    public void hideKeyboard(View view) {
        final InputMethodManager imn = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imn != null) {
            imn.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * when the app has connected with google maps it starts the search for
     * the location
     *
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {


        displayLocation();
        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /**
     * starts the functions used to search the current location if the permission
     * has been granted
     *
     * @param requestCode  the access that has been requested
     * @param permissions  which permissions were granted
     * @param grantResults used to check if the permission has been granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {

                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }

    }

    /**
     * sets up the location with every methode
     */
    private void setUpLocation() {
        if (android.support.v4.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                android.support.v4.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestRuntimePermission();

        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }
        }
    }

    /**
     * adds the location to the map and moves the camera
     */
    private void displayLocation() {
        if (android.support.v4.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                android.support.v4.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastLocation = location;
                        }
                    }
                });
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            if (myCurrent != null) {
                myCurrent.remove();
            }

            myCurrent = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(getString(R.string.your_location)));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));


        }
    }

    /**
     * creates a location request
     */
    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTETS_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * makes a googleAPIClient and connects the client
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    /**
     * checks if the user has google play services
     *
     * @return returns true when the google play service is available
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (googleAPI.isUserResolvableError(resultCode)) {
                googleAPI.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {

                Toast.makeText(this, "This device can't support", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }

    /**
     * requests permission to access the gps
     */
    private void requestRuntimePermission() {
        android.support.v4.app.ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION

                }, MY_PERMISSION_REQUEST_CODE);
    }

    /**
     * updates the startLocation
     */
    private void startLocationUpdates() {
        if (android.support.v4.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                android.support.v4.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);


    }

    /**
     * sets the map to the mMap
     *
     * @param map the map on the xml file
     */
    public void onMapReady(GoogleMap map) {
        mMap = map;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        nfcForegroundUtil.disableForeground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        displayLocation();
        nfcForegroundUtil.enableForeground();
        if (!android.nfc.NfcAdapter.getDefaultAdapter(this.getApplicationContext()).isEnabled()) {
            Toast.makeText(getApplicationContext(), R.string.Warning_NFC_turned_off, Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));

        }
    }


    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

}



