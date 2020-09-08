package com.speakerz.model.network;

import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.model.enums.EVT;
import com.speakerz.model.event.EventHandler;
import com.speakerz.model.event.UpdateEventManager;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.event.channel.ConnectionUpdatedEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseNetwork  {

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    protected String nickName;
    public Event<EventArgs1<Exception>> ExceptionEvent;
    protected WifiBroadcastReciever reciever;

    public IntentFilter getIntentFilter() {
        return intentFilter;
    }

    protected IntentFilter intentFilter;
    public Event<TextChangedEventArgs> TextChanged=null;
    public Event<EventArgs> ListChanged = new Event<>();
    //@Injected
    public Event<PermissionCheckEventArgs> PermissionCheckEvent =null;
    //Music player events
    //

    List<WifiP2pDevice> peers;
    List<String> deviceNames = new ArrayList<>();
    WifiP2pDevice[] devices = new WifiP2pDevice[0];

    public WifiBroadcastReciever getReciever() {
        return reciever;
    }

    WifiP2pManager.PeerListListener peerListListener;
    UpdateEventManager updateEventManagerToModel;

    public void setTextChanged(Event<TextChangedEventArgs> textChanged) {
        TextChanged = textChanged;
    }

    public BaseNetwork(WifiBroadcastReciever reciever) {
        this.reciever = reciever;
        if(reciever!=null) {
            reciever.setPeerListListener(peerListListener);
        }
        else{
            //D.log("err: reviecer was null.");
        }
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public void addUpdateEventListener(EventHandler event) {
        updateEventManagerToModel.addListener(event);
        //D.log("event added");
    }

    public void start() {
        //D.log("network started");
        initWifiManager();
    }

    /**
     * initializes the wifiManager and turns on the wifi adapter.
     * Sends a signal to the view trough the model according to its status.
     *
     */
    public void initWifiManager() {
        if (!reciever.getWifiManager().isWifiEnabled()) {
            //bekapcsoljuk a wifit
            reciever.getWifiManager().setWifiEnabled(true);
            TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_wifi_status,"Turning on wifi..."));
        } else {

        }
    }
    protected String SERVICE_INSTANCE="SPEAKERZ";
    protected String TXTRECORD_PROP_AVAILABLE="AVAILABLE";


    //SETTERS

}
