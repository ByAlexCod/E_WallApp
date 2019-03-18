package com.andit.e_wall;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andit.e_wall.data_model.BoardModel;
import com.andit.e_wall.data_model.Coord;
import com.andit.e_wall.data_model.MessageModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class ARPage extends AppCompatActivity {
    private static final String TAG = ARPage.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    boolean poped = false;
    private float[] mGravity;
    private ViewRenderable rendera;
    float currentBearing;
    private LatLng startLoc;
    private float[] mMagnetic;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        SharedPreferences prefs = this.getSharedPreferences("com.andit.E_WALL", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        setContentView(R.layout.ar_page);
        String[] latLng = getIntent().getStringExtra("latlng").split(";");

        startLoc = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
        initSensors();

        ApiHelper ap = new ApiHelper();
        try {
            ap.getBoardMessages(token, String.valueOf(getIntent().getIntExtra("boardId", 0)), new ApiRequestMessageList() {
                @Override
                public void apiResult(List<MessageModel> messages, BoardModel boardModel) {
                    InitAr(messages, savedInstanceState, boardModel);




                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }




    }

    private void InitAr(List<MessageModel> messages, Bundle savedInstanceState, BoardModel boardModel) {
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.onCreate(savedInstanceState);


        RenderableSet(messages);

        UpdateOnFragment(boardModel);
    }

    private void UpdateOnFragment(BoardModel boardModel) {
        arFragment.getArSceneView().getScene().addOnUpdateListener((frameTime) -> {
            try {
                if (!poped) {
                    arFragment.getPlaneDiscoveryController().hide();
                    float orientation = currentBearing;

                                        try {


                                            Coord point = MapHelper.TranslatePlan(orientation, startLoc, new LatLng(boardModel.getLatitude(), boardModel.getLongitude()));
                                            poped = true;
                                            Anchor anchora = arFragment.getArSceneView().getSession().createAnchor(Pose.makeTranslation(0f, 0f, 0f));
                                            AnchorNode anchorNode1 = new AnchorNode(anchora);
                                            anchorNode1.setParent(arFragment.getArSceneView().getScene());
                                            TransformableNode andy1 = new TransformableNode(arFragment.getTransformationSystem());
                                            andy1.setParent(anchorNode1);
                                            andy1.setWorldPosition(new Vector3(point.getX(), 0, point.getY() - 3f));
                                            andy1.setRenderable(rendera);
                                            andy1.select();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            poped = false;
                                        }

                                }



            } catch (Exception e) {
                poped = false;
                Log.e("Normal", "Normal", e);
            }
        });
    }

    private void RenderableSet(List<MessageModel> messages) {
        runOnUiThread(() -> {
            List<String> strModels =  messages.stream().map(it -> it.getMessage()).collect(Collectors.toList());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(ARPage.this,
                        android.R.layout.simple_list_item_1, strModels);

            ViewRenderable.builder()
                    .setView(ARPage.this, R.layout.ar_overlay)
                    .build()
                    .thenAccept(renderable -> {
                        rendera = renderable;
                        ListView listMessages = rendera.getView().findViewById(R.id._messagesList);
                        listMessages.setAdapter(adapter);
                    })
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(ARPage.this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });
        });
    }


    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }

        return true;
    }


    private void initSensors() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mSensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor mSensorMagneticField = sensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        /* Initialize the gravity sensor */
        if (mSensorGravity != null) {
            Log.i(TAG, "Gravity sensor available. (TYPE_GRAVITY)");
            sensorManager.registerListener(mSensorEventListener,
                    mSensorGravity, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.i(TAG, "Gravity sensor unavailable. (TYPE_GRAVITY)");
        }

        /* Initialize the magnetic field sensor */
        if (mSensorMagneticField != null) {
            Log.i(TAG, "Magnetic field sensor available. (TYPE_MAGNETIC_FIELD)");
            sensorManager.registerListener(mSensorEventListener,
                    mSensorMagneticField, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.i(TAG,
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
                    TextView compText = findViewById(R.id.compass);
                    compText.setText(String.valueOf(Math
                            .round(current_measured_bearing)));

                    /*
                     * Update variables for next use (Required for Low Pass
                     * Filter)
                     */
                    currentBearing = current_measured_bearing;

                }
            }
        }
    };

    public interface ApiRequestMessageList {
        void apiResult(List<MessageModel> messages, BoardModel boardModel);
    }


}
