package com.example.nowdrive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    MapView mapView;
    GoogleMap map;
    RequestQueue queue;
    RequestQueue queueTwo;
    FusedLocationProviderClient mFusedLocationClient;
    LatLng userLatLng;
    Polyline currentPoly;
    SearchView routeSearch;
    Button removePointsBtn;
    Button calRoutes;
    Button saveRoute;
    Button prevRoute;
    Switch locationSwitch;
    Switch tollSwitch;
    Switch highwaySwitch;
    Boolean userLocation;
    Boolean isUserlocationChecked;
    Boolean locFlag;
    Boolean searchFlag;
    ProgressBar calBar;
    ListView routesView;

    Deque<Route> previousRoutes = new ArrayDeque<Route>();
    ArrayAdapter<String> adapter;
    ArrayList<Route> gatheredRoutes = new ArrayList<Route>();
    ArrayList<Route> DBRoutes = new ArrayList<Route>();
    ArrayList<String> DBRoutesName = new ArrayList<String>();
    ArrayList<Marker> markers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setContentView(R.layout.activity_home_page);

        locFlag = false;
        searchFlag = false;
        userLocation = false;
        isUserlocationChecked = false;
        queue = Volley.newRequestQueue(this);
        queueTwo = Volley.newRequestQueue(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        routeSearch = findViewById(R.id.route_search);
        removePointsBtn = findViewById(R.id.clearPointsBtn);
        prevRoute = findViewById(R.id.previousRouteBtn);
        saveRoute = findViewById(R.id.save_route_btn);
        calRoutes = findViewById(R.id.calRoutesBtn);
        locationSwitch = findViewById(R.id.locationSwitch);
        tollSwitch = findViewById(R.id.toll_switch);
        highwaySwitch = findViewById(R.id.highway_switch);
        calBar = findViewById(R.id.calBar);
        routesView = findViewById(R.id.routes_view);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        removePointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removePoints();
            }
        });

        routeSearch.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    routesView.setVisibility(View.VISIBLE);
                } else {
                    routesView.setVisibility(View.GONE);
                    DBRoutesName.clear();
                }
            }
        });

        routeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBRoutes.clear();
                DBRoutesName.clear();
                routeSearch.setIconified(false);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Routes");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot postSnap: snapshot.getChildren()){
                            Route collectedRoute = postSnap.getValue(Route.class);
                            DBRoutes.add(collectedRoute);
                            DBRoutesName.add(collectedRoute.routeName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomePage.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                adapter = new ArrayAdapter<String>(HomePage.this, android.R.layout.simple_list_item_1, DBRoutesName);
                routesView.setAdapter(adapter);
                routesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String route = (String) routesView.getItemAtPosition(i);
                        Route currentRoute = null;
                        for (int j=0;j<DBRoutes.size();j++){
                            if(DBRoutes.get(j).routeName.equals(route)){
                                currentRoute = DBRoutes.get(j);
                            }
                        }
                        if (currentRoute == null){
                            Toast.makeText(HomePage.this, "Error in gathering saved route.", Toast.LENGTH_LONG).show();
                        } else {
                            if(searchFlag){
                                removePoints();
                                searchFlag = false;
                            } else {
                                searchFlag = true;
                                removePoints();
                                searchFlag = false;
                                PolylineOptions polyOpt = new PolylineOptions();
                                List<LatLng> cords= new ArrayList<LatLng>();

                                Boolean avoidHighways = Boolean.parseBoolean(currentRoute.avoidHighways);
                                Boolean avoidTolls = Boolean.parseBoolean(currentRoute.avoidTolls);

                                locationSwitch.setChecked(false);
                                highwaySwitch.setChecked(avoidHighways);
                                tollSwitch.setChecked(avoidTolls);

                                double originLat = Double.parseDouble(currentRoute.originLat);
                                double originLng = Double.parseDouble(currentRoute.originLng);
                                LatLng origin = new LatLng(originLat,originLng);

                                double destLat = Double.parseDouble(currentRoute.destLat);
                                double destLng = Double.parseDouble(currentRoute.destLng);
                                LatLng dest = new LatLng(destLat,destLng);

                                MarkerOptions originOpt = new MarkerOptions().position(origin).title("Position A");
                                MarkerOptions destOpt = new MarkerOptions().position(dest).title("Position B");

                                Marker originMark = map.addMarker(originOpt);
                                markers.add(originMark);
                                Marker destMark = map.addMarker(destOpt);
                                markers.add(destMark);

                                cords = PolyUtil.decode(currentRoute.encodedPolyLine);
                                polyOpt.addAll(cords).width(5).color(Color.RED).geodesic(true).jointType(JointType.ROUND);
                                currentPoly = map.addPolyline(polyOpt);
                                routeSearch.clearFocus();
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(originMark.getPosition(), 13));
                            }
                        }
                    }
                });

            }
        });

        routesView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Routes");
                String route = (String) routesView.getItemAtPosition(i);
                Task<DataSnapshot> task= ref.get();
                task.addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        Boolean nameFlag = false;
                        DataSnapshot sc = dataSnapshot;
                        for (DataSnapshot child : sc.getChildren()) {
                            if(child.child("routeName").getValue().equals(route)){
                                child.getRef().removeValue();
                                routeSearch.clearFocus();
                                DBRoutesName.clear();
                                routesView.setVisibility(View.GONE);
                                Toast.makeText(HomePage.this, "Route removed!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomePage.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                searchFlag = true;
                return false;
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
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.cal_route_popup, null, false);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;

                PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                popupWindow.setOutsideTouchable(true);

                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                popupWindow.getContentView().setVisibility(View.GONE);

                if(currentPoly!=null){
                    currentPoly.remove();
                }
                if(markers.size()<2){
                    Toast.makeText(HomePage.this, "Two points must be placed!", Toast.LENGTH_LONG).show();
                    popupWindow.dismiss();
                } else {
                    gatheredRoutes.clear();
                    Marker originMark = null;
                    Marker destMark = null;
                    for(int i=0;i<markers.size();i++){
                        if(i==0){
                            originMark = markers.get(i);
                        } else {
                            destMark = markers.get(i);
                        }
                    }
                    queue.cancelAll("Here Directions API Call");
                    calBar.setVisibility(View.VISIBLE);
                    ArrayList<StringRequest> strReqs = createStringRequest(originMark.getPosition().latitude,originMark.getPosition().longitude,destMark.getPosition().latitude,destMark.getPosition().longitude, false);

                    for (int i=0;i<strReqs.size();i++){
                        StringRequest strReq = strReqs.get(i);
                        strReq.setTag("Here Directions API Call");
                        queue.add(strReq);
                    }

                    Marker finalOriginMark = originMark;
                    Marker finalDestMark = destMark;
                    queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                        @Override
                        public void onRequestFinished(Request<Object> request) {
                            ArrayList<StringRequest> finalStrReqs = new ArrayList<StringRequest>();
                            finalStrReqs = createStringRequest(finalOriginMark.getPosition().latitude, finalOriginMark.getPosition().longitude, finalDestMark.getPosition().latitude, finalDestMark.getPosition().longitude, true);
                            if(finalStrReqs.isEmpty()){
                                Route selectedRoute = gatheredRoutes.get(0);
                                calBar.setVisibility(View.GONE);
                                previousRoutes.add(selectedRoute);

                                PolylineOptions polyOpt = new PolylineOptions().addAll(PolyUtil.decode(selectedRoute.encodedPolyLine)).width(5).color(Color.RED).geodesic(true).jointType(JointType.ROUND);
                                currentPoly = map.addPolyline(polyOpt);
                                popupWindow.dismiss();
                            } else {
                                queueTwo.cancelAll("Here Directions API Call");
                                for (StringRequest strReq: finalStrReqs){
                                    strReq.setTag("Here Directions API Call");
                                    queueTwo.add(strReq);
                                }
                                queueTwo.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                                    @Override
                                    public void onRequestFinished(Request<Object> request) {
                                        if (gatheredRoutes.size()>2){
                                            popupWindow.getContentView().setVisibility(View.VISIBLE);
                                            popupView.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    popupWindow.dismiss();
                                                }
                                            });

                                            ListView routeView = popupView.findViewById(R.id.route_list);
                                            ArrayList<String> routeNames = new ArrayList<String>();

                                            for(int i=0;i<gatheredRoutes.size();i++){
                                                routeNames.add("Route "+(i+1));
                                            }

                                            ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(HomePage.this, android.R.layout.simple_list_item_1, routeNames);
                                            routeView.setAdapter(routeAdapter);
                                            calBar.setVisibility(View.GONE);

                                            routeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                    Route selectedRoute = gatheredRoutes.get(i);
                                                    previousRoutes.add(selectedRoute);

                                                    PolylineOptions polyOpt = new PolylineOptions().addAll(PolyUtil.decode(selectedRoute.encodedPolyLine)).width(5).color(Color.RED).geodesic(true).jointType(JointType.ROUND);
                                                    currentPoly = map.addPolyline(polyOpt);
                                                    routeNames.clear();
                                                    popupWindow.dismiss();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(originMark.getPosition(), 13));
                }
            }
        });

        saveRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentPoly==null){
                    Toast.makeText(HomePage.this, "A route must be plotted in order to be saved!", Toast.LENGTH_LONG).show();
                } else {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.save_route_popup, null);
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = true;

                    PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                    popupView.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popupWindow.dismiss();
                        }
                    });

                    popupView.findViewById(R.id.submit_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TextView routeName = popupView.findViewById(R.id.route_name_input);
                            if(routeName.getText().toString().isEmpty()){
                                Toast.makeText(HomePage.this,"A name for the route must be assigned!", Toast.LENGTH_LONG).show();
                            } else {
                                ProgressBar progBar = popupView.findViewById(R.id.prog_bar);
                                String route_name = routeName.getText().toString();
                                String originLat = ""+markers.get(0).getPosition().latitude;
                                String originLng = ""+markers.get(0).getPosition().longitude;
                                String destLat = ""+markers.get(1).getPosition().latitude;
                                String destLng = ""+markers.get(1).getPosition().longitude;
                                String encodedPolyline = PolyUtil.encode(currentPoly.getPoints());
                                String avoidHighways = ""+highwaySwitch.isChecked();
                                String avoidTolls = ""+tollSwitch.isChecked();

                                progBar.setVisibility(View.VISIBLE);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("Routes");

                                Task<DataSnapshot> task= ref.get();
                                task.addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                    @Override
                                    public void onSuccess(DataSnapshot dataSnapshot) {
                                        Boolean nameFlag = false;
                                        DataSnapshot sc = dataSnapshot;
                                        for (DataSnapshot child : sc.getChildren()) {
                                            if(child.child("routeName").getValue().equals(route_name)){
                                                System.out.println("Value: "+child.getValue());
                                                Toast.makeText(HomePage.this, "Error: Route name already exists, please make a new one.", Toast.LENGTH_LONG).show();
                                                nameFlag = true;
                                            }
                                        }

                                        if(!nameFlag){
                                            Route newRoute = new Route(route_name, originLat, originLng, destLat, destLng, encodedPolyline, avoidHighways, avoidTolls);
                                            FirebaseDatabase.getInstance().getReference("Users")
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .child("Routes").child(ref.push().getKey()).setValue(newRoute).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(HomePage.this, "Route Saved!", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(HomePage.this, "Error: Route cannot be saved!", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(HomePage.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });

                                progBar.setVisibility(View.GONE);
                                popupWindow.dismiss();
                            }
                        }
                    });
                }
            }
        });

        prevRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(previousRoutes.isEmpty()){
                    Toast.makeText(HomePage.this, "There are no previous routes!", Toast.LENGTH_LONG).show();
                } else {
                    Route prev = previousRoutes.pop();
                    if(previousRoutes.isEmpty()){
                        Toast.makeText(HomePage.this, "There are no previous routes!", Toast.LENGTH_LONG).show();
                    } else {
                        locationSwitch.setChecked(false);
                        locFlag = false;
                        searchFlag = true;
                        removePoints();
                        searchFlag = false;

                        double originLat = Double.parseDouble(prev.originLat);
                        double originLng = Double.parseDouble(prev.originLng);
                        LatLng origin = new LatLng(originLat,originLng);

                        double destLat = Double.parseDouble(prev.destLat);
                        double destLng = Double.parseDouble(prev.destLng);
                        LatLng dest = new LatLng(destLat,destLng);

                        MarkerOptions originOpt = new MarkerOptions().position(origin).title("Position A");
                        MarkerOptions destOpt = new MarkerOptions().position(dest).title("Position B");

                        Marker originMark = map.addMarker(originOpt);
                        markers.add(originMark);
                        Marker destMark = map.addMarker(destOpt);
                        markers.add(destMark);

                        List<LatLng> cords= new ArrayList<LatLng>();
                        PolylineOptions polyOpt = new PolylineOptions();
                        cords = PolyUtil.decode(prev.encodedPolyLine);
                        polyOpt.addAll(cords).width(5).color(Color.RED).geodesic(true).jointType(JointType.ROUND);
                        currentPoly = map.addPolyline(polyOpt);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(originMark.getPosition(), 13));
                    }
                }
            }
        });

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        routeSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                routesView.setVisibility(View.GONE);
                routeSearch.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                routesView.setVisibility(View.VISIBLE);
                return false;
            }
        });

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
        if(markers.size()==0&&!locFlag&&!searchFlag){
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
            Boolean checker = currentPoly==null;
            if(currentPoly!=null){
                if (locationSwitch.isChecked()&&markers.size()!=0){
                    currentPoly.remove();
                    map.clear();
                    MarkerOptions currMarker = new MarkerOptions().position(markers.get(0).getPosition()).title("Position A");
                    markers.clear();
                    Marker newMarker = map.addMarker(currMarker);
                    markers.add(newMarker);
                }
                else {
                    currentPoly.remove();
                    map.clear();
                    markers.clear();
                }
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

    private ArrayList<StringRequest> createStringRequest(double originLat, double originLng, double destLat, double destLng, boolean routeFlag) {
        ArrayList<StringRequest> strArr = new ArrayList<StringRequest>();
        String prefix = "https://router.hereapi.com/v8/routes?transportMode=car&";
        String origin = "origin="+originLat+","+originLng+"&";
        String dest = "destination="+destLat+","+destLng+"&";
        String avoid = "";
        String avoidFeatures = "";
        String expect = "return=polyline&";
        String key = "apikey="+getString(R.string.here_api_key);


        System.out.println("Highway: "+highwaySwitch.isChecked()+" Toll: "+tollSwitch.isChecked());
        if(highwaySwitch.isChecked()){
            avoidFeatures = "avoid[features]=controlledAccessHighway";
        }
        if(tollSwitch.isChecked()){
            if(avoidFeatures.equals("")){
                avoidFeatures = "avoid[features]=tollroad";
            }
            else {
                avoidFeatures += ",tollroad";
            }
        }
        if(!avoidFeatures.equals("")){
            avoidFeatures += "&";
        }

        if(routeFlag) {
            ArrayList<Accident> accidents = new ArrayList<Accident>();
            try {
                JSONObject  jsonObject = new JSONObject(getJSONAsset());
                JSONArray jsonArray = jsonObject.getJSONArray("accidents");
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject accident = jsonArray.getJSONObject(i);
                    double cordLat = accident.getDouble("cordLat");
                    double cordLng = accident.getDouble("cordLng");
                    int minor = accident.getInt("minor")*2;
                    int serious = accident.getInt("serious")*6;
                    int fatal = accident.getInt("fatal")*10;
                    int weight = minor+serious+fatal;
                    Accident newAccident = new Accident(cordLat,cordLng,weight);
                    accidents.add(newAccident);
                }
            } catch (JSONException e){
                e.printStackTrace();
            }

            Boolean routeCheck = false;

            for(Accident accident: accidents){
                LatLng pos = new LatLng(accident.cordLat,accident.cordLng);
                if(PolyUtil.isLocationOnEdge(pos, PolyUtil.decode(gatheredRoutes.get(0).encodedPolyLine), true, 15)){
                    routeCheck = true;
                }
            }

            if(routeCheck) {
                avoid = "avoid[areas]=";
                for (Accident accident: accidents){
                    if(accident.weight<20){
                        if(avoid.equals("avoid[areas]=")){
                            avoid += "bbox:"+(accident.cordLng-0.000188)+","+(accident.cordLat+0.000171)+","+(accident.cordLng+0.000204)+","+(accident.cordLat-0.000194);
                        } else {
                            avoid += "|bbox:"+(accident.cordLng-0.000188)+","+(accident.cordLat+0.000171)+","+(accident.cordLng+0.000204)+","+(accident.cordLat-0.000194);
                        }
                    }
                }
                avoid += "&";
                String mediumUrl = prefix+origin+dest+avoid+avoidFeatures+expect+key;
                strArr.add(sendRequest(mediumUrl, originLat, originLng, destLat, destLng));

                avoid = "avoid[areas]=";
                for (Accident accident: accidents){
                    if(avoid.equals("avoid[areas]=")){
                        avoid += "bbox:"+(accident.cordLng-0.000188)+","+(accident.cordLat+0.000171)+","+(accident.cordLng+0.000204)+","+(accident.cordLat-0.000194);
                    } else {
                        avoid += "|bbox:"+(accident.cordLng-0.000188)+","+(accident.cordLat+0.000171)+","+(accident.cordLng+0.000204)+","+(accident.cordLat-0.000194);
                    }
                }
                avoid += "&";
                String highURl = prefix+origin+dest+avoid+avoidFeatures+expect+key;
                strArr.add(sendRequest(highURl, originLat, originLng, destLat, destLng));
            }
        } else {
            String url = prefix+origin+dest+avoid+avoidFeatures+expect+key;

            System.out.println("url: "+url);

            StringRequest req = sendRequest(url, originLat, originLng, destLat, destLng);

            strArr.add(req);
        }
        return strArr;
    }

    private String getJSONAsset() {
        String json=null;
        try {
            InputStream inputStream = getAssets().open("dockland_accidents.json");
            int sizeOfFile = inputStream.available();
            byte[] bufferData = new byte[sizeOfFile];
            inputStream.read(bufferData);
            inputStream.close();
            json = new String(bufferData, "UTF-8");
        } catch (IOException e){
             e.printStackTrace();
             return null;
        }
        return json;
    }

    private StringRequest sendRequest(String url, double originLat, double originLng, double destLat, double destLng) {
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
                            JSONArray sections = route.getJSONArray("sections");
                            JSONObject section = sections.getJSONObject(0);

                            String polyEncode = section.getString("polyline");

                            List<flexiableDecoder.LatLngZ> test = flexiableDecoder.decode(polyEncode);

                            for(int i=0;i<test.size();i++){
                                flexiableDecoder.LatLngZ testLatLngZ = test.get(i);
                                LatLng newLatLng = new LatLng(testLatLngZ.lat,testLatLngZ.lng);
                                cords.add(newLatLng);
                            }

                            Route gatheredRoute = new Route("Previous",originLat+"",originLng+"",destLat+"",destLng+"",PolyUtil.encode(cords),highwaySwitch.isChecked()+"",tollSwitch.isChecked()+"");
                            gatheredRoutes.add(gatheredRoute);

                        } catch (JSONException e) {
                            System.out.println("Error: "+e.getMessage());
                            Toast.makeText(HomePage.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomePage.this, "Here Directions API not responding", Toast.LENGTH_LONG).show();
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
        intialiseMap();
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