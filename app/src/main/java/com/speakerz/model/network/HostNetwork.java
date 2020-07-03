package com.speakerz.model.network;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;

import androidx.core.app.ActivityCompat;

import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HostNetwork extends BaseNetwork {
   public HostNetwork(WifiBroadcastReciever reciever) {
      super(reciever);
   }

   public Event<BooleanEventArgs> GroupConnectionChangedEvent = new Event<>();


   public void startAdvertising() {
      reciever.discoverPeers(new WifiP2pManager.ActionListener() {
         //  @SuppressLint("MissingPermission")
         @SuppressLint("MissingPermission")
         @Override
         public void onSuccess() {
            TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status,"Advertising..."));
            //this makes sure that ACESS_FINE_LOCATION is enabled
            PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.connectionPermission,Manifest.permission.ACCESS_FINE_LOCATION,PackageManager.PERMISSION_GRANTED));
            //create a ServerSide thread

         }

         @Override
         public void onFailure(int i) {
            TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status,"Advertising init failed..."));
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

   HostNetwork self=this;


}
