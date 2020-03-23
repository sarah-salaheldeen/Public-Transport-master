package com.example.publictransport;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

public class SourceActivity extends AppCompatActivity {

    MapSetup mapSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //change the text on the actionBar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setTitle("Source");

        /** initializing the Map */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        /** Initialize the Autocomplete search box . */
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);

        //when we go back from destination activity to source activity => save destination location
        if (getCallingActivity() != null) {
            Log.i("SourceActivity", "getCallingActivity() != null");
            if(getCallingActivity().getClassName().equals("DestinationActivity")){
                Intent i = getIntent();
                LatLng ll = i.getParcelableExtra("longLat_dataPrivider");
            }
        }

        else {
            Log.i("SourceActivity", "getCallingActivity() = null");
            //setting up google map
            mapSetup = new MapSetup(this, mapFragment, autocompleteFragment);

            //get user's last location
            mapSetup.getLastLocation();

            saveLocation();
        }
    }

    //save source location and start DestinationActivity
    public void saveLocation(){
        Button saveLocationButton = findViewById(R.id.save_location_btn);
        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SourceActivity", "mapSetup.getSourceOrDestLocation()" + mapSetup.getSourceOrDestLocation());
                LatLng sourceOrDestLocation = mapSetup.getSourceOrDestLocation();

                if(sourceOrDestLocation != null) {
                    Bundle args = new Bundle();
                    args.putParcelable("EXTRA_SOURCE_LOCATION", sourceOrDestLocation);
                    Intent intent = new Intent(SourceActivity.this, DestinationActivity.class);
                    intent.putExtras(args);
                    //ActivityCompat.startActivityForResult(SourceActivity.this, intent, 0 , null);
                    //startActivityForResult(intent, 0, null);
                    startActivity(intent);
                }
            }
        });
    }
}
