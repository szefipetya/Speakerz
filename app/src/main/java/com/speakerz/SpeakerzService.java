package com.speakerz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.view.Display;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.speakerz.debug.D;
import com.speakerz.model.BaseModel;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.HostModel;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.network.DeviceNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventListener;
import com.speakerz.viewModel.TextValueStorage;

import org.w3c.dom.Text;

import ealvatag.audio.exceptions.CannotReadException;

public class SpeakerzService extends Service {


    public final int PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE =104 ;

    private final class ServiceHandler extends Handler {
        private  SpeakerzService service;
        private BaseModel model = null;
        private int startId = -1;


        public ServiceHandler(Looper looper, SpeakerzService service) {
            super(looper);
            this.service = service;
            //D.log("servicehandler created");
        }

        @SuppressLint("MissingPermission")
        private void startService(boolean isHost, int sId){


          /*  if(isHost) {
                D.log("hostmodel created");
                model = new HostModel(service.receiver, service.connectivityManager);
            }
            else {
                D.log("devicemodel created");
                model=new DeviceModel(service.receiver, service.connectivityManager);
            }*/

            if(isHost && (model==null || model instanceof DeviceModel)){
                if(model!=null){stopService("changing service type");}

                model = new HostModel(service, service.receiver,service.connectivityManager,PermissionCheckEvent);
                model.start();
                model.getMusicPlayerModel().loadAudio();


                registerReceiver(model.getNetwork().getReciever(), model.getNetwork().getIntentFilter());


                startId = sId;
                ModelReadyEvent.invoke(new BooleanEventArgs(service,isHost));
                this.subscribeEvents();

                D.log("hostmodel created");
            }
            else if(!isHost&&(model==null || model instanceof HostModel)){
                if(model!=null){stopService("changing service type");}

                model = new DeviceModel(service, service.receiver,service.connectivityManager,PermissionCheckEvent);
                registerReceiver(model.getNetwork().getReciever(), model.getNetwork().getIntentFilter());
                model.start();
                model.getMusicPlayerModel().loadAudio();


                startId = sId;
                ModelReadyEvent.invoke(new BooleanEventArgs(service,isHost));
                this.subscribeEvents();
                D.log("devicemodel created");
            }
            else { // nem kell service csere
                stopSelf(startId);
                startId = sId;
            }




        }


        public void stopService(String msg){
            D.log("Stopping service: " + startId);
            if(model == null) return;
            unregisterReceiver(model.getNetwork().getReciever());
          //  model.getNetwork().getReciever().abortBroadcast();
            model.stop();
            stopSelf(startId);
            startId = -1;


            model = null;
            System.gc();
            Toast.makeText(service,"speakerZ service shutdown\nreason: "+msg,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void handleMessage(Message msg) {
            /*if(model != null){
                stopService("model already exists");
            }*/
            D.log("starting service");
            startService(msg.arg2 == 1, msg.arg1);
        }

        private void subscribeEvents(){


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

    private static final int NOTIFICATION_ID = 1;
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel( String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility( Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }
    private void showForegroundNotification(String contentText) {
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

         String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                 createNotificationChannel("my_service", "My Background Service") :
                 "";

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_m_playbutton)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
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

        showForegroundNotification("SpeakerZ service");
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }



    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SpeakerzService getService() {
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
        serviceHandler.stopService("terminated by system");
        D.log("onDestroy");
        super.onDestroy();
    }

    public void onUserDestroy() {
        serviceHandler.stopService("terminated by user");
        D.log("onUserDestroy");
        super.onDestroy();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        D.log("ontaskRemoved");
        serviceHandler.stopService("terminated by user");

        //STOP SERVICE OR WHATEVER YOU WANT
        super.onTaskRemoved(rootIntent);

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
    public final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 102;
    public final int PERMISSIONS_REQUEST_CODE_READ_EXTERNAL_STORAGE= 103;


}
