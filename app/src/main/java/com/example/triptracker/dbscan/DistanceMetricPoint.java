package com.example.triptracker.dbscan;

import android.os.Build;

import com.example.triptracker.Point;

import java.time.Duration;
import java.time.LocalDateTime;


/**
 * Distance metric implementation for numeric values.
 * 
 * @author <a href="mailto:cf@christopherfrantz.org">Christopher Frantz</a>
 * @version 0.1
 *
 */
public class DistanceMetricPoint implements DistanceMetric<Point>{
    private final double r2d = 180.0D / 3.141592653589793D;
    private final double d2r = 3.141592653589793D / 180.0D;
    private final double d2km = 111189.57696D * r2d;

    
    @Override
	public double calculateDistance(Point me, Point other) throws DBSCANClusteringException  {
        double x = me.getLatitude() * d2r;
        double y = other.getLatitude() * d2r;
        return Math.acos( Math.sin(x) * Math.sin(y) + Math.cos(x) * Math.cos(y) * Math.cos(d2r * (me.getLongtitude() - other.getLongtitude()))) * d2km;
    }
    
    @Override
	public long calculateTime(Point me, Point other) throws DBSCANClusteringException  {
        LocalDateTime x = me.getTimestamp();
        LocalDateTime y = other.getTimestamp();
        Duration duration = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            duration = Duration.between(y, x);
            System.out.println("x: " + x + " y: " + y + "  T: " + duration.getSeconds());
            return duration.getSeconds();
        }
        return 0;
    }




}
