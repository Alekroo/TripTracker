package com.example.triptracker;

import android.widget.Toast;

import java.util.ArrayList;

public class MovementMode {

    private ArrayList<Point> pointList;
    private int movement_counter;
    private ArrayList<int[]> movementTripList;

    public MovementMode(ArrayList<Point> pointList)
    {
        this.pointList = pointList;
        movement_counter = 0;
        movementTripList = new ArrayList<int[]>();
        movementRule();

    }

    public void movementRule()
    {
        int firstIndex = -1;
        int rangeIndex = -1;

        for(int x = 0; x < pointList.size(); x++)
        {
            if(pointList.get(x).getMoved() == 1)
            {
                movement_counter = movement_counter + 1;

                for(int y = x + 1; y < pointList.size(); y++)
                {
                    if(pointList.get(y).getMoved() == 1)
                    {

                        movement_counter = movement_counter + 1;
                        if(pointList.get(x).distanceTo(pointList.get(y)) >= 100)
                        {
                            rangeIndex = 1;
                        }
                        if(movement_counter == 12)
                        {
                            firstIndex = x;
                        }
                    }
                    else
                    {
                        checkMovementIndex(firstIndex,y,rangeIndex);
                        firstIndex = -1;
                        movement_counter = 0;
                        x = y;
                        rangeIndex = -1;
                        break;
                    }
                }
                checkMovementIndex(firstIndex,pointList.size(),rangeIndex);
                firstIndex = -1;
                rangeIndex = -1;
                if(x == pointList.size())
                {
                    break;
                }
            }
        }
    }

    public void checkMovementIndex(int startIndex, int endIndex, int rangeIndex)
    {
        if(startIndex != -1 && rangeIndex == 1)
        {
            int[] trip = new int[2];
            trip[0] = startIndex;
            trip[1] = endIndex;
            movementTripList.add(trip);
        }

    }

    public ArrayList<ArrayList<Point>> getAllTrips()
    {
        ArrayList<ArrayList<Point>> tripList = new ArrayList<ArrayList<Point>>();
        ArrayList<int[]> checkedTrips = falsePositiveCheck(movementTripList);
        for(int x = 0; x < checkedTrips.size(); x++) {
            ArrayList<Point> newTrip = new ArrayList<Point>();
            for (int y = checkedTrips.get(x)[0]; y < checkedTrips.get(x)[1]; y++) {
                newTrip.add(pointList.get(y));
            }
            tripList.add(newTrip);
        }
        return tripList;
    }

    public ArrayList<int[]> falsePositiveCheck(ArrayList<int[]> tripz) {
        ArrayList<int[]> trips = tripz;
        ArrayList<int[]> mergedTrips = new ArrayList<>();
        int[] currentTrip = null;
        for (int[] trip : trips) {
            if (currentTrip == null) {
                currentTrip = trip;
            } else if (trip[0] - currentTrip[1] <= 6) {
                currentTrip[1] = trip[1];
            } else {
                mergedTrips.add(currentTrip);
                currentTrip = trip;
            }
        }
        if (currentTrip != null) {
            mergedTrips.add(currentTrip);
        }
        return mergedTrips;
    }

}
