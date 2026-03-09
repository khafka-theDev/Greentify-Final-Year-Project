package org.oss.greentify.RecycleStart;

import com.google.android.gms.maps.model.LatLng;

public class Facility {
    String name;
    LatLng location;
    String imageUrl;

    // Constructor accepting three arguments
    public Facility(String name, LatLng location, String imageUrl) {
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    // Constructor accepting four arguments (if you need distance)
    public Facility(String name, LatLng location, double distance, String imageUrl) {
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}