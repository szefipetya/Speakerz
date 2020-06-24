package com.speakerz.model.network;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.speakerz.App.App;
import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.event.EventHandler;
import com.speakerz.model.event.UpdateEventManager;
import com.speakerz.R;
import com.speakerz.util.Event;


public class WifiBroadcastReciever extends BroadcastReceiver {
    public Event<WirelessStatusChangedEventArgs> WirelessStatusChanged = new Event<>();

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;


    public void discoverPeers(WifiP2pManager.ActionListener actionListener) {
        if (ActivityCompat.checkSelfPermission(App.instance, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            //return;
        }
        wifiP2pManager.discoverPeers(channel, actionListener);
    }


    public void setPeerListListener(WifiP2pManager.PeerListListener peerListListener) {
        this.peerListListener = peerListListener;
    }

    private WifiP2pManager.PeerListListener peerListListener;

    public WifiBroadcastReciever(WifiP2pManager manager, WifiP2pManager.Channel channel){
        this.wifiP2pManager=manager;
        this.channel=channel;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state=intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);

            WirelessStatusChanged.invoke(new WirelessStatusChangedEventArgs(this, state==WifiP2pManager.WIFI_P2P_STATE_ENABLED));

            if(state==WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Toast.makeText(context,"Wifi is on", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context,"Wifi is off", Toast.LENGTH_SHORT).show();
            }
        }
       else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
           if(wifiP2pManager!=null){
                wifiP2pManager.requestPeers(channel,peerListListener);
           }else D.log("err: wifip2pmanager was null");
           D.log("Peers changed action");
        }
       else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }
    }


}
