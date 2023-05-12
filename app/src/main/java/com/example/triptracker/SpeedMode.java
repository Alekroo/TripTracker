package com.example.triptracker;

import java.util.ArrayList;

public class SpeedMode {

    ArrayList<Point> pointList;
    double SPEED_THRESHOLD = 0.09; //The minimum speed the user has to move in
    private int speed_counter; //Consecutive times user has moved equal or above SPEED_THRESHOLD
    ArrayList<int[]> speedTripList;


    public SpeedMode(ArrayList<Point> pointList)
    {
        this.pointList = pointList;
        speed_counter = 0;
        speedTripList = new ArrayList<int[]>();
        speedRule();
    }

    public void speedRule()
    {
        int firstIndex = -1;
        int rangeIndex = -1;

        for(int x = 0; x < pointList.size(); x++)
        {
            if(pointList.get(x).getSpeed() >= SPEED_THRESHOLD)
            {
                speed_counter = speed_counter + 1;

                for(int y = x + 1; y < pointList.size(); y++)
                {
                    if(pointList.get(y).getSpeed() >= SPEED_THRESHOLD)
                    {
                        speed_counter = speed_counter + 1;
                        if(pointList.get(x).distanceTo(pointList.get(y)) >= 100)
                        {
                            rangeIndex = 1;
                        }
                        if(speed_counter == 12)
                        {
                            firstIndex = x;
                        }
                    }
                    else
                    {
                        checkSpeedIndex(firstIndex,y,rangeIndex);
                        rangeIndex = -1;
                        firstIndex = -1;
                        speed_counter = 0;
                        x = y;
                        break;
                    }
                }
                checkSpeedIndex(firstIndex,pointList.size(),rangeIndex);
                rangeIndex = -1;
                firstIndex = -1;
                if(x == pointList.size())
                {
                    break;
                }
            }
        }

    }

    public void checkSpeedIndex(int startIndex, int endIndex, int rangeIndex)
    {
        if(startIndex != -1 && rangeIndex == 1)
        {
            int[] trip = new int[2];
            trip[0] = startIndex;
            trip[1] = endIndex;
            speedTripList.add(trip);
        }
    }

    public ArrayList<ArrayList<Point>> getAllTrips()
    {
        ArrayList<ArrayList<Point>> tripList = new ArrayList<ArrayList<Point>>();
        ArrayList<int[]> checkedTrips = falsePositiveCheck(speedTripList);
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
            } else if (trip[0] - currentTrip[1] <= 3) {
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

