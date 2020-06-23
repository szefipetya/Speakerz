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
import com.speakerz.util.Event;

import java.util.ArrayList;

public class HostNetwork extends BaseNetwork {
   public HostNetwork(WifiBroadcastReciever reciever){
      super(reciever);
   }


   public void startAdvertising() {
      reciever.discoverPeers(new WifiP2pManager.ActionListener() {
         @Override
         public void onSuccess() {
            TextChanged.invoke(new TextChangedEventArgs(this, "Advertising..."));
         }

         @Override
         public void onFailure(int i) {
            TextChanged.invoke(new TextChangedEventArgs(this, "Advertising init failed..."));
         }
      });

      peers=new ArrayList<WifiP2pDevice>();
      peerListListener=new WifiP2pManager.PeerListListener() {
         @Override
         public void onPeersAvailable(WifiP2pDeviceList peerList) {

         }
      };
      reciever.setPeerListListener(peerListListener);
   }
}
