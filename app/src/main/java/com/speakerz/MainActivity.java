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
import android.widget.EditText;
import android.widget.Toast;


import com.speakerz.SpeakerzService.LocalBinder;
import com.speakerz.debug.D;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.event.CommonModel_ViewEventHandler;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;
import com.speakerz.view.PlayerRecyclerActivity;

import java.util.ArrayList;
import java.util.List;

import ealvatag.audio.exceptions.CannotReadException;


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


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //set the viewEventhandler to handle events from model
        super.onCreate(savedInstanceState);
        //need the policy to send data below API 28
        checkDataSendingPolicy();


        setContentView(R.layout.activity_main);


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
             /*   Intent Act2 = new Intent(getApplicationContext(),Create.class);
                startActivity(Act2);*/
                Intent Act2 = new Intent(getApplicationContext(), PlayerRecyclerActivity.class);
                startActivity(Act2);


            }

        });

     //   gpsStatusCheck();
    }

    //Permission&Policy
    // Function to check and request permission
    List<String> permissionsToAsk=new ArrayList<String>();
    public void checkPermissions(String permissions[], int requestCode) {

        // Checking if permission is not granted
       permissionsToAsk.clear();
for(int i=0;i<permissions.length;i++) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) == PackageManager.PERMISSION_DENIED) {
       permissionsToAsk.add(permissions[i]);
    }

    if(permissionsToAsk.size()>0) {
        String[] arr = new String[permissionsToAsk.size()];
        arr = permissionsToAsk.toArray(arr);

        ActivityCompat
                .requestPermissions(
                        MainActivity.this,
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
