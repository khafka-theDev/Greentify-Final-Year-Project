package org.oss.greentify.RecycleStart;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.oss.greentify.R;

import java.util.ArrayList;
import java.util.List;

public class LocationSelectionActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private ImageView locationImageView;
    private TextView locationName;
    private Button chooseLocationButton;
    private View locationDetailsContainer;

    private String selectedType;
    private List<Facility> facilities = new ArrayList<>();

    private static final String PREFS_NAME = "GreentifyPrefs";
    private static final String KEY_HAS_SEEN_LOCATION_WALKTHROUGH = "hasSeenLocationWalkthrough";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selection);

        locationImageView = findViewById(R.id.locationImageView);
        locationName = findViewById(R.id.locationName);
        chooseLocationButton = findViewById(R.id.chooseLocationButton);
        locationDetailsContainer = findViewById(R.id.locationDetailsContainer);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        selectedType = getIntent().getStringExtra("type");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        chooseLocationButton.setOnClickListener(v -> {
            if (locationDetailsContainer.getVisibility() == View.VISIBLE) {
                String locationNameText = locationName.getText().toString();
                String imageUrl = (String) chooseLocationButton.getTag();

                Intent intent = new Intent(LocationSelectionActivity.this, UploadRecycleActivity.class);
                intent.putExtra("location", locationNameText);
                intent.putExtra("type", selectedType);
                intent.putExtra("imageUrl", imageUrl);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermissionAndEnable();
    }

    private void checkLocationPermissionAndEnable() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            enableUserLocation();
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f));
                    loadNearbyFacilities(userLatLng);
                } else {
                    Toast.makeText(this, "Unable to detect current location. Please ensure GPS is enabled.", Toast.LENGTH_LONG).show();
                    Log.e("LocationSelection", "getLastLocation() returned null");
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot access current location.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadNearbyFacilities(LatLng userLatLng) {
        facilities.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recycling_centers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            for (var doc : result.getDocuments()) {
                                String name = doc.getString("name");
                                Double latitude = doc.getDouble("latitude");
                                Double longitude = doc.getDouble("longitude");
                                String imageUrl = doc.getString("imageUrl");

                                if (latitude != null && longitude != null) {
                                    LatLng facilityLocation = new LatLng(latitude, longitude);
                                    mMap.addMarker(new MarkerOptions().position(facilityLocation).title(name));
                                    facilities.add(new Facility(name, facilityLocation, imageUrl));
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error fetching facilities", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error fetching documents", task.getException());
                    }
                });

        mMap.setOnMarkerClickListener(marker -> {
            String facilityName = marker.getTitle();
            for (Facility facility : facilities) {
                if (facility.getName().equals(facilityName)) {
                    locationDetailsContainer.setVisibility(View.VISIBLE);
                    locationName.setText(facility.getName());
                    Picasso.get().load(facility.getImageUrl()).into(locationImageView);
                    chooseLocationButton.setTag(facility.getImageUrl());
                    break;
                }
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasSeenLocationWalkthrough = prefs.getBoolean(KEY_HAS_SEEN_LOCATION_WALKTHROUGH, false);

        if (!hasSeenLocationWalkthrough) {
            new android.os.Handler().postDelayed(() -> {
                startLocationWalkthrough();
                prefs.edit().putBoolean(KEY_HAS_SEEN_LOCATION_WALKTHROUGH, true).apply();
            }, 1000); // Delay to ensure map & UI are ready
        }
    }

    private void startLocationWalkthrough() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.map), "Pick a Facility", "Tap on any red marker on the map to view its location details.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showSelectButtonGuide();
                    }
                });
    }

    private void showSelectButtonGuide() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.chooseLocationButton), "Select Location", "After selecting a facility, tap this button to continue.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .cancelable(true));
    }

}
