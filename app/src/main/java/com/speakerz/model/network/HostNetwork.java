package com.speakerz.model.network;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.HostAddressEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.event.channel.MusicPlayerActionEventArgs;
import com.speakerz.model.network.threads.ServerControllerSocketThread;
import com.speakerz.model.network.threads.ServerSocketWrapper;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.dom.DOMLocator;

public class HostNetwork extends BaseNetwork {
   public HostNetwork(WifiBroadcastReciever reciever) {
      super(reciever);
      reciever.HostAddressAvailableEvent.addListener(new EventListener<HostAddressEventArgs>() {
         @Override
         public void action(HostAddressEventArgs args) {
            if(args.isHost()) {
               D.log("Starting serverController thread...");
               serverSocketWrapper.controllerSocket = new ServerControllerSocketThread();
               serverSocketWrapper.controllerSocket.start();
               ControllerSocketEstablishedEvent.invoke(new EventArgs(self));
            }
         }
      });
   }

   public ServerSocketWrapper getServerSocketWrapper() {
      return serverSocketWrapper;
   }



   Event<MusicPlayerActionEventArgs> MusicPlayerActionEvent=new Event<>();

   ServerSocketWrapper serverSocketWrapper=new ServerSocketWrapper();

   public Event<BooleanEventArgs> GroupConnectionChangedEvent = new Event<>();





   private  void advertiseMySelf(){
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

   @SuppressLint("MissingPermission")
   private void createGroup(){
      reciever.getWifiP2pManager().createGroup(reciever.getChannel(), new WifiP2pManager.ActionListener() {
         @Override
         public void onSuccess() {
            // Device is ready to accept incoming connections from peers.
            D.log("group created succesfully");
            TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status,"Group Created succesfully"));
            //this makes sure that ACESS_FINE_LOCATION is enabled
            PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.connectionPermission,Manifest.permission.ACCESS_FINE_LOCATION,PackageManager.PERMISSION_GRANTED));

            //advertiseMySelf();
         }

         @Override
         public void onFailure(int reason) {
            D.log("group creation failed");
            TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status,"Failed to create group"));
         }
      });
   }
   @SuppressLint("MissingPermission")
   public void startGroup() {
    //  WifiP2pConfig config=new WifiP2pConfig();
      //config.groupOwnerIntent=15;
      //config.wps.setup= WpsInfo.PBC;



      reciever.getWifiP2pManager().requestGroupInfo(reciever.getChannel(), new WifiP2pManager.GroupInfoListener() {
                 @Override
                 public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null) {
                       reciever.getWifiP2pManager().removeGroup(reciever.getChannel(), new WifiP2pManager.ActionListener() {
                          @Override
                          public void onSuccess() {
                             createGroup();
                          }

                          @Override
                          public void onFailure(int reason) {
                             D.log("group creation failed. reason: " + reason);
                          }
                       });
                    } else {
                       createGroup();

                    }
                 }
              });
      //group creation end

   }

   HostNetwork self=this;


}
