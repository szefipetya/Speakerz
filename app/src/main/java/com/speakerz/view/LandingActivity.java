package com.speakerz.view;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.speakerz.R;
import com.speakerz.SpeakerzService;
import com.speakerz.debug.D;
import com.speakerz.model.event.CommonModel_ViewEventHandler;

import java.util.ArrayList;
import java.util.List;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing_layout);

        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(4000);
        animationDrawable.setExitFadeDuration(8000);
        animationDrawable.start();

        checkDataSendingPolicy();

        ImageButton back = (ImageButton) findViewById(R.id.button_back);
        if(back!=null)
        back.setImageResource(R.drawable.ic_m_up_white);
        Button buttonJoin = (Button) findViewById(R.id.button_join);
        buttonJoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    initAndStart(false);
                    Intent Act2 = new Intent(getApplicationContext(), JoinActivity.class);
                    startActivity(Act2);
                }
                else{
                    Toast.makeText( getApplicationContext(),"PLS ENABLE WIFI",Toast.LENGTH_SHORT).show();
                }
            }

        });


        Button buttonCreate = (Button) findViewById(R.id.button_create);
        buttonCreate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    initAndStart(true);
                    Intent Act2 = new Intent(getApplicationContext(), PlayerRecyclerActivity.class);
                    startActivity(Act2);
                }
                else{
                    Toast.makeText( getApplicationContext(),"PLS ENABLE WIFI",Toast.LENGTH_SHORT).show();
                }


            }

        });

        gpsStatusCheck();

    }

    SpeakerzService _service;
    boolean _isBounded;

    CommonModel_ViewEventHandler viewEventHandler;

    //REQUIRED_END MODEL
    Integer PermissionCheckEvent_EVT_ID=145;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;
            _service =  localBinder.getService();
            _isBounded = true;
            //permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkCoarseLocationPermission();
            }
            D.log("service connected");


            subscribePermissionEvents();
          /*  _service.PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED));
            _service.PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.connectionPermission,Manifest.permission.ACCESS_FINE_LOCATION,PackageManager.PERMISSION_GRANTED));
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,_service.PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);*/

            checkPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},200);


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            _isBounded = false;
        }
    };
    Activity self=this;
    private void subscribePermissionEvents(){

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, SpeakerzService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if(_service!=null)
            _service.getTextValueStorage().autoConfigureTexts(this);
        else{
            //D.log("err: MainActivity : service is null");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        _isBounded = false;
        D.log("main.onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(_service!=null)
            _service.getTextValueStorage().autoConfigureTexts(this);
        //a bánat tudja, hogy ez mit csinál, de kell

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
    private void initAndStart(boolean isHost) {

        Intent intent = new Intent(this, SpeakerzService.class);
        intent.putExtra("isHost",isHost);
        this.startService(intent);



    }

        //Permission&Policy
    // Function to check and request permission
    List<String> permissionsToAsk=new ArrayList<String>();
    public void checkPermissions(String[] permissions, int requestCode) {

        // Checking if permission is not granted
        permissionsToAsk.clear();
        for(int i=0;i<permissions.length;i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(LandingActivity.this, permissions[i]) == PackageManager.PERMISSION_DENIED) {
                permissionsToAsk.add(permissions[i]);
            }

            if(permissionsToAsk.size()>0) {
                String[] arr = new String[permissionsToAsk.size()];
                arr = permissionsToAsk.toArray(arr);

                ActivityCompat
                        .requestPermissions(
                                LandingActivity.this,
                                arr,
                                requestCode);
            }


        }


        //permission already granted
        D.log(permissions +" already granted.");
        if(requestCode==_service.PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE){
            D.log("storage permission granted.");
            //  if(_service.getModel()!=null)
            //   _service.getModel().getMusicPlayerModel().loadAudioWithPermission();
        }
        if(requestCode== _service.ACCESS_FINE_LOCATION_CODE){
            D.log("fine location permission granted originally.");

            //  if(_service.getModel()!=null)
            //   _service.getModel().getMusicPlayerModel().loadAudioWithPermission();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        synchronized (_service.PermissionAcceptLocker) {
            _service.PermissionAcceptLocker.notify();
        }

        if(grantResults.length!=permissionsToAsk.size()) {

        }

        D.log("ACCESS_COARSE_LOCATION Permission granted.");

        if(requestCode==_service.PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE){
            D.log("storage permission granted.");
            //    if(_service.getModel()!=null)
            //     _service.getModel().getMusicPlayerModel().loadAudioWithPermission();
        }
        if(requestCode== _service.ACCESS_FINE_LOCATION_CODE){
            D.log("fine location permission granted by request");

            //  if(_service.getModel()!=null)
            //   _service.getModel().getMusicPlayerModel().loadAudioWithPermission();
        }

    }


    private void checkDataSendingPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


    //ujproba
    public void gpsStatusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkCoarseLocationPermission(){


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    _service.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        }else{
            //do something, permission was previously granted; or legacy device
            D.log("ACCESS_COARSE_LOCATION Permission granted originally");

        }
    }



    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}