package com.example.publictransport;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.neo4j.driver.types.Point;

import java.util.ArrayList;

public class PlanJourney {

    //private DocumentReference mDocRef = FirebaseFirestore.getInstance().document("lines/al-siteen");
    private Point mSourceLocation;
    private Point mDestinationLocation;
    private String TAG = "PlanJourney";
    private Context mContext;
    DatabaseQuery databaseQuery;
    private String mLine1Name;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference stopsCollRef = db.collection("stops");
    private CollectionReference linesCollRef = db.collection("lines");
    private GeoFirestore geoFirestore = new GeoFirestore(stopsCollRef);
    //private GeoQuery geoQuery;

    //private CollectionReference linePointsCollRef = db.collection("lines/points");

    private ArrayList<String> departureStopsList;
    private ArrayList<String> arrivalStopsList;

    private ArrayList<Line> departureStopsLinesList;
    private ArrayList<Line> arrivalStopsLinesList;
    private ArrayList<Line> possibleLinesList;

    private ProgressBar progressBar;
    private TextView textView;
    private String mLine2Name;


    public PlanJourney(Point startLocation, Point endLocation, Context context) {
        mSourceLocation = startLocation;
        mDestinationLocation = endLocation;
        mContext = context;
    }

    public PlanJourney(String lineName){
        mLine1Name = lineName;
    }

    public PlanJourney(String line1Name, String line2Name) {
        mLine1Name = line1Name;
        mLine2Name = line2Name;
    }

    public void readDataFromFirestore() {

        departureStopsList = new ArrayList<>();
        arrivalStopsList = new ArrayList<>();

        databaseQuery = new DatabaseQuery(mSourceLocation, mDestinationLocation, mContext);
        databaseQuery.queryLines();

    }


    public ArrayList<PolylineOptions> getPoints() {

        ArrayList<PolylineOptions> polylineOptionsList = new ArrayList<>();
        PolylineOptions polylineOptions = new PolylineOptions();
        PolylineOptions polylineOptions2 = new PolylineOptions();

        linesCollRef.whereEqualTo("line_name", mLine1Name)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Log.d(TAG, "*******doc1: " + doc.getId());
                            ArrayList<GeoPoint> pointsArray = (ArrayList<GeoPoint>) doc.get("points");

                            if (pointsArray != null) {
                                for (int i = 0; i < pointsArray.size(); i++) {
                                            /*String[] latLng =  pointsArray.get(i).split(",");
                                            double latitude = Double.parseDouble(latLng[0]);
                                            double longitude = Double.parseDouble(latLng[1]);*/
                                    LatLng point = new LatLng(pointsArray.get(i).getLatitude(), pointsArray.get(i).getLongitude());
                                    Log.d(TAG, point.latitude + " , " + point.longitude);
                                    polylineOptions.add(point);
                                }
                        }

                                        polylineOptions
                                                .width(10)
                                                .color(Color.BLUE)
                                                .geodesic(true);
                                    }
                                }
                                });

        linesCollRef.whereEqualTo("line_name", mLine2Name)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Log.d(TAG, "*******doc2: " + doc.getId());
                            ArrayList<GeoPoint> pointsArray2 = (ArrayList<GeoPoint>) doc.get("points");

                            if (pointsArray2 != null) {
                                for (int i = 0; i < pointsArray2.size(); i++) {
                                            /*String[] latLng =  pointsArray.get(i).split(",");
                                            double latitude = Double.parseDouble(latLng[0]);
                                            double longitude = Double.parseDouble(latLng[1]);*/
                                    LatLng point = new LatLng(pointsArray2.get(i).getLatitude(), pointsArray2.get(i).getLongitude());
                                    Log.d(TAG, point.latitude + " , " + point.longitude);
                                    polylineOptions2.add(point);
                                }
                            }

                            polylineOptions2
                                    .width(10)
                                    .color(Color.BLUE)
                                    .geodesic(true);
                        }
                    }
                });

        polylineOptionsList.add(polylineOptions);

        polylineOptionsList.add(polylineOptions2);

        return polylineOptionsList;
    }
}
        /*public void removeListeners(){
            geoQuery.removeAllListeners();
        }*/
