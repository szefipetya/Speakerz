package com.speakerz.App;

import android.app.Application;
import android.app.Service;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;

import android.app.Activity;

import com.speakerz.MainActivity;
import com.speakerz.model.BaseModel;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.HostModel;
import com.speakerz.model.event.Model_ViewEventHandler;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.viewModel.TextValueStorage;

public class App extends Application {
    private static BaseModel model;
    private static final TextValueStorage textValueStorage = new TextValueStorage();
    private static WifiManager wifiManager;
    private static WifiP2pManager wifiP2pManager;
    private static WifiP2pManager.Channel wifiP2pChannel;
    private static WifiBroadcastReciever reciever;
    public static IntentFilter intentFilter;

    public static void setChannel(MainActivity mainActivity, Looper mainLooper, WifiP2pManager.ChannelListener cl) {
        wifiP2pChannel=wifiP2pManager.initialize(mainActivity,mainLooper,cl);
    }

    public static void jStartDiscovering(Activity activity, ListView lvPeersList) {
        if(model!=null){
            ((DeviceModel)model).discoverPeers(activity,lvPeersList);
        }
    }
    public static void hStartAdvertising() {
        if(model!=null){
            ((HostModel)model).startAdvertising();
        }
    }

    public static TextValueStorage getTextValueStorage() {
        return textValueStorage;
    }

    public static String[] jGetDeviceNames(){
        return  ((DeviceModel)model).getDeviceNames();
    }

    //instance
    public static Application instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Your methods here...
    }
    public static BaseModel initModel(boolean isHost){
        if (isHost){
            model=new HostModel(reciever);
        }else {
            model = new DeviceModel(reciever);
        }

        model.setTextValueStorageForViewUpdateEventManager(textValueStorage);
        model.setWifiManager(wifiManager);

        model.setIntentFilter(intentFilter);
        return model;
    }

    public static void initIntentFilter(){
        intentFilter=new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }
    public static void initWifiBroadCastReciever(){

       reciever = new WifiBroadcastReciever(wifiP2pManager,wifiP2pChannel);

    }

    public static void setWifiManagerForModel(WifiManager manager){

    }
    public static void startModel(){
        model.start();
    }


    //auto configurates all the textfields if there is any element exists in the storage
    public static void autoConfigureTexts(Activity act){
        textValueStorage.autoConfigureTexts(act);
    }
    //REQUIRED
    // FIXME SETTERS
    public static void setP2pWifiManager(WifiP2pManager manager){
        wifiP2pManager=manager;
    }
    public static void setWifiManager(WifiManager manager){
        wifiManager=manager;
    }

    //FIXME GETTERS
    public static String getTextFromStorage(Integer id){
           return textValueStorage.getTextValue(id);
    }
    public static WifiManager getWifiManager(){
        return wifiManager;
    }
     public static WifiBroadcastReciever getWifiBroadcastReciever(){
        return reciever;
    }
    public static IntentFilter getIntentFilter(){
        return intentFilter;
    }
    public static  BaseModel getModel(){
        return  model;
    }
}