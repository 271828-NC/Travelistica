package com.example.travelistica;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static String TAG = "MapsActivity";
    private DrawerLayout drawer;
    private Slider slider;
    private GoogleMap mMap;
    private GoogleApiClient client;
    SupportMapFragment mapFragment;
    private LocationRequest locationRequest;
    private Location lastlocation;
    LocationManager locationManager;
    private Marker currentLocationmMarker;
    int PROXIMITY_RADIUS = 500;
    double latitude, longitude;
    double currentLatitude = 0.0;
    double currentLongitude = 0.0;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    String selectedPlace = "";
    GetNearbyPlacesData getNearbyPlacesData;

    @SuppressLint("MissingPermission")
    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION, false);

                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.

                            locationPermissionGranted();

                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                            locationPermissionGranted();
                        } else {
                            Toast.makeText(MapsActivity.this, "No permission accepted",
                                    Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "No permission accepted");
                        }
                    }
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        slider = findViewById(R.id.slider);
        NavigationView nv = findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);//Set the action bar to the toolbar
        //Toggle is used for when the user click on the drawer button
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();//covers the rotating hamburger icon


        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                Log.d(TAG, "onValueChange: " + value);
                PROXIMITY_RADIUS = (int) value;
                mMap.clear();
                updatePlaces();
            }
        });

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            locationPermissionGranted();
        }


    }

    private void locationPermissionGranted() {
        Log.d(TAG, "locationPermissionGranted");

        if (client == null) {
            bulidGoogleApiClient();
        }
        mapFragment.getMapAsync(this);
    }

    private void CreateCircle() {
        Log.d(TAG, "CreateCircle");
        Log.d(TAG, "CreateCircle: currentLatitude = " + latitude + " | currentLongitude = " + longitude);
        LatLng userLatLng = new LatLng(latitude, longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(userLatLng);//marker on the user's current location
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));//color of the marker
        currentLocationmMarker = mMap.addMarker(markerOptions);//add the marker
        CircleOptions circle_details = new CircleOptions()
                //creating a new circle of center user position and the range selected from the slider
                .center(userLatLng)
                .radius(PROXIMITY_RADIUS);

        mMap.addCircle(circle_details);//display circle on map
    }

    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapscustomization));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onMapReady: currentLatitude = " + currentLatitude + " | currentLongitude = " + currentLongitude);


            if (client == null) {
                bulidGoogleApiClient();
            }
            mMap.setMyLocationEnabled(true);
        }
    }


    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        latitude = location.getLatitude();//current latitude
        longitude = location.getLongitude();//current longitude
        Log.d(TAG, "onLocationChanged: latitude = " + latitude + " | longitude = " + longitude);

        lastlocation = location;
        Log.d("lat = ", "" + latitude);
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        updatePlaces();
        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    private void updatePlaces() {
        if (selectedPlace.isEmpty()) {
            Log.d(TAG, "updatePlaces: selectedPlace is empty");
        } else {
            Log.d(TAG, "updatePlaces: selectedPlace = " + selectedPlace);
            Object dataTransfer[] = new Object[2];

            mMap.clear();//clear map

            String url = getUrl(latitude, longitude, selectedPlace);//get url
            dataTransfer[0] = mMap;
            dataTransfer[1] = url;

            if (getNearbyPlacesData != null) {
                getNearbyPlacesData.cancel(true);
            }
            getNearbyPlacesData = new GetNearbyPlacesData();//displaying nearby places
            getNearbyPlacesData.execute(dataTransfer);//displaying nearby places based on url data

        }
        CreateCircle();
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_option1:
                selectedPlace = "hospital";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);

                Toast.makeText(MapsActivity.this, "Showing Nearby Hospitals", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option2:
                selectedPlace = "school";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Schools", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option3:
                selectedPlace = "restaurant";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Restaurants", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option4:
                selectedPlace = "tourist_attraction";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Tourist Attractions", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option5:
                selectedPlace = "zoo";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Zoos", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option6:
                selectedPlace = "aquarium";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Aquariums", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option7:
                selectedPlace = "church";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Churches", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option8:
                selectedPlace = "museum";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Museums", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option9:
                selectedPlace = "art_gallery";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Art Galleries", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option10:
                selectedPlace = "amusement_park";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Amusement Parks", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_option11:
                selectedPlace = "movie_theater";
                updatePlaces();
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(MapsActivity.this, "Showing Nearby Cinemas", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MapsActivity.this, "User has been logged out successfully", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MapsActivity.this, LandingActivity.class));
                break;
        }

        return true;
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyBczFTaYkeG8kx-Wj3UAy09ropfVGNzOCI");

        Log.d("MapsActivity", "url = " + googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }


    public void requestLocationPermission() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}