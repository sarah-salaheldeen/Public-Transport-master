package com.example.publictransport;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

import java.util.ArrayList;

public class StartJourny extends AppCompatActivity {

    MapSetup mapSetup;
    PlanJourney planJourney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //change the text on the actionBar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setTitle("Start your journey");

        Button startButton = findViewById(R.id.save_location_btn);
        startButton.setText(R.string.start_button);

        // Initialize the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);

        mapSetup = new MapSetup(this, mapFragment, autocompleteFragment);

        mapSetup.getLastLocation();

        drawLine();

        //mapSetup.drawLine();

        start();
    }

    public void drawLine(){
        String lineName = getIntent().getStringExtra("lineName");
        if (lineName != null) {
            //planJourney = new PlanJourney(lineName);
            /*ArrayList<PolylineOptions> polylineOptions = planJourney.getPoints();
            mapSetup.drawLine(polylineOptions);*/
        }

        String line1Name = getIntent().getStringExtra("line1Name");
        String line2Name = getIntent().getStringExtra("line2Name");
        if (line1Name != null && line2Name != null) {
            Log.d("StartJourney", "There is a line: " + line1Name);
           /* planJourney = new PlanJourney(line1Name, line2Name);
            ArrayList<PolylineOptions> polylineOptionsList = planJourney.getPoints();*/
            //Log.d("StartJourney", polylineOptions + " ");
            /*mapSetup.drawLine(polylineOptionsList);*/
        }
    }

    public void start(){
        Button startJourneyButton = findViewById(R.id.save_location_btn);
        startJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Todo do something
            }
        });
    }
}
