package com.example.publictransport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseQueryOld {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private GeoPoint mSourceLocation;
    private GeoPoint mDestinationLocation;
    private Boolean isSource = true;
    private String TAG = "DatabaseQueryOld";
    private Context mContext;
    private ProgressBar progressBar;
    private CollectionReference stopsCollRef = db.collection("stops");
    private CollectionReference linesCollRef = db.collection("lines");
    private GeoFirestore geoFirestore = new GeoFirestore(stopsCollRef);

    private GeoPoint mLocation;

    private ArrayList<String> departureStopsList = new ArrayList<>();
    private ArrayList<String> arrivalStopsList = new ArrayList<>();

    private ArrayList<Line> linesList;
    private ArrayList<Line> possibleLinesList;
    private ArrayList<Line> mPossibleLinesList;
    private ArrayList<Line> departureStopsLinesList = new ArrayList<>();
    private ArrayList<Line> arrivalStopsLinesList = new ArrayList<>();

    private String stop_id;
    private ArrayList<String> mStopsList;

    private ArrayList<Line> mLines;
    private ArrayList<Line> finalLines;

    private TextView textView;

    public DatabaseQueryOld(GeoPoint sourceLocation, GeoPoint destinationLocation, Context context) {
        mSourceLocation = sourceLocation;
        mDestinationLocation = destinationLocation;
        mContext = context;
    }

    public void queryStops() {
        if (isSource) {
            mLocation = mSourceLocation;
        } else mLocation = mDestinationLocation;

        //get the nearest stop from the user location (mSourceLocation) with radius of 0.5 kilometers.
        geoFirestore.queryAtLocation(mLocation, 0.5)
                .addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                    @Override
                    public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                        //when a stop is found, add it to departureStopsList
                        if (mLocation == mSourceLocation) {
                            departureStopsList.add(documentSnapshot.getId());
                            Log.d(TAG, "Document entered! departureStopsList: " + departureStopsList);
                        } else if (mLocation == mDestinationLocation) {
                            arrivalStopsList.add(documentSnapshot.getId());
                            Log.d(TAG, "Document entered! arrivalStopsList: " + arrivalStopsList);
                        }
                    }

                    @Override
                    public void onDocumentExited(DocumentSnapshot documentSnapshot) {
                    }

                    @Override
                    public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                    }

                    @Override
                    public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                    }

                    @Override
                    public void onGeoQueryReady() {
                        if (mLocation == mSourceLocation) {
                            isSource = false;
                            Log.d(TAG, "this is the line before calling queryStops for the second time. mLocation value is: " + mLocation);
                            queryStops();
                        }
                        if (departureStopsList != null && arrivalStopsList != null) {
                            isSource = true;
                            queryLines();
                        }


                        //if there is no stops nearby the user, try and get the street name and see if there is any transport lines in it.
                        else if (departureStopsList == null) {
                            Geocoder geocoder = new Geocoder(mContext);
                            List<Address> addresses;
                            try {
                                addresses = geocoder.getFromLocation(mSourceLocation.getLatitude(), mSourceLocation.getLongitude(), 1);
                                Log.d(TAG, "Street name is: " + addresses);
                            } catch (IOException ioException) {
                                // Catch network or other I/O problems.
                                String errorMessage = "service not available";
                                Log.e(TAG, errorMessage, ioException);
                            }
                        }
                    }

                    @Override
                    public void onGeoQueryError(Exception e) {
                    }
                });
    }

    private void queryLines() {
        if (!arrivalStopsList.isEmpty() && !departureStopsList.isEmpty()) {
            if (isSource) {
                stop_id = "departure_stop_id";
                mStopsList = departureStopsList;
                linesList = departureStopsLinesList;
            } else {
                stop_id = "arrival_stop_id";
                mStopsList = arrivalStopsList;
                linesList = arrivalStopsLinesList;
            }
            //find the transport lines where the departure_stop_id is in that departureStopsList, which means the lines are in the stop near to the user location
            linesCollRef.whereIn(stop_id, mStopsList)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            //add each line you find to the departureStopsLinesList
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Log.d(TAG, "lines: " + doc.getId());
                                Line line = doc.toObject(Line.class);
                                //if (isSource)
                                linesList.add(line);
                            }
                            if (isSource) {
                                departureStopsLinesList = linesList;
                                isSource = false;
                                queryLines();
                            } else arrivalStopsLinesList = linesList;


                            if (!departureStopsLinesList.isEmpty() && !arrivalStopsLinesList.isEmpty()) {
                                possibleLinesList();
                                Log.d(TAG, "departureStopsLinesList: " + departureStopsLinesList.get(0).getLine_name() + " arrivalStopsLinesList: " + arrivalStopsLinesList.get(0).getLine_name());
                            }
                        }
                    });
        }
    }

    private void possibleLinesList() {
        mPossibleLinesList = new ArrayList<>();
        //Todo nested loops is not the best way (performance-wise) with array lists ...
        //get the lines where their departure stop is near the user's current location
        // and their arrival stop is near to the user's destination location
        // and put them in the possibleLinesList
        for (Line line : departureStopsLinesList) {
            for (Line line2 : arrivalStopsLinesList) {
                if (line.getLine_name().equals(line2.getLine_name())) {
                    mPossibleLinesList.add(line);
                }
            }
        }
        //Log.d(TAG, "mPossibleLinesList: " + mPossibleLinesList.get(0).getLine_name());
        if (!mPossibleLinesList.isEmpty()) {
            displayLinesOnScreen(mPossibleLinesList, null);
        } else {
            Log.d(TAG, "linesArrayList is null");
            for (Line departureLine : departureStopsLinesList) {
                String arrival_line_stop = departureLine.getArrival_stop_id();
                linesCollRef.whereEqualTo("departure_stop_id", arrival_line_stop).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        finalLines = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Line line = doc.toObject(Line.class);
                            String arrival_stop = line.getArrival_stop_id();
                            Log.d(TAG, arrival_stop);

                            stopsCollRef.whereEqualTo(FieldPath.documentId(), arrival_stop).addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                        Log.d(TAG, "final stop: " + doc.getId());
                                        if (arrivalStopsList.contains(doc.getId())) {
                                            //Log.d(TAG, "line1: " + departureLineName + ", line2: " + line.getLine_name());
                                            Line finalPath = new Line(doc.getId(), departureLine.getDeparture_stop_id(), 10, 1, departureLine.getLine_name() + " - " + line.getLine_name(), null);
                                            finalLines.add(finalPath);

                                        }
                                    }
                                    if (!finalLines.isEmpty()) {
                                        displayLinesOnScreen(finalLines, departureLine);
                                    } else {
                                        if (mContext instanceof Activity) {
                                            Activity activity = (Activity) mContext;
                                            progressBar = activity.findViewById(R.id.progress_bar);
                                            progressBar.setVisibility(View.GONE);
                                            textView = activity.findViewById(R.id.text_view);
                                            textView.setVisibility(View.VISIBLE);
                                            textView.setText(R.string.no_lines);
                                            Log.d(TAG, "source or destination is not recognized!");
                                        }
                                    }

                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private void displayLinesOnScreen(ArrayList<Line> linesList, Line firstLine) {
        mLines = linesList;
        Log.d(TAG, "mPossibleLinesList in displayLinesOnScreen(): " + mLines.get(0).getLine_name());
        if (mContext instanceof Activity) {
            Log.d(TAG, "Are we there?");
            Activity activity = (Activity) mContext;
            //if their is suitable transport lines (ie.. the possibleLinesList is not null,
            // get the info of the line and put it into the list view
            //if (!mPossibleLinesList.isEmpty()) {
            progressBar = activity.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);
            CardInfoAdapter cardInfoAdapter = new CardInfoAdapter(activity, mLines);
            Log.d(TAG, "Card info adapter has been created!");
            ListView listView = activity.findViewById(R.id.list);
            listView.setAdapter(cardInfoAdapter);
            Log.d(TAG, "listView.setAdapter() is done!");

            //when a list item is clicked on, save the line name, departure_stop_id and arrival_stop_id and
            //start StartJourny activity
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Line item = mLines.get(i);
                    Intent intent = new Intent(activity, StartJourny.class);
                    intent.putExtra("sourceLocation", item.getDeparture_stop_id());
                    intent.putExtra("destinationLocation", item.getArrival_stop_id());
                    intent.putExtra("lineName", item.getLine_name());
                    if (firstLine != null) {
                        intent.putExtra("line1Name", item.getLine_name());
                        intent.putExtra("line2Name", firstLine.getLine_name());
                    }
                    Log.d(TAG, "are we there??");
                    activity.startActivity(intent);
                }
            });


            //}
        }
    }

}