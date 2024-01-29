package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private Handler handler;
    private List<LatLng> markerPositions = new ArrayList<>();
    private int currentIndex = 0;
    private List<Circle> circles = new ArrayList<>();
    private int totalCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Set a default location
        LatLng defaultLocation = new LatLng(19.076090, 72.877426);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationRef = database.getReference();

        // Read data from Firebase and add markers to the map with delays
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    totalCoordinates=(int)dataSnapshot.getChildrenCount();
                    for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                        Double latitude = locationSnapshot.child("Lat").getValue(Double.class);
                        Double longitude = locationSnapshot.child("Long").getValue(Double.class);

                        if (latitude != null && longitude != null) {
                            LatLng location = new LatLng(latitude, longitude);
                            markerPositions.add(location);
                        }
                    }

                    addDot();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error fetching data from Firebase: " + databaseError.getMessage());
            }
        });
    }

    private void addDot() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentIndex < markerPositions.size()) {
                    LatLng currentLocation = markerPositions.get(currentIndex);
                    // Add a small-sized dot on the map
                    CircleOptions circleOptions = new CircleOptions()
                            .center(currentLocation)
                            .radius(2) // Radius in meters, adjust as needed
                            .fillColor(getResources().getColor(R.color.redDotColor)); // Use your desired color

                    Circle circle = googleMap.addCircle(circleOptions);
                    circles.add(circle);

                    animateCameraToDot(currentLocation);
                    currentIndex++;

                    // Display marker count with Toast
                    showMarkerCount();

                    addDot();
                }
            }
        }, 10); // Adjust the delay in milliseconds
    }

    private void animateCameraToDot(LatLng dotPosition) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dotPosition, 25));
    }

    private void showMarkerCount() {
        Toast.makeText(MainActivity.this, "All coordinates plotted: " + totalCoordinates, Toast.LENGTH_SHORT).show();
    }
}