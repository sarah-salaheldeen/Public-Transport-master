package com.example.publictransport;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;

public class MapSetup extends FragmentActivity implements OnMapReadyCallback {
    private static final int REQUEST_CODE = 101;

    private Location mLastLocation;
    private GoogleMap mMap;
    private LatLng mSourceOrDestLocation;
    private Context mContext;
    private SupportMapFragment mMapFragment;
    private AutocompleteSupportFragment mAutocompleteFragment;

    private ArrayList<PolylineOptions> mPolylineOptionsList;

    //MapSetup constructor
    public MapSetup(Context context, SupportMapFragment mapFragment, AutocompleteSupportFragment autocompleteFragmet) {
        mContext = context;
        mMapFragment = mapFragment;
        mAutocompleteFragment = autocompleteFragmet;
    }


    public void getLastLocation() {
        FusedLocationProviderClient mFusedLocationProviderClient;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: type casting context to activity (Activity) context can cause ClassCastException, so i must find a way to surround this by try/catch block
            ActivityCompat.requestPermissions((Activity) mContext, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            //return;
        }
        Task<Location> task = mFusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.i("MapSetup", "location is not nullll!!!");
                    mLastLocation = location;
                    Log.i("MapSetup", "mLastLocation: " + mLastLocation);
                    //Toast.makeText(mContext, mLastLocation.getLatitude() + "" + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                } else Log.i("MapSetup", "location is nullll!!!");
            }
        });
        task.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                mMapFragment.getMapAsync(MapSetup.this);
            }
        });
    }

    //Callback for the result from requesting permissions. This method is invoked for every call on requestPermissions(android.app.Activity, String[], int).
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    afterMapReady();
                }
                break;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //if there is a map and a polyline, add the polyline to the map
        //initialize the search box (AutocompleteFragment) and invoke afterMapReady() method which displays the current user location as a blue dot and move the camera to it.
        if (mMap != null && mPolylineOptionsList != null) {
            for (PolylineOptions polylineOptions : mPolylineOptionsList) {
                mMap.addPolyline(polylineOptions).setJointType(JointType.ROUND);
                //Polyline.setJointType(JointType.ROUND);
            }
        }
        initializeAutocompleteFragment();
        afterMapReady();

    }

    public void afterMapReady() {


        Log.i("MapSetup", "mLocation2: " + mLastLocation);

        /** display the blue dot on the current user location */
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }

        //get lat and long of the current location "mLastLocation", and move the camera to the current location
        Log.i("MapSetup", "mLastLocation value: " + mLastLocation);
        if (mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }

        //when the camera stops moving that means that the user has selected a location. now we can get that location and save it into mSourceOrDestLocation
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //if(mSourceOrDestLocation != null)
                //get the position [latlng] in the center of the map, which is also the position of the custom marker
                mSourceOrDestLocation = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
                Toast.makeText(mContext, mSourceOrDestLocation.latitude + "" + mSourceOrDestLocation.longitude, Toast.LENGTH_SHORT).show();

                Log.i("centerLat", String.valueOf(mMap.getCameraPosition().target.latitude));

                Log.i("centerLong", String.valueOf(mMap.getCameraPosition().target.longitude));

            }

        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

            }
        });
        //drawLine();
    }

    public LatLng getSourceOrDestLocation() {
        return mSourceOrDestLocation;
    }

    public void initializeAutocompleteFragment() {
// Initialize Places.
        if (!Places.isInitialized()) {
            Places.initialize(mContext, String.valueOf(R.string.google_maps_key));
        }

// Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(mContext);

        // Specify the types of place data to return.
        //autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        // Specify the fields to return.
        mAutocompleteFragment.setPlaceFields( Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        mAutocompleteFragment.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(15.5007, 32.5599),
                new LatLng(15.5007, 32.5599)));

        mAutocompleteFragment.setCountry("SDN");

// Set up a PlaceSelectionListener to handle the response.
        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng selectedPlace = place.getLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(selectedPlace));
                // TODO: Get info about the selected place.
                Log.i("MainActivity", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i("MainActivity", "An error occurred: " + status);
            }
        });
    }

    public void drawLine(ArrayList<PolylineOptions> polylineOptionsList) {
        mPolylineOptionsList = polylineOptionsList;
        if (mMap == null) {
            mMapFragment.getMapAsync(MapSetup.this);
        } else {
            for (PolylineOptions polylineOptions : mPolylineOptionsList) {
                mMap.addPolyline(polylineOptions).setJointType(JointType.ROUND);
                //Polyline.setJointType(JointType.ROUND);
            }
        }
    }
}
