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
            //create the service
            startRegistration();



            //create a service


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
   @SuppressLint("MissingPermission")
   private void startRegistration() {
      //permissions
      PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.connectionPermission,Manifest.permission.ACCESS_FINE_LOCATION,PackageManager.PERMISSION_GRANTED));

      //  Create a string map containing information about your service.
      Map record = new HashMap();
      record.put("listenport", String.valueOf(5777));
      record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
      record.put("available", "visible");

      // Service information.  Pass it an instance name, service type
      // _protocol._transportlayer , and the map containing
      // information other devices will want once they connect to this one.
      WifiP2pDnsSdServiceInfo serviceInfo =
              WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

      // Add the local service, sending the service info, network channel,
      // and listener that will be used to indicate success or failure of
      // the request.
      reciever.getWifiP2pManager().addLocalService(reciever.getChannel(), serviceInfo, new WifiP2pManager.ActionListener() {
         @Override
         public void onSuccess() {
            // Command successful! Code isn't necessarily needed here,
            // Unless you want to update the UI or add logging statements.
            TextChanged.invoke(new TextChangedEventArgs(self,EVT.h_service_created,"Service creation succesful"));
         }

         @Override
         public void onFailure(int arg0) {
            // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            TextChanged.invoke(new TextChangedEventArgs(self,EVT.h_service_created,"Service creation failed"));

         }
      });
   }

}



//group creation (backup plan)
          /*  reciever.getWifiP2pManager().createGroup(reciever.getChannel(), new WifiP2pManager.ActionListener() {
               @Override
               public void onSuccess() {
                  GroupConnectionChangedEvent.invoke(new BooleanEventArgs(this, EVT.host_group_creation, true));
                  // Device is ready to accept incoming connections from peers.
               }

               @Override
               public void onFailure(int reason) {
                  GroupConnectionChangedEvent.invoke(new BooleanEventArgs(this, EVT.host_group_creation, false));

               }
            });*/
