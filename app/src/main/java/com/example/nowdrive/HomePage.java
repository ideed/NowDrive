package com.example.nowdrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    MapView mapView;
    GoogleMap map;
    RequestQueue queue;
    FusedLocationProviderClient mFusedLocationClient;
    LatLng userLatLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setContentView(R.layout.activity_home_page);

        queue = Volley.newRequestQueue(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void intialiseMap() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 1);
            return;
        }

        map.setMyLocationEnabled(true);

        MarkerOptions nciMarker = new MarkerOptions().position(new LatLng(53.349203, -6.242245)).title("NCI");
        MarkerOptions userMarker = new MarkerOptions().position(userLatLng).title("Current Location");
        MarkerOptions pointMarker =new MarkerOptions().position(new LatLng(53.348698,  -6.229743)).title("The Point");
        map.addMarker(nciMarker);
        map.addMarker(userMarker);
        map.addMarker(pointMarker);

        queue.cancelAll("Google Maps API Call");
        StringRequest strReq = createStringRequest(userLatLng.latitude,userLatLng.longitude,pointMarker.getPosition().latitude,pointMarker.getPosition().longitude);
        strReq.setTag("Google Maps API Call");
        queue.add(strReq);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13));
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 1);
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    userLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                    intialiseMap();
                }
            }
        });
    }

    private StringRequest createStringRequest(double originLat, double originLng, double destLat, double destLng) {
        String prefix = "https://maps.googleapis.com/maps/api/directions/json?";
        String origin = "origin="+originLat+","+originLng+"&";
        String dest = "destination="+destLat+","+destLng+"&";
        String key = "key="+getString(R.string.google_maps_key);

        String url = prefix+origin+dest+key;
        System.out.println("url: "+url);
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            PolylineOptions polyOpt = new PolylineOptions();
                            ArrayList<LatLng> cords= new ArrayList<LatLng>();
                            JSONObject result = new JSONObject(response);
                            JSONArray routes = result.getJSONArray("routes");
                            JSONObject route = routes.getJSONObject(0);
                            JSONArray legs = route.getJSONArray("legs");
                            JSONObject leg = legs.getJSONObject(0);
                            JSONArray steps = leg.getJSONArray("steps");
                            for(int i = 0;i < steps.length();i++){
                                JSONObject step = steps.getJSONObject(i);
                                System.out.println("step: "+step);
                                JSONObject start = step.getJSONObject("start_location");
                                JSONObject end = step.getJSONObject("end_location");
                                double startLat = start.getDouble("lat");
                                double startLng = start.getDouble("lng");
                                double endLat = end.getDouble("lat");
                                double endLng = end.getDouble("lng");
                                LatLng startFinal = new LatLng(startLat,startLng);
                                cords.add(startFinal);
                                LatLng endFinal = new LatLng(endLat,endLng);
                                cords.add(endFinal);
                            }
                            polyOpt.addAll(cords).width(5).color(Color.RED).geodesic(true).jointType(JointType.ROUND);
                            map.addPolyline(polyOpt);
                        } catch (JSONException e) {
                            System.out.println("Error: "+e.getMessage());
                            Toast.makeText(HomePage.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomePage.this, "Google Maps API not responding", Toast.LENGTH_LONG).show();
                        System.out.println("Error Response: "+error.getMessage());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                break;
            case R.id.nav_settings:
                startActivity(new Intent(HomePage.this, Settings.class));
                break;
            case R.id.nav_logout:
                Toast.makeText(HomePage.this, "Successfully logged out!", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                startActivity(new Intent(HomePage.this, MainActivity.class));
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        getLastKnownLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(HomePage.this, "Location Access Granted!", Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(getIntent());
                } else {
                    Toast.makeText(HomePage.this, "Please enable location services for the application in settings.", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }
}