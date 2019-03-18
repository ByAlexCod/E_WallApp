package com.andit.e_wall;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.android.gms.maps.GoogleMap;

import static com.andit.e_wall.R.layout.activity_main_page;


public class MainPage extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener, ActivityCompat.OnRequestPermissionsResultCallback
{
    private TextView mTextMessage;
    private LinearLayout layout;
    private Context act;
    private TextView resp;
    AppCompatActivity ctx;
    private QRCodeReaderView qrCodeReaderView;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final Intent intent = getIntent();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    layout.setVisibility(LinearLayout.VISIBLE);
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    ToMap tm = new ToMap();
                    tm.run();
                    return true;
                case R.id.navigation_notifications:
                    Intent it = new Intent(MainPage.this, CalibrationPage.class);
                    startActivity(it);
                    return true;
            }
            return false;

        }
    };
    private int REQUEST_IMAGE_CAPTURE;
    private int DSI_height;
    private int DSI_width;
    private LinearLayout qrLayout;
    private GoogleMap mMap;
    private LinearLayout mapLayout;
    private MenuItem dashboardButton;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 25: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!getIntent().getBooleanExtra("restarted", false)){
                        Intent intent = getIntent();
                        finish();
                        intent.putExtra("restarted", true);
                        startActivity(intent);
                    }

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},26
                            );

                }
                return;
            }

        }
    }
    private Size imageDimension;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main_page);
        ctx = this;
        resp = findViewById(R.id.resp);
        act = getApplication();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DSI_height = displayMetrics.heightPixels;
        DSI_width = displayMetrics.widthPixels;
        imageDimension = new Size(DSI_width, DSI_height);
        qrLayout = findViewById(R.id.QRLayout);
        mapLayout = findViewById(R.id.map_layout);
        mTextMessage = findViewById(R.id.message);
        layout = findViewById(R.id.QRLayout);
        layout.setVisibility(LinearLayout.VISIBLE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},25
            );
        }





        qrCodeReaderView = findViewById(R.id.qrdecoderview);
        QRCodeSetup();

        PackageManager packageManager = layout.getContext().getPackageManager();
        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Toast tt = Toast.makeText(layout.getContext(), "no camera", Toast.LENGTH_SHORT);
            tt.show();
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setAspectRatioTextureView(imageDimension.getHeight(),imageDimension.getWidth());


    }

    private void QRCodeSetup() {
        qrCodeReaderView.setOnQRCodeReadListener(this);

        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(2000L);

        // Use this function to set front camera preview
        qrCodeReaderView.setBackCamera();
    }

    public class ToMap implements  Runnable {

        @Override
        public void run() {
            Intent it = new Intent(mTextMessage.getContext(), MapPage.class);
            startActivity(it);
        }
    }





    private void setAspectRatioTextureView(int ResolutionWidth , int ResolutionHeight )
    {
        if(ResolutionWidth > ResolutionHeight){
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionWidth)/ResolutionHeight);
            updateTextureViewSize(newWidth,newHeight);

        }else {
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionHeight)/ResolutionWidth);
            updateTextureViewSize(newWidth,newHeight);
        }

    }

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        Log.d("TEXTURE SIZE", "TextureView Width : " + viewWidth + " TextureView Height : " + viewHeight);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = viewHeight /2;
        params.width = viewWidth /2;
        layout.setLayoutParams(params);
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        ApiHelper helper = new ApiHelper();

        String txt = null;
        txt = helper.SendQRCodeResult(text, ctx, new ApiRequestListener() {
            @Override
            public void apiResult(String token) {
                String tok = token;
                Context context = getApplicationContext();
                SharedPreferences sharedPref = context.getSharedPreferences(
                        "com.andit.E_WALL", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("token", tok);
                editor.commit();
                ToMap a = new ToMap();
                a.run();
            }
        });

        Toast tt = Toast.makeText(ctx, txt, Toast.LENGTH_SHORT);
        tt.show();
        qrCodeReaderView.stopCamera();

        resp.setText(text);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {


    }
    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.startCamera();
    }
    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();

    }
    public interface ApiRequestListener {
        void apiResult(String token);
    }


}
