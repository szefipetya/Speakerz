package com.speakerz.model.network;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.event.EventHandler;
import com.speakerz.model.event.UpdateEventManager;
import com.speakerz.R;
import com.speakerz.util.Event;

import java.util.List;


public abstract class BaseNetwork  {

    protected WifiManager wifiManager;
    protected WifiBroadcastReciever reciever;
    protected IntentFilter intentFilter;

    public Event<TextChangedEventArgs> TextChanged = new Event<>();

    List<WifiP2pDevice> peers;
    String[] deviceNames = new String[1];
    WifiP2pDevice[] devices = new WifiP2pDevice[1];

    public WifiBroadcastReciever getReciever() {
        return reciever;
    }

    WifiP2pManager.PeerListListener peerListListener;
    UpdateEventManager updateEventManagerToModel;

    public BaseNetwork(WifiBroadcastReciever reciever) {
        this.reciever = reciever;
        if(reciever!=null) {
            reciever.setPeerListListener(peerListListener);
        }
        else{
            D.log("err: reviecer was null.");
        }
    }

    public void addUpdateEventListener(EventHandler event) {
        updateEventManagerToModel.addListener(event);
        D.log("event added");
    }

    public void start() {
        D.log("network sent msg");
    }

    public void initP2pWifiManager() {
    }

    /**
     * initializes the wifiManager and turns on the wifi adapter.
     * Sends a signal to the view trough the model according to its status.
     *
     */
    public void initWifiManager() {
        if (!wifiManager.isWifiEnabled()) {
            //bekapcsoljuk a wifit
            wifiManager.setWifiEnabled(true);
        } else {

        }
    }

    //SETTERS

    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }


    public void setIntentFilter(IntentFilter intentFilter) {
        this.intentFilter=intentFilter;
    }

}
