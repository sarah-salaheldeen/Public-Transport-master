package com.example.publictransport;

import android.content.Context;
import android.util.Log;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseQuery {

    private Point mSourceLocation;
    private Point mDestinationLocation;
    private Context mContext;

    ArrayList<String> sourceStationsList;
    ArrayList<String> destinationStationsList;

   /* private List<String> sourceStationsList;
    private ArrayList<String> destinationStationsList;*/

    public DatabaseQuery(Point sourceLocation, Point destinationLocation, Context context){
        mSourceLocation = sourceLocation;
        mDestinationLocation = destinationLocation;
        mContext = context;
    }

    public void queryLines(){
        Driver driver = GraphDatabase.driver("bolt://192.168.43.154:7687", AuthTokens.basic("neo4j", "123"));
        try(Session session = driver.session()){
            session.readTransaction( tx -> {
                        Log.i("DatabaseQuery", "HELLO, anybody here??");
                    sourceStationsList = new ArrayList<>();
                    destinationStationsList = new ArrayList<>();
                        Result result = tx.run("with point({ longitude: " + "32.580306" +
                                ", latitude: " + "15.554754" + "}) AS userLocation,\n" +
                                "point({longitude: " + "32.555174" +
                                ", latitude: " + "15.607662" + "}) AS userDestination\n" +
                                "match (source:Station) WHERE distance(point({longitude:source.Longitude, latitude:source.Latitude }), userLocation) < 3000\n" +
                                "match (destination:Station) WHERE distance(point({longitude:destination.Longitude, latitude:destination.Latitude }), userDestination) < 3000\n" +
                                "with source, destination\n" +
                                "match (leg1:Leg) -[:STARTS_AT] ->(source)\n" +
                                "where (leg1) -[:ENDS_AT] -> (destination) OR \n" +
                                "(leg1) - [:NEXT_LEG*1..100] -> (leg2:Leg) - [:ENDS_AT] -> (destination)\n" +
                                "with leg2" +
                                "return source, destination, leg1, leg2");
                        while (result.hasNext()) {
                            Map<String,Object> row = result.next().asMap();
                            for (Map.Entry<String,Object> column : row.entrySet()){
                                /*if (column.getKey().equals("source.name")){ sourceStationsList.add(column.getValue().toString()); }
                                else if (column.getKey().equals("destination.name")) { destinationStationsList.add(column.getValue().toString()); }*/
                                Log.i("DatabaseQuery", column.getKey() + ": " + column.getValue() + "; ");
                            }
                        }
                        return true;
                    });


        }catch (Exception e){
            Log.d("DatabaseQuery", "error: " + e);
        }
    }
}
