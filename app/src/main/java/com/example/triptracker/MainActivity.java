package com.example.triptracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int ACTIVITY_PERMISSION_REQUEST_CODE = 101;
    private Button btnStart, btnStop, btnResults;
    private TextView tvStatus, tvLocperm, tvActperm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Connects xml components (buttons and text) with java code
        setUpLayout();

        //Checks if the device has a step-detect-sensor
        //If yes then everything functions correctly
        //If no then the user gets informed about it and the start and stop buttons get disabled
        checkForStepSensor();

        // Request permission to access the step detector sensor (
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) ;
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 101);
        }

        //Checks if the device has inbuilt sensor, if not the user is informed and the buttons disabled.
        checkForStepSensor();

        //When start button is clicked (assuming it is not disabled) -
        // TripService is started, start button is diabled and stop button is enabled.
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvStatus.setText("Data gathering ongoing");
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                startService();
            }
        });

        //When stop button is clicked (assuming it is not disabled) -
        //TripService is stopped, start button is enabled and stop button is disabled.
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvStatus.setText("Data gathering stopped");
                btnStop.setEnabled(false);
                btnStart.setEnabled(true);
                stopService();
            }
        });

        //When the result button is clicked (assuming it is not disabled), ResultActivity is started.
        btnResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileActivity();
            }
        });
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, TripService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, TripService.class);
        stopService(serviceIntent);
    }

    public void fileActivity() {
        Intent i = new Intent(this,FileActivity.class);
        startActivity(i);
    }


    //Callback method that is called when the user responds to a permission request.
    //If both permissions are granted their text will turn green and the application will function normally.
    //If a permission is not granted their text will turn red and the start button will be disabled.

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Activity recognition permission is granted
                tvActperm.setBackground(new ColorDrawable(Color.GREEN));
                Toast.makeText(this, "Activity recognition permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Activity recognition permission is denied
                tvActperm.setBackground(new ColorDrawable(Color.RED));
                Toast.makeText(this, "Activity recognition permission denied", Toast.LENGTH_SHORT).show();
                btnStart.setEnabled(false);
            }
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ;
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        }
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission is granted
                tvLocperm.setBackground(new ColorDrawable(Color.GREEN));
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Location permission is denied
                tvLocperm.setBackground(new ColorDrawable(Color.RED));
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                btnStart.setEnabled(false);
            }
        }

    }

    //Connects xml components (buttons and text) with Java code.
    public void setUpLayout()
    {
        btnStop = findViewById(R.id.btnStop);
        btnResults = findViewById(R.id.btnResults);
        btnStart = findViewById(R.id.btnStart);
        tvStatus = findViewById(R.id.tvStatus);
        tvLocperm = findViewById(R.id.tvLocperm);
        tvActperm = findViewById(R.id.tvActperm);
    }

    //Checks if device has a step-detect-sensor.
    //If yes it functions normally.
    //if not, it informs the user and disables buttons.
    public void checkForStepSensor()
    {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepSensor == null) {
            tvStatus.setText("Your device does not have\n a step-detect-sensor.\n The app cannot be used without it");
            btnStart.setEnabled(false);
            btnStop.setEnabled(false);
        }
    }

}