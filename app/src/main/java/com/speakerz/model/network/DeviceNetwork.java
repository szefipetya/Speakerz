package com.speakerz.model.network;


import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.util.EventArgs;

import java.util.ArrayList;
import java.util.List;

public class DeviceNetwork extends BaseNetwork {


    public DeviceNetwork(WifiBroadcastReciever reciever){
        super(reciever);
    }

    public void discoverPeers() {
        reciever.discoverPeers(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextChanged.invoke(new TextChangedEventArgs(this,EVT.update_discovery_status, "Discovering..."));
            }

            @Override
            public void onFailure(int i) {
                TextChanged.invoke(new TextChangedEventArgs(this,EVT.update_discovery_status,  "Discovering init failed..."));
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

                    deviceNames.clear();
                    devices=new WifiP2pDevice[peerList.getDeviceList().size()];

                    int index=0;
                    for(WifiP2pDevice device : peerList.getDeviceList()){
                        deviceNames.add(device.deviceName);
                        devices[index]=device;
                        index++;
                        D.log("device found: "+device.deviceName);
                    }

                    ListChanged.invoke(new EventArgs(this));

                }
                if(peers.size()==0){
                  TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status,"No devices found"));
                }
            }
        };
        reciever.setPeerListListener(peerListListener);
    }

    public List<String> getDeviceNames() {
        return deviceNames;
    }

}
