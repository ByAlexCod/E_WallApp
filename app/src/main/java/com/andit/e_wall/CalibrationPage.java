package com.andit.e_wall;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.widget.TextView;
import android.widget.Toast;

public class CalibrationPage extends AppCompatActivity {

    private boolean doIt = true;
    private float currentBearing;
    private float[] mGravity;
    private float[] mMagnetic;
    private int attempts;
    private float firstBearing = 0f;

    @Override
    public void onCreate(Bundle a) {

        super.onCreate(a);
        setContentView(R.layout.calibration_page);
        initSensors();
    }

    private void initSensors() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mSensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor mSensorMagneticField = sensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        /* Initialize the gravity sensor */
        if (mSensorGravity != null) {
            Log.i("Sensor", "Gravity sensor available. (TYPE_GRAVITY)");
            sensorManager.registerListener(mSensorEventListener,
                    mSensorGravity, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.i("Sensor", "Gravity sensor unavailable. (TYPE_GRAVITY)");
        }

        /* Initialize the magnetic field sensor */
        if (mSensorMagneticField != null) {
            Log.i("Sensor", "Magnetic field sensor available. (TYPE_MAGNETIC_FIELD)");
            sensorManager.registerListener(mSensorEventListener,
                    mSensorMagneticField, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.i("Sensor",
                    "Magnetic field sensor unavailable. (TYPE_MAGNETIC_FIELD)");
        }

    }
    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

                mGravity = event.values.clone();

            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                mMagnetic = event.values.clone();

            }

            if (mGravity != null && mMagnetic != null) {

                /* Create rotation Matrix */
                float[] rotationMatrix = new float[9];
                if (SensorManager.getRotationMatrix(rotationMatrix, null,
                        mGravity, mMagnetic)) {

                    /* Compensate device orientation */
                    // http://android-developers.blogspot.de/2010/09/one-screen-turn-deserves-another.html
                    float[] remappedRotationMatrix = new float[9];
                    switch (getWindowManager().getDefaultDisplay()
                            .getRotation()) {
                        case Surface.ROTATION_0:
                            SensorManager.remapCoordinateSystem(rotationMatrix,
                                    SensorManager.AXIS_X, SensorManager.AXIS_Y,
                                    remappedRotationMatrix);
                            break;
                        case Surface.ROTATION_90:
                            SensorManager.remapCoordinateSystem(rotationMatrix,
                                    SensorManager.AXIS_Y,
                                    SensorManager.AXIS_MINUS_X,
                                    remappedRotationMatrix);
                            break;
                        case Surface.ROTATION_180:
                            SensorManager.remapCoordinateSystem(rotationMatrix,
                                    SensorManager.AXIS_MINUS_X,
                                    SensorManager.AXIS_MINUS_Y,
                                    remappedRotationMatrix);
                            break;
                        case Surface.ROTATION_270:
                            SensorManager.remapCoordinateSystem(rotationMatrix,
                                    SensorManager.AXIS_MINUS_Y,
                                    SensorManager.AXIS_X, remappedRotationMatrix);
                            break;
                    }

                    /* Calculate Orientation */
                    float results[] = new float[3];
                    String orientation = String.valueOf(SensorManager.getOrientation(remappedRotationMatrix,
                            results));

                    /* Get measured value */
                    float current_measured_bearing = (float) (results[0] * 180 / Math.PI);
                    if (current_measured_bearing < 0) {
                        current_measured_bearing += 360;
                    }

                    /* Smooth values using a 'Low Pass Filter' */
                    current_measured_bearing = current_measured_bearing
                            + 1
                            * (current_measured_bearing - current_measured_bearing);

                    /* Update normal output */
                    TextView compText = findViewById(R.id.make_a_8);
                    compText.setText(String.valueOf(Math
                            .round(current_measured_bearing)));

                    /*
                     * Update variables for next use (Required for Low Pass
                     * Filter)
                     */
                    currentBearing = current_measured_bearing;

                    if(attempts == 45) {
                        firstBearing = current_measured_bearing;
                        attempts++;
                    } else if(attempts > 45){

                        if(currentBearing > firstBearing + 80 || currentBearing < firstBearing -80){
                            Toast tt = Toast.makeText(CalibrationPage.this, "Calibrated", Toast.LENGTH_SHORT);
                            tt.show();
                        }
                    }
                    else {
                        attempts++;
                    }


                }
            }
        }
    };
}
