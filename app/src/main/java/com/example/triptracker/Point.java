package com.example.triptracker;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Point implements Serializable {

    private final int moved;
    private double distance;
    private LocalDateTime timestamp;
    private double longtitude;
    private double latitude;
    private double speed;
    private final double r2d = 180.0D / 3.141592653589793D;
    private final double d2r = 3.141592653589793D / 180.0D;
    private final double d2km = 111189.57696D * r2d;

    public Point(LocalDateTime timestamp, double longtitude, double latitude, double speed,
                 double distance, int moved) {
        this.timestamp = timestamp;
        this.longtitude = longtitude;
        this.latitude = latitude;
        this.speed = speed;
        this.distance = distance;
        this.moved = moved;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getMoved() {
        return moved;
    }

    public double getSpeed() {
        return speed;
    }


    public double distanceTo(Point other) {
        double x = latitude * d2r;
        double y = other.getLatitude() * d2r;
        return Math.acos(Math.sin(x) * Math.sin(y) + Math.cos(x) * Math.cos(y) * Math.cos(d2r * (longtitude - other.getLongtitude()))) * d2km;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public double getLatitude() {
        return latitude;
    }


}
