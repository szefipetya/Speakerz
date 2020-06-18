package com.app.speakerz.model.network;


import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.speakerz.debug.D;
import com.app.speakerz.model.enums.EVT;
import com.example.speakerz.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceNetwork extends BaseNetwork {


    public DeviceNetwork(){

    }

    public void discoverPeers(final AppCompatActivity activity, final ListView lvPeersList) {
        getWifiP2pManager().discoverPeers(wifiP2pChannel,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                updateEventManagerToModel.updateAll(EVT.updateText, R.id.discover_status,"Discovering...");
            }

            @Override
            public void onFailure(int i) {
                updateEventManagerToModel.updateAll(EVT.updateText, R.id.discover_status,"Discovery failed...");

            }
        });

        peers=new ArrayList<WifiP2pDevice>();
        peerListListener=new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                D.log("Peers available");
                //if the saved list is outdated, replace it with the fresh devices
                if(!peerList.getDeviceList().equals(peers)){

                    D.log("Peers and not equals"+peerList.getDeviceList().size());
                    peers.clear();
                    peers.addAll(peerList.getDeviceList());

                    deviceNames=new String[peerList.getDeviceList().size()];
                    devices=new WifiP2pDevice[peerList.getDeviceList().size()];
                    int index=0;
                    for(WifiP2pDevice device : peerList.getDeviceList()){
                        deviceNames[index]=device.deviceName;
                        devices[index]=device;
                        index++;
                        D.log("device found");
                    }
                    ArrayAdapter<String> adapter=new ArrayAdapter<>(activity.getApplicationContext(),android.R.layout.simple_list_item_1,deviceNames);
                    lvPeersList.setAdapter(adapter);

                }
                if(peers.size()==0){
                    Toast.makeText(activity.getApplicationContext(),"No Devices found",Toast.LENGTH_SHORT).show();
                }
            }
        };
        reciever.setPeerListListener(peerListListener);
    }

    public String[] getDeviceNames() {
        return deviceNames;
    }

    //SETTERS
    @Override
    public void setWifiP2pChannel(WifiP2pManager.Channel wifiP2pChannel) {
        this.wifiP2pChannel=wifiP2pChannel;
    }
    @Override
    public void setIntentFilter(IntentFilter intentFilter) {
        this.intentFilter=intentFilter;

    }
    @Override
    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }
    @Override
    public void setWifiP2pManager(WifiP2pManager wifiP2pManager) {
        this.wifiP2pManager = wifiP2pManager;
    }
    @Override
    public void setWifiBroadcastReciever(WifiBroadcastReciever reciever) {
        this.reciever=reciever;
        reciever.addEventHandlerToUpdateManager(this);
        if(reciever!=null) {
            reciever.setPeerListListener(peerListListener);
            reciever.setChannel(this.wifiP2pChannel);
            reciever.setWifiP2pManager(wifiP2pManager);
        }
        else{
            D.log("err: reviecer was null.");}
    }
}
