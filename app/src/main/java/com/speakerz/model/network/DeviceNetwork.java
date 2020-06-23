package com.speakerz.model.network;


import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.app.Activity;

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceNetwork extends BaseNetwork {


    public DeviceNetwork(WifiBroadcastReciever reciever){
        super(reciever);
    }

    public void discoverPeers(final Activity activity, final ListView lvPeersList) {
        reciever.discoverPeers(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextChanged.invoke(new TextChangedEventArgs(this, "Discovering..."));
            }

            @Override
            public void onFailure(int i) {
                TextChanged.invoke(new TextChangedEventArgs(this, "Discovering init failed..."));
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

}
