package com.speakerz;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.speakerz.SpeakerzService.LocalBinder;
import com.speakerz.debug.D;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.event.CommonModel_ViewEventHandler;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.util.EventListener;


/**REQUIRED means: it needs to be in every Activity.*/
public class MainActivity extends Activity {
    //REQUIRED_BEG MODEL
    SpeakerzService _service;
    boolean _isBounded;

    CommonModel_ViewEventHandler viewEventHandler;

    //REQUIRED_END MODEL
    Integer PermissionCheckEvent_EVT_ID=145;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder localBinder = (LocalBinder) binder;
            _service =  localBinder.getService();
            _isBounded = true;
            //permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkCoarseLocationPermission();
            }

            subscribePermissionEvents();


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            _isBounded = false;
        }
    };

    private void subscribePermissionEvents(){



        _service.PermissionCheckEvent.addListener(new EventListener<PermissionCheckEventArgs>() {
            @Override
            public void action(PermissionCheckEventArgs args) {
                if(args.getReason()== PERM.connectionPermission) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, args.getRequiredPermission()) != args.getSuccessNumber()) {
                        Toast.makeText(MainActivity.this, "Failure at granting a permission. ", Toast.LENGTH_SHORT).show();
                        //D.log("connection failure: Service");
                        checkPermission(args.getRequiredPermission(), _service.ACCESS_FINE_LOCATION_CODE);
                    } else if (_service.getModel() instanceof DeviceModel) {
                        //critical call. Need to make sure the type before casting...
                        ((DeviceNetwork) (_service.getModel().getNetwork())).connectWithPermissionGranted();
                        //D.log("access granted");
                    }
                }

                if(args.getReason()==PERM.ACCESS_COARSE_LOCATION){
                    checkPermission(args.getRequiredPermission(),_service.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                }
                if(args.getReason()==PERM.READ_EXTERNAL_STORAGE){
                    checkPermission(args.getRequiredPermission(),_service.PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE);
                }
            }
        },PermissionCheckEvent_EVT_ID);
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

        //D.log("main_onResume");
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


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //set the viewEventhandler to handle events from model
        super.onCreate(savedInstanceState);
        //need the policy to send data below API 28
        checkDataSendingPolicy();


        setContentView(R.layout.activity_main);

        //D.log("oncreate_main");

        Button buttonJoin = (Button) findViewById(R.id.join);
        buttonJoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                initAndStart(false);
                Intent Act2 = new Intent(getApplicationContext(),Join.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);
            }

        });


        Button buttonCreate = (Button) findViewById(R.id.create);
        buttonCreate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                initAndStart(true);
                Intent Act2 = new Intent(getApplicationContext(),Create.class);
                startActivity(Act2);
                Act2.putExtra("Hello","Hello World");


            }

        });


        Button buttonOptions = (Button) findViewById(R.id.options);
        buttonOptions.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),Options.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });

        gpsStatusCheck();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //checkCoarseLocationPermission();
        }

        //ez Android 8.0 felett kell


    }

    //Permission&Policy
    // Function to check and request permission
    public void checkPermission(String permission, int requestCode) {

        // Checking if permission is not granted

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            MainActivity.this,
                            new String[]{permission},
                            requestCode);
        } else {
            //permission already granted
            D.log(permission +" already granted.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == _service.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            D.log("ACCESS_COARSE_LOCATION Permission granted.");
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
            //requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    //service.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
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
