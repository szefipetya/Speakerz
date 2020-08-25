package com.speakerz.model.network;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.speakerz.debug.D;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.HostAddressEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.util.Event;

import java.net.InetAddress;


public class WifiBroadcastReciever extends BroadcastReceiver {
    public Event<WirelessStatusChangedEventArgs> WirelessStatusChanged = new Event<>();
    public Event<BooleanEventArgs> ConnectionChangedEvent = new Event<>();
    public Event<PermissionCheckEventArgs> PermissionCheckEvent = new Event<>();
    public Event<HostAddressEventArgs> HostAddressAvailableEvent = new Event<>();
    private WifiBroadcastReciever self = this;
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;

    private ConnectivityManager connectivityManager;
    private boolean isHost =false;

    public void setHost(boolean host) {
        isHost = host;
    }

    private WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            //inform the view about the connection changes

         InetAddress hostAddress=info.groupOwnerAddress;
            D.log("onCOnnectionInfoavailable "+hostAddress);
            // After the group negotiation, we can determine the group owner
            // (server).
            if (info.groupFormed && info.isGroupOwner&&isHost) {
                D.log("owner");
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a group owner thread and accepting
                // incoming connections.
                // ConnectionChangedEvent.invoke(new ConnectionChangedEventArgs(self, wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner));
                HostAddressAvailableEvent.invoke(new HostAddressEventArgs(self,info.groupOwnerAddress,true));
            } if (info.groupFormed&&!isHost) {
                if (info.groupOwnerAddress != null) {
                    D.log("client " + info.groupOwnerAddress);
                    HostAddressAvailableEvent.invoke(new HostAddressEventArgs(self,info.groupOwnerAddress,false));
                    // The other device acts as the peer (client). In this case,
                    // you'll want to create a peer thread that connects
                    // to the group owner.
                }

            }
        }
    };


    @SuppressLint("MissingPermission")
    public void discoverPeers(WifiP2pManager.ActionListener actionListener) {

        wifiP2pManager.discoverPeers(channel, actionListener);
    }


    public void setPeerListListener(WifiP2pManager.PeerListListener peerListListener) {
        this.peerListListener = peerListListener;
    }

    private WifiP2pManager.PeerListListener peerListListener;


    public WifiBroadcastReciever(WifiManager wifiManager, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.wifiManager = wifiManager;
        this.wifiP2pManager = manager;
        this.channel = channel;
        initWifip2pManager();
    }

    @SuppressLint("MissingPermission")
    private void initWifip2pManager() {
      //ACCESS_FINE_LOCATION is required
        wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                String groupPassword;
                if (group!=null)
                     groupPassword = group.getPassphrase();
            }
        });
    }

    @SuppressLint("MissingPermission")
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
           }else {}//D.log("err: wifip2pmanager was null");
           D.log("new peers available.");
        }
       else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
           //D.log("connection changed action");

            if (wifiP2pManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // We are connected with the other device, request connection
                // info to find group owner IP
                D.log("connected ");
                wifiP2pManager.requestConnectionInfo(channel, connectionInfoListener);

            }else{
                //disconnected
                D.log("disconnected");
                ConnectionChangedEvent.invoke(new BooleanEventArgs(self,false));
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }
    }


    //getters
    public WifiP2pManager getWifiP2pManager() {
        return wifiP2pManager;
    }

    public WifiP2pManager.Channel getChannel() {
        return channel;
    }
    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public void setConnectivityManager(ConnectivityManager connectivityManager) {
        this.connectivityManager = connectivityManager;


    }

    public ConnectivityManager getConnectivityManager() {
        return connectivityManager;
    }

    public void clearConnections() {
        wifiP2pManager.clearServiceRequests(channel,null);
        wifiP2pManager.clearLocalServices(channel,null);
        wifiP2pManager.cancelConnect(channel,null);
        wifiP2pManager.removeGroup(channel,null);

    }
}
