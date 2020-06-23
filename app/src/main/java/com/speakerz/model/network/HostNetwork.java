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

public class HostNetwork extends BaseNetwork {



   public HostNetwork(){

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

   public void startAdvertising() {
      getWifiP2pManager().discoverPeers(wifiP2pChannel,new WifiP2pManager.ActionListener() {
         @Override
         public void onSuccess() {
            updateEventManagerToModel.updateAll(EVT.updateText, R.id.discover_status,"Advertising...");
         }

         @Override
         public void onFailure(int i) {
            updateEventManagerToModel.updateAll(EVT.updateText, R.id.discover_status,"Advertising init failed...");

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
