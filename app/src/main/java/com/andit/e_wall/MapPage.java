package com.andit.e_wall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.andit.e_wall.data_model.BoardModel;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;

public class MapPage extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap map;
    List<BoardModel> boardsListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_page);
        CheckPermissions();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_dashboard);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);


    }

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        ApiHelper helper = new ApiHelper();
        SharedPreferences prefs = this.getSharedPreferences("com.andit.E_WALL", Context.MODE_PRIVATE);
        String lanSettings = prefs.getString("token", null);

        try {
            helper.getPathBoards(lanSettings, new ApiRequestListener() {

                @Override
                public void apiResult(List<BoardModel> boardsList) {
                    boardsListing = boardsList;
                    runOnUiThread(() -> {
                        for (BoardModel board : boardsList) {
                            LatLng latLng = new LatLng(board.getLatitude(), board.getLongitude());

                            MarkerOptions markerOptions = new MarkerOptions();

                            markerOptions.position(latLng);
                            Toast tt = Toast.makeText(findViewById(R.id.map_view).getContext(), "board", Toast.LENGTH_SHORT);

                            tt.show();
                            markerOptions.title(board.getName());

                            Random rnd = new Random();
                            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

                            map.addCircle(new CircleOptions()
                                    .center(latLng)
                                    .radius(100)
                                    .strokeColor(color)
                                    .fillColor(adjustAlpha(Color.TRANSPARENT, 0.5f)));
                            Marker locationMarker = map.addMarker(markerOptions);
                            locationMarker.showInfoWindow();

                        }
                        if (ActivityCompat.checkSelfPermission(findViewById(R.id.boardsListView).getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(findViewById(R.id.boardsListView).getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            CheckPermissions();
                            return;
                        }

                        LocationManager locationManager = (LocationManager)
                                getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();

                        Location location = locationManager.getLastKnownLocation(locationManager
                                .getBestProvider(criteria, false));

                        UpdateDistances(boardsList);
                        handler.post(runnable);

                        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 18.0f));
                    });
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(() -> {
                UpdateDistances(boardsListing);
            });
            handler.postDelayed(runnable, 15000);
        }
    };



    public interface ApiRequestListener {
        void apiResult(List<BoardModel> boardsList);
    }

    public void UpdateDistances(List<BoardModel> boardsList){
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        ListView boardsListView =findViewById(R.id.boardsListView);

        LatLng ll = new LatLng(latitude, longitude);
        CustomAdapter customAdapter = new CustomAdapter(boardsList, ll ,boardsListView.getContext().getApplicationContext());


        boardsListView.setAdapter(customAdapter);

    }

    public void CheckPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        2);

            }
        } else {
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        2);

            }
        } else {
        }

    }

    public class MapRealoader implements Runnable {
        List<BoardModel> boardsList;
        public MapRealoader(List<BoardModel> boards){
            boardsList = boards;
        }
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent it = new Intent(findViewById(R.id.map_view).getContext(), MainPage.class);
                    startActivity(it);
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    Intent iti = new Intent(findViewById(R.id.map_view).getContext(), ARPage.class);
                    startActivity(iti);

                    return true;
            }
            return false;

        }
    };
}
