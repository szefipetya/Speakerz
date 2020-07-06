package com.speakerz;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.view.Display;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.speakerz.debug.D;
import com.speakerz.model.BaseModel;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.HostModel;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventListener;
import com.speakerz.viewModel.TextValueStorage;

public class SpeakerzService extends Service {
    private final class ServiceHandler extends Handler {
        private  SpeakerzService service;
        private BaseModel model = null;
        private int startId = -1;


        public ServiceHandler(Looper looper, SpeakerzService service) {
            super(looper);
            this.service = service;
            //D.log("servicehandler created");
        }

        private void startService(boolean isHost){

            if(model!=null){model.stop();}

          /*  if(isHost) {
                D.log("hostmodel created");
                model = new HostModel(service.receiver, service.connectivityManager);
            }
            else {
                D.log("devicemodel created");
                model=new DeviceModel(service.receiver, service.connectivityManager);
            }*/

            if(isHost && (model==null || model instanceof DeviceModel)){
                model = new HostModel(service.receiver,service.connectivityManager);
                model.start();
                this.subscribeEvents();
                D.log("hostmodel created");
            }
            else if(!isHost&&(model==null || model instanceof HostModel)){
                model = new DeviceModel(service.receiver,service.connectivityManager);
                model.start();
                this.subscribeEvents();
                D.log("devicemodel created");
            }



            ModelReadyEvent.invoke(new BooleanEventArgs(service,isHost));




        }

        public void stopService(){
            if(model == null) return;

            model.stop();
            stopSelf(startId);
            startId = -1;
            model = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if(model != null){
                stopService();
            }
            startId = msg.arg1;
            startService(msg.arg2 == 1);
        }

        private void subscribeEvents(){
            model.getNetwork().PermissionCheckEvent.addListener(new EventListener<PermissionCheckEventArgs>() {
                @Override
                public void action(PermissionCheckEventArgs args) {
                    //pass the permission sh*t to one of the views
                    PermissionCheckEvent.invoke(args);

                }

            });

        }
        public BaseModel getModel(){
            return model;
        }
    }

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private final IBinder binder = (IBinder) new LocalBinder();

    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel wifiP2pChannel;
    private WifiBroadcastReciever receiver;
    private ConnectivityManager connectivityManager;
    //from App
    private IntentFilter intentFilterForNetwork;

    public Event<BooleanEventArgs> ModelReadyEvent=new Event<>();
    public Event<PermissionCheckEventArgs> PermissionCheckEvent = new Event<>();


    private TextValueStorage textValueStorage ;
    @Override
    public void onCreate() {
        // Initialize connection objects
        textValueStorage = new TextValueStorage();

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager)getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        wifiP2pChannel = wifiP2pManager.initialize(this, getMainLooper(), null);
        receiver = new WifiBroadcastReciever(wifiManager,wifiP2pManager,wifiP2pChannel);
        connectivityManager=(ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper, this);
    }

    /**
     * Start a background service
     * Use the intent's 'isHost' boolean extra to specify the mode
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Sending a message to ServiceHandler to start a new model in the given mode
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        if(intent!=null){
            msg.arg2 = intent.getBooleanExtra("isHost", true)? 1 : 0;
        }else
            msg.arg2=1;

        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }



    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        SpeakerzService getService() {
            // Return this instance of LocalService so clients can call public methods
            return  SpeakerzService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onDestroy() {
        serviceHandler.stopService();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    //GETTERS
    public TextValueStorage getTextValueStorage() {
        return textValueStorage;
    }

    public BaseModel getModel(){
        return serviceHandler.getModel();
    }

    //ask the user for permission


//permissions
    public final int ACCESS_FINE_LOCATION_CODE = 100;
    public final int STORAGE_PERMISSION_CODE = 101;
}
