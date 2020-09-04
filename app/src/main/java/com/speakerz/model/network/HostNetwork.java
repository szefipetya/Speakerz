package com.speakerz.model.network;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.InetAddresses;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Build;

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
import java.util.HashMap;
import java.util.Map;

public class HostNetwork extends BaseNetwork {
    private static final String SERVICE_REG_TYPE ="REG_SPEAKERZ" ;
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
       serverSocketWrapper.controllerSocket.setAddress(addr);
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
    private void startRegistration() {
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        record.put("host_name", nickName);
        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        reciever.getWifiP2pManager().addLocalService(reciever.getChannel(), service, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                D.log("Added Local Service");
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Party Created successfully"));


            }

            @Override
            public void onFailure(int error) {
                D.log("Failed to add a service");
            }
        });
    }
   @SuppressLint("MissingPermission")
   private void createGroup(){
    //  startRegistration();
       advertiseMe();
       //if device is under 6
      /* if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
           reciever.getWifiP2pManager().createGroup(reciever.getChannel(), new WifiP2pManager.ActionListener() {
               @Override
               public void onSuccess() {
                   TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Party Created successfully"));
               }

               @Override
               public void onFailure(int reason) {
                   TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Could not create the Party"));

               }
           });


       }*/
   }

   @SuppressLint("MissingPermission")
   public void advertiseMe(){
       getReciever().getWifiP2pManager().discoverPeers(getReciever().getChannel(), new WifiP2pManager.ActionListener() {
           @Override
           public void onSuccess() {
               D.log("advertising...");
               TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status,"Advertising... "));

           }

           @Override
           public void onFailure(int i) {
               D.log("advertising init failed");
              TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status,"Advertise init failed"));

           }
       });
   }

   @SuppressLint("MissingPermission")
   public void removeGroupIfExists() {
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
