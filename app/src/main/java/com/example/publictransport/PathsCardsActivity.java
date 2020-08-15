package com.example.publictransport;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import org.neo4j.driver.types.Point;

public class PathsCardsActivity extends AppCompatActivity {

    ListView listView;
    ProgressBar progressBar;
    String TAG = "PathsCardsActivity";
    PlanJourney journey ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paths_cards);

        //change the text on the actionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("Possible paths");

        listView = findViewById(R.id.list);
        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        readObject();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void readObject() {
        LatLng sourceLocationLatLng = getIntent().getExtras().getParcelable("sourceLocation");
        Point sourceLocation = new Point() {
            @Override
            public int srid() {
                return 0;
            }

            @Override
            public double x() {
                return sourceLocationLatLng.longitude;
            }

            @Override
            public double y() {
                return sourceLocationLatLng.latitude;
            }

            @Override
            public double z() {
                return 0;
            }
        };

        LatLng destinationLocationLatLng = getIntent().getExtras().getParcelable("destinationLocation");
        Point destinationLocation = new Point() {
            @Override
            public int srid() {
                return 0;
            }

            @Override
            public double x() {
                return destinationLocationLatLng.longitude;
            }

            @Override
            public double y() {
                return destinationLocationLatLng.latitude;
            }

            @Override
            public double z() {
                return 0;
            }
        };

        //create an object of PlanJourney class
        //journey = new PlanJourney(sourceLocation, destinationLocation, this);

        //journey.readDataFromFirestore();
    }
}
