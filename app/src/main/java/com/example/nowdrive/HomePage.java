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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    Polyline currentPoly;
    Button removePointsBtn;
    Button calRoutes;
    Switch locationSwitch;
    Boolean userLocation;
    Boolean isUserlocationChecked;
    Boolean locFlag;
    ProgressBar calBar;
    ArrayList<Marker> markers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setContentView(R.layout.activity_home_page);

        locFlag = false;
        userLocation = false;
        isUserlocationChecked = false;
        queue = Volley.newRequestQueue(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        removePointsBtn = findViewById(R.id.clearPointsBtn);
        calRoutes = findViewById(R.id.calRoutesBtn);
        locationSwitch = findViewById(R.id.locationSwitch);
        calBar = findViewById(R.id.calBar);

        removePointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removePoints();
            }
        });

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(!userLocation){
                    locationSwitch.setChecked(false);
                    Toast.makeText(HomePage.this, "Please enable location in device settings for this app.", Toast.LENGTH_LONG).show();
                } else {
                    isUserlocationChecked = isChecked;
                    if(isChecked){
                        locFlag = true;
                        removePoints();
                        MarkerOptions currentMark = new MarkerOptions().position(userLatLng).title("Position A");
                        Marker locMark = map.addMarker(currentMark);
                        markers.add(locMark);
                    } else {
                        locFlag = false;
                        removePoints();
                    }
                }
            }
        });

        calRoutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(markers.size()<2){
                    Toast.makeText(HomePage.this, "Two points must be placed!", Toast.LENGTH_LONG).show();
                } else {
                    Marker originMark = null;
                    Marker destMark = null;
                    for(int i=0;i<markers.size();i++){
                        if(i==0){
                            originMark = markers.get(i);
                        } else {
                            destMark = markers.get(i);
                        }
                    }
                    queue.cancelAll("Google Maps API Call");
                    calBar.setVisibility(View.VISIBLE);
                    StringRequest strReq = createStringRequest(originMark.getPosition().latitude,originMark.getPosition().longitude,destMark.getPosition().latitude,destMark.getPosition().longitude);
                    strReq.setTag("Google Maps API Call");
                    queue.add(strReq);
                    calBar.setVisibility(View.GONE);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(originMark.getPosition(), 13));
                }
            }
        });

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

    private void removePoints() {
        if(markers.size()==0&&!locFlag){
            Toast.makeText(HomePage.this, "No points on map!", Toast.LENGTH_LONG).show();
        } else {
            System.out.println("Size: "+markers.size());
            ArrayList<Marker> removeMarksList = new ArrayList<Marker>();
            for (int i=0;i<markers.size();i++){
                System.out.println("Index: "+i);
                Marker currentMark = markers.get(i);
                if(i==0&&isUserlocationChecked){
                    continue;
                }
                currentMark.remove();
                removeMarksList.add(currentMark);
            }
            markers.removeAll(removeMarksList);
            if(currentPoly!=null){
                currentPoly.remove();
            }
            }
        }

    private void intialiseMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 1);
        }

        if(userLocation){
            map.setMyLocationEnabled(true);
        }

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng point) {
                if(markers.size()<2){
                    MarkerOptions userMarker = new MarkerOptions().position(point);
                    if(markers.size()<1){
                        userMarker.title("Point A");
                    } else {
                        userMarker.title("Point B");
                    }
                    Marker userMark = map.addMarker(userMarker);
                    markers.add(userMark);
                    Toast.makeText(HomePage.this, "Point created!", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(HomePage.this, "Number of points exceeds 2 please clear the points.", Toast.LENGTH_LONG).show();
                }
            }
        });
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
                    userLocation = true;
                    Location location = task.getResult();
                    userLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                    intialiseMap();
                } else {
                    userLocation = false;
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
                            List<LatLng> cords= new ArrayList<LatLng>();
                            JSONObject result = new JSONObject(response);
                            JSONArray routes = result.getJSONArray("routes");
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyEncode = poly.getString("points");
                            cords = PolyUtil.decode(polyEncode);
                            polyOpt.addAll(cords).width(5).color(Color.RED).geodesic(true).jointType(JointType.ROUND);
                            currentPoly = map.addPolyline(polyOpt);
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
        intialiseMap();
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