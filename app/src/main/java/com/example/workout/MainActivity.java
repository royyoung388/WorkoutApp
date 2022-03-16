package com.example.workout;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private String TAG = "sensor";
    private final String[] SIGNIFICANT = {"easy", "medium", "hard"};
    // easy-1, medium-2, hard-3
    private int currentMode;

    private int SIGNIFICANT_SHAKE;
    private int shakeCounter;
    private long startTime;

    private float lastX, lastY, lastZ;
    private float acceleration;
    private float currentAcc;
    private float lastAcc;

    private ListView lv;
    private Button btStart, btStop;
    private TextView tvStep;

    private CameraManager CamManager;
    private String CamID;

    private SensorEventListener sensorEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = findViewById(R.id.lv_list);
        btStart = findViewById(R.id.bt_start);
        btStop = findViewById(R.id.bt_stop);
        tvStep = findViewById(R.id.tv_step);

        sensorEventListener = new AccSensorListener();

        // listview
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, SIGNIFICANT));
        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            String item = (String) adapterView.getItemAtPosition(i);
            switch (item) {
                case "easy":
                    SIGNIFICANT_SHAKE = 20;
                    currentMode = 1;
                    break;
                case "medium":
                    SIGNIFICANT_SHAKE = 60;
                    currentMode = 2;
                    break;
                case "hard":
                    SIGNIFICANT_SHAKE = 100;
                    currentMode = 3;
                    break;
            }
        });
        currentMode = 1;
        SIGNIFICANT_SHAKE = 20;

        // button
        btStart.setOnClickListener(view -> {
            lv.setEnabled(false);
            btStart.setEnabled(false);
            openAcc();
            startTime = System.currentTimeMillis();
        });
        btStop.setOnClickListener(view -> {
            lv.setEnabled(true);
            btStart.setEnabled(true);
            shakeCounter = 0;
            closeAcc();
            closeFlash();
        });

        //flash
        CamManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CamID = CamManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // acc
        acceleration = 0.00f;
        currentAcc = SensorManager.GRAVITY_EARTH;
        lastAcc = SensorManager.GRAVITY_EARTH;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeAcc();
        closeFlash();
    }

    private void openAcc() {
        SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void closeAcc() {
        SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    public void openFlash() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CamManager.setTorchMode(CamID, true);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeFlash() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CamManager.setTorchMode(CamID, false);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private class AccSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            lastAcc = currentAcc;

            currentAcc = x * x + y * y + z * z;
            acceleration = 0.1f * (currentAcc - lastAcc);

            // one shake
            if (acceleration > SIGNIFICANT_SHAKE) {
                Log.e(TAG, "delta x = " + (x - lastX));
                Log.e(TAG, "delta y = " + (y - lastY));
                Log.e(TAG, "delta z = " + (z - lastZ));
                Log.e(TAG, "current acc = " + currentAcc);
                Log.e(TAG, "last acc = " + lastAcc);
                shakeCounter++;
                tvStep.setText(String.valueOf(shakeCounter));
                // stop after 100
                if (shakeCounter >= 100) {
                    btStop.callOnClick();

                    long endTime = System.currentTimeMillis();
                    // 2 minutes
                    if (endTime - startTime < 120000) {
                        Toast.makeText(MainActivity.this, "You are a rockstar.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Great job, keep practicing to get faster.", Toast.LENGTH_SHORT).show();
                    }
                }

                // easy mode
                if (currentMode == 1 && shakeCounter > 10) {
                    openFlash();
                    closeFlash();

                    if (shakeCounter == 30) {
                        // todo play music
                    }
                }

                // medium mode
                if (currentMode == 2 && shakeCounter > 30) {
                    openFlash();
                    closeFlash();

                    if (shakeCounter == 45) {
                        // todo play music
                    }
                }

                // hard mode
                if (currentMode == 3 && shakeCounter > 30) {
                    openFlash();
                    closeFlash();

                    if (shakeCounter == 60) {
                        // todo play music
                    }
                }
            }

            lastX = x;
            lastY = y;
            lastZ = z;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}