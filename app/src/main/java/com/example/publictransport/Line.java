package com.example.publictransport;

import java.util.ArrayList;

public class Line {

    private String arrival_stop_id;
    private String departure_stop_id;
    private float fees;
    private int id;
    private String line_name;
    private ArrayList<String> streets;

    public Line(){}

    public Line(String arrival_stop_id, String departure_stop_id, float fees, int id, String line_name, ArrayList<String> streets){
        this.arrival_stop_id = arrival_stop_id;
        this.departure_stop_id = departure_stop_id;
        this.fees = fees;
        this.id = id;
        this.line_name = line_name;
        this.streets = streets;
    }

    public String getArrival_stop_id(){
        return arrival_stop_id;
    }

    public String getDeparture_stop_id(){
        return departure_stop_id;
    }

    public float getFees(){
        return fees;
    }

    public int getId(){
        return id;
    }

    public String getLine_name(){
        return line_name;
    }

    public ArrayList<String> getStreets(){
        return streets;
    }


}
