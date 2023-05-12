package com.example.triptracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TripService extends Service implements LocationListener, SensorEventListener {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private Location previous_location;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount;
    private Handler handler;
    private PowerManager.WakeLock wakeLock;
    private int mNotificationValue = 0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private int previous_step;
    private boolean moved;
    private String fileName;

    @Override
    public void onCreate() {

        super.onCreate();
        //Sets up sensors
        setUpSensors();

        handler = new Handler();
        stepCount = 0;
        moved = true;

        //Creates file name
        Date date = new Date();
        Timestamp timestamp2 = new Timestamp(date.getTime());
        fileName = "" + timestamp2 + ".csv";

        //Creates a new file and adds the first sentence containing field names -
        //timestamp,longitude,latitude,speed,distance,moved
        writeStartOfFile();
    }

    //Sets up the service, acquires WakeLock, obtains first location, and starts the handler loop
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:tagforclassMyService");
        wakeLock.acquire();
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("TripTracker")
                .setSmallIcon(com.google.android.material.R.drawable.navigation_empty_icon)
                .setContentIntent(pendingIntent)
                .setPriority(2)
                .build();

        startForeground(1, notification);
        checkForUpdates();
        startUpdateLoop();
        return START_STICKY;
    }


    //Used to create the notification channel for the service
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    //When service is closed it will release the lock, unregister GPS and step-detect-sensors
    //listeners and stop the service.
    @Override
    public void onDestroy() {
        //removes handler callbacks
        handler.removeCallbacksAndMessages(null);
        // Release the WakeLock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
        //Unregister the location updates to avoid unnecessary resource usage
        if (fusedLocationProviderClient != null) {
            // Release the resources associated with fusedLocationProviderClient
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            fusedLocationProviderClient = null;
        }
        //locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
        stopForeground(true);
        stopSelf();
    }

    //When the step-detect-sensor detects a step, the
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        stepCount++;

    }

    //Removes location updates from fusedLocationProviderClient
    //after 5 seconds, then calls updateStepSensor.
    private void startUpdateLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mLocationCallback != null)
                {
                    fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                }
                updateStepSensor();
            }
        }, 5000);
    }

    //After 15 seconds obtains new location update, saves it to file and calls startUpdateLoop()
    private void startUpdateLoop2() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkForUpdates();
                startUpdateLoop();
            }
        }, 15000);
    }

    //After 20 seconds writes to file the previous location, then calls updateStepSensor
    private void startUpdateLoop3() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                writeToFile(previous_location);
                updateStepSensor();
            }
        }, 20000);
    }

    //Checks if the user has moved
    //If yes calls startUpdateLoop2() to obtain a new gps location
    //if no calls startUpdateLoop3() to write the previous location into file
    private void updateStepSensor()
    {
        if(previous_step < stepCount)
        {
            moved = true;
            startUpdateLoop2();
        }
        else{
            if (previous_location != null)
            {
                moved = false;
                startUpdateLoop3();
            }
        }
        previous_step=stepCount;
    }


    //Obtains a new location from the GPS and saves it into file.
    private void checkForUpdates() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        writeToFile(location);
                        previous_location = location;
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    //Writes data into file -
    //timestamp,longitude,latitude,speed,distance,moved
    public void writeToFile(Location location){
        try {
            Date date = new Date();
            Timestamp timestamp2 = new Timestamp(date.getTime());
            FileOutputStream fileout=openFileOutput(fileName, MODE_APPEND);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(String.valueOf(timestamp2) + ",");
            outputWriter.write((String.valueOf(location.getLongitude())) + ",");
            outputWriter.write((String.valueOf(location.getLatitude())) + ",");
            if(previous_location != null)
            {
                outputWriter.write((String.valueOf((previous_location.distanceTo(location)/20)))+",");
            }
            else {
                outputWriter.write((String.valueOf(0)));
            }
            if(previous_location != null)
            {
                outputWriter.write((String.valueOf(previous_location.distanceTo(location))));
            }
            else {
                outputWriter.write(",0");
            }
            if(moved)
            {
                outputWriter.write(",1");
            }
            else {
                outputWriter.write(",0");
            }
            outputWriter.write("\n");
            outputWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Creates a new file and adds the first sentence containing field names -
    //timestamp,longitude,latitude,speed,distance,moved
    public void writeStartOfFile()
    {
        FileOutputStream fileout= null;
        try {
            fileout = openFileOutput(fileName, MODE_APPEND);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write("timestamp,");
            outputWriter.write("longitude,");
            outputWriter.write("latitude,");
            outputWriter.write("speed,");
            outputWriter.write("distance,");
            outputWriter.write("moved\n");
            outputWriter.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Sets up the gps and step detect sensor with its listeners
    public void setUpSensors()
    {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /* Unused methods*/
    @Override
    public IBinder onBind(Intent intent) {
        // Here we do not use a binder to interact with the foreground service.
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    @Override
    public void onLocationChanged(@NonNull Location location) {

    }


}