package com.speakerz.model.network;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.InetAddresses;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.HostAddressEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.threads.ServerControllerSocketThread;
import com.speakerz.model.network.threads.ServerSocketWrapper;
import com.speakerz.model.network.threads.audio.ServerAudioMultiCastSocketThread;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventListener;

import java.io.IOException;
import java.net.InetAddress;

public class HostNetwork extends BaseNetwork {
   boolean firstStart=true;
    public HostNetwork(WifiBroadcastReciever reciever) {
      super(reciever);
      serverSocketWrapper.controllerSocket = new ServerControllerSocketThread();

          serverSocketWrapper.audioSocket=new ServerAudioMultiCastSocketThread();

       //this is necessary to create the eventListeners for the serverSocketThread

       reciever.setHost(true);
       reciever.HostAddressAvailableEvent.addListener(new EventListener<HostAddressEventArgs>() {
         @Override
         public void action(HostAddressEventArgs args) {
            if(args.isHost()&&firstStart) {
                startServerThread(args.getAddress());
                firstStart=false;
            }
          }
       });
   }

   private void startServerThread(InetAddress addr){
            serverSocketWrapper.controllerSocket.start();
            serverSocketWrapper.audioSocket.setAddress(addr);
            serverSocketWrapper.audioSocket.start();
   }

   public ServerSocketWrapper getServerSocketWrapper() {
      return serverSocketWrapper;
   }




   ServerSocketWrapper serverSocketWrapper=new ServerSocketWrapper();

   public Event<BooleanEventArgs> GroupConnectionChangedEvent = new Event<>();

   @SuppressLint("MissingPermission")
   private void createGroup(){

      reciever.getWifiP2pManager().createGroup(reciever.getChannel(), new WifiP2pManager.ActionListener(){
         @Override
         public void onSuccess() {
            // Device is ready to accept incoming connections from peers.
            D.log("group created succesfully");
            TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status,"Group Created succesfully"));
            //this makes sure that ACESS_COARSE_LOCATION is enabled
            PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,PackageManager.PERMISSION_GRANTED));

            getReciever().getWifiP2pManager().discoverPeers(getReciever().getChannel(), new WifiP2pManager.ActionListener() {
               @Override
               public void onSuccess() {
                  D.log("advertising...");

               }

               @Override
               public void onFailure(int i) {
                  D.log("advertising init failed");

               }
            });
         }

         @Override
         public void onFailure(int reason) {
            D.log("group creation failed");
            TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status,"Failed to create group"));
         }
      });
   }
   @SuppressLint("MissingPermission")
   public void removeGroupIfExists() {
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
                             D.log("group removed");
                             createGroup();
                          }

                          @Override
                          public void onFailure(int reason) {
                             D.log("group removing failed. reason: " + reason);
                          }
                       });
                    } else {
                       createGroup();
                       D.log("no groups found");
                    }
                 }
              });
      //group creation end

   }

   HostNetwork self=this;
}
