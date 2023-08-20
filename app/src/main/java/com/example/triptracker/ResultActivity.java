package com.example.triptracker;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.triptracker.dbscan.DBSCANClusterer;
import com.example.triptracker.dbscan.DBSCANClusteringException;
import com.example.triptracker.dbscan.DistanceMetric;
import com.example.triptracker.dbscan.DistanceMetricPoint;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private ArrayList<Point> pointList;
    private ListView lvResults;
    private Point from1, to1;
    private ArrayList<ArrayList<Point>> allTrips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //Connect xml code to java (list)
        lvResults = findViewById(R.id.lvResults);

        //Check if file name exists provided from FileActivity, if it does read it in.
        try {
            readInFile(getIntent().getStringExtra("fileName"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        //Identify trips based on the mode and file recieved
        //Display them in a list if possible
        findTrips();
    }

    //Identifies trips based on mode, saves them into allTrips array and calls displayTrips()
    public void findTrips()
    {
        String mode = getIntent().getStringExtra("mode");
        if(mode.equals("movement"))
        {
            MovementMode mr = new MovementMode(pointList);
            allTrips = mr.getAllTrips();
        }
        else if(mode.equals("speed") )
        {
            SpeedMode sr = new SpeedMode(pointList);
            allTrips = sr.getAllTrips();
        }
        else if(mode.equals("db"))
        {
            DistanceMetric dmp = new DistanceMetricPoint();
            try {
                DBSCANClusterer db = new DBSCANClusterer(pointList, 5, 10, 120,dmp);
                ArrayList<ArrayList<Point>> ap = db.performClustering();
                allTrips = fromActivitiesToTrips(ap);

            } catch (DBSCANClusteringException e) {
                throw new RuntimeException(e);
            }
        }
        displayTrips(allTrips);
    }

    //Used to create a list of the trips that have been identified
    //By selecting any trip, MapsActivity will run displaying the trip
    public void displayTrips(ArrayList<ArrayList<Point>> allTrips)
    {
        ArrayList<String> pa = new ArrayList<>();
        for(ArrayList<Point> trip : allTrips)
        {
            pa.add("Started: " + trip.get(0).getTimestamp() + "\nEnded: "
                    + trip.get(trip.size()-1).getTimestamp());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, pa);
        lvResults.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvResults.setAdapter(arrayAdapter);
        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                from1 = allTrips.get(i).get(0);
                to1 = allTrips.get(i).get(allTrips.get(i).size()-1);
                startMapsActivity();
            }
        });
    }

    //method to start MapsActivity, passing an array of Points that represent the trip
    public void startMapsActivity()
    {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("al", pointsToMap(from1, to1));
        startActivity(intent);
    }

    //Reads in file, creates Point object out of it and saves them in pointList array
    public void readInFile(String fileName) throws FileNotFoundException {
        pointList = new ArrayList<>();
        FileInputStream fis = this.openFileInput(fileName);
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            reader.readLine();
            String line = reader.readLine();
            while (line != null) {
                String[] data = line.split(",");
                DateTimeFormatter formatter = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    formatter = new DateTimeFormatterBuilder()
                            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"))
                            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
                            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            .appendOptional(DateTimeFormatter.ofPattern("HH:mm"))
                            .toFormatter();
                }
                LocalDateTime timestamp = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    timestamp = LocalDateTime.parse(data[0], formatter);
                }
                Point p = new Point(timestamp,Double.parseDouble(data[1]),Double.parseDouble(data[2]),Double.parseDouble(data[3]),
                        Double.parseDouble(data[4]),Integer.parseInt(data[5]));
                pointList.add(p);
                line = reader.readLine();
            }
            fis.close();
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
        }
    }

    //Creates a new arraylist of points meant to represent the trip, which will be
    //further used in MapsActivity to mark the trip
    public ArrayList<Point> pointsToMap(Point from, Point to)
    {
        ArrayList<Point> tripz = new ArrayList<>();
        for(int x = 0; x < pointList.size(); x++)
        {
            if(pointList.get(x).getTimestamp() == from.getTimestamp())
            {
                if(to != null)
                {
                    for(int y = x +1; y < pointList.size(); y++)
                    {
                        if(pointList.get(y).getTimestamp() == to.getTimestamp())
                        {
                            return tripz;
                        }
                        else{
                            tripz.add(pointList.get(y));
                        }

                    }
                }
                for(int y = x +1; y < pointList.size(); y++)
                {
                    tripz.add(pointList.get(y));
                }
            }
        }
        return tripz;
    }

    //TDBSCANs returns clusters, not trips. This method is used to obtain trips
    //from the points between clusters.

    public ArrayList<ArrayList<Point>> fromActivitiesToTrips(ArrayList<ArrayList<Point>> activities)
    {
        ArrayList<ArrayList<Point>> foundTrips = new ArrayList<ArrayList<Point>>();
        Point tripStart;
        Point tripEnd;

        if(activities.size() == 0)
        {
            tripStart = pointList.get(0);
            tripEnd = pointList.get(pointList.size()-1);

            foundTrips.add(formTrip(tripStart,tripEnd));

            return foundTrips;
        }

        for(int x = 0; x < activities.size(); x++)
        {
            if(x == 0)
            {
                tripStart = pointList.get(0);
                tripEnd =  activities.get(0).get(0);

                foundTrips.add(formTrip(tripStart,tripEnd));

            }
            else if(x+1 < activities.size()){

                tripStart = activities.get(x).get(activities.get(x).size()-1);
                tripEnd = activities.get(x+1).get(0);

                foundTrips.add(formTrip(tripStart,tripEnd));

            }
            if(x+1 == activities.size())
            {
                tripStart = activities.get(x).get(activities.get(x).size()-1);
                tripEnd = pointList.get(pointList.size()-1);


                foundTrips.add(formTrip(tripStart,tripEnd));

            }

        }
        return foundTrips;
    }

    public ArrayList<Point> formTrip(Point tripStart, Point tripEnd)
    {
        ArrayList<Point> newTrip = new ArrayList<Point>();
        for(int x = 0; x < pointList.size(); x++ )
        {
            if(tripStart == pointList.get(x))
            {
                newTrip.add(pointList.get(x));
                for(int y = x+1; y < pointList.size(); y++ )
                {
                    newTrip.add(pointList.get(y));
                    if(tripEnd == pointList.get(y)){
                        return newTrip;
                    }
                }
            }
        }
        return newTrip;
    }



}