package com.app.speakerz.model.network;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.app.speakerz.debug.D;
import com.app.speakerz.model.enums.EVT;
import com.app.speakerz.model.event.EventHandler;
import com.app.speakerz.model.event.UpdateEventManager;
import com.example.speakerz.R;

import java.util.List;


public abstract class BaseNetwork implements EventHandler {

    protected WifiP2pManager wifiP2pManager;
    protected WifiManager wifiManager;
    protected WifiP2pManager.Channel wifiP2pChannel;
    protected WifiBroadcastReciever reciever;
    protected IntentFilter intentFilter;


    List<WifiP2pDevice> peers;
    String[] deviceNames = new String[1];
    WifiP2pDevice[] devices = new WifiP2pDevice[1];
    WifiP2pManager.PeerListListener peerListListener;
    UpdateEventManager updateEventManagerToModel;


    public void init() {
        updateEventManagerToModel = new UpdateEventManager();
    }

    public void init(EventHandler e) {
        init();
        addUpdateEventListener(e);

    }

    /**
     * api doc holy shit
     *
     * @param event event ezt csinálja
     */
    public void addUpdateEventListener(EventHandler event) {
        updateEventManagerToModel.addListener(event);
        D.log("event added");
    }

    public void start() {


        D.log("network sent msg");
    }

    public BaseNetwork() {






    }

    public void initP2pWifiManager() {
    }

    /**
     * initializes the wifiManager and turns on the wifi adapter.
     * Sends a signal to the view trough the model according to its status.
     *
     */
    public void initWifiManager() {
        if (!getWifiManager().isWifiEnabled()) {
            //bekapcsoljuk a wifit
            getWifiManager().setWifiEnabled(true);
        } else {

        }
    }


    @Override
    public void onUpdate(EVT type, Object o) {

    }

    @Override
    public void onUpdate(EVT evt, Object o, Object o2) {
        if (evt == EVT.updateText) {
            updateEventManagerToModel.updateAll(evt, o, o2);
        }
    }

    @Override
    public void onUpdate(EVT type, Object o, Object o2, Object o3) {

    }

    @Override
    public void onUpdate(EVT type, Object o, Object o2, Object o3, Object o4) {

    }

    //SETTERS
    abstract public void setWifiP2pChannel(WifiP2pManager.Channel wifiP2pChannel);

    abstract public void setIntentFilter(IntentFilter intentFilter);

    abstract public void setWifiManager(WifiManager wifiManager);

    abstract public void setWifiP2pManager(WifiP2pManager wifiP2pManager);

    abstract public void setWifiBroadcastReciever(WifiBroadcastReciever reciever);

    //GETTERS
    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public WifiP2pManager getWifiP2pManager() {
        return wifiP2pManager;
    }


}
