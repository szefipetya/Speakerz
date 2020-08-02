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

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.event.HostAddressEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.threads.ClientControllerSocketThread;
import com.speakerz.model.network.threads.ClientSocketWrapper;
import com.speakerz.model.network.threads.audio.ClientAudioMultiCastReceiverSocketThread;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventListener;

import java.util.ArrayList;
import java.util.List;

public class DeviceNetwork extends BaseNetwork {

ClientSocketWrapper clientSocketWrapper =new ClientSocketWrapper();
Event<EventArgs1<Body>> MetaInfoReceivedEvent=new Event<>();
    public DeviceNetwork(WifiBroadcastReciever reciever) {
        super(reciever);
        clientSocketWrapper.controllerSocket = new ClientControllerSocketThread();
        clientSocketWrapper.audioSocket = new ClientAudioMultiCastReceiverSocketThread();
        subscribeSocketEvents();
        reciever.HostAddressAvailableEvent.addListener(new EventListener<HostAddressEventArgs>() {
            @Override
            public void action(HostAddressEventArgs args) {
                if(!args.isHost()) {

                    clientSocketWrapper.controllerSocket.setAddress(args.getAddress());
                    clientSocketWrapper.controllerSocket.start();

                    clientSocketWrapper.audioSocket.setAddress(args.getAddress());
                    clientSocketWrapper.audioSocket.start();

                }
            }
        });

    }

    private void subscribeSocketEvents(){

    }

    public void discoverPeers() {
       // PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this,PERM.ACCESS_COARSE_LOCATION,));
        PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,PackageManager.PERMISSION_GRANTED));

        reciever.discoverPeers(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Discovering..."));
            }

            @Override
            public void onFailure(int i) {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Discovering init failed..."));
            }
        });

        peers = new ArrayList<>();
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                D.log("Peers available");
                //if the saved list is outdated, replace it with the fresh devices

                    D.log("Found peers: " + peerList.getDeviceList().size());
                    peers.clear();
                    peers.addAll(peerList.getDeviceList());

                    deviceNames.clear();
                    devices = new WifiP2pDevice[peerList.getDeviceList().size()];

                    int index = 0;
                    for (WifiP2pDevice device : peerList.getDeviceList()) {
                        deviceNames.add(device.deviceName);
                        devices[index] = device;
                        index++;
                        D.log("device found: " + device.deviceName);
                    }

                    ListChanged.invoke(new EventArgs(this));

                if (peers.size() == 0) {
                    TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "No devices found"));
                } else {
                    TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Found some devices"));
                }
            }
        };
        reciever.setPeerListListener(peerListListener);
    }

    public List<String> getDeviceNames() {
        return deviceNames;
    }

    public ClientSocketWrapper getClientSocketWrapper() {
        return clientSocketWrapper;
    }

    private WifiP2pDevice hostDevice = null;
    private WifiP2pConfig hostConnectionConfig = null;

    /**
     * This function connects a client to a host by giving an index.
     * We can use that index to find the device in the devices list
     * @param i this param descibed the index of the selected device in the deviceList
     */
    @SuppressLint("MissingPermission")
    private void removeGroupIfExists(final int i){
        reciever.getWifiP2pManager().requestGroupInfo(reciever.getChannel(), new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                    reciever.getWifiP2pManager().removeGroup(reciever.getChannel(), new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                           D.log("group removed");
                           connectWithNoGroup(i);
                        }

                        @Override
                        public void onFailure(int reason) {
                            D.log("group removing failed. reason: " + reason);
                        }
                    });
                } else {
                  D.log("no group found");
                    connectWithNoGroup(i);
                }
            }
        });
    }
    public void connectWithNoGroup(final int i){
        hostDevice = devices[i];
        hostConnectionConfig = new WifiP2pConfig();
        hostConnectionConfig.deviceAddress = hostDevice.deviceAddress;
        // make sure, this device does not become a groupowner
        hostConnectionConfig.wps.setup = WpsInfo.PBC;

        hostConnectionConfig.groupOwnerIntent=0;
        PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.connectionPermission,Manifest.permission.ACCESS_FINE_LOCATION,PackageManager.PERMISSION_GRANTED));

    }

    public void connect(int i) {
        removeGroupIfExists(i);



        //send an invoke to the service, to check the FINE_LOCATION access permission



    }
    @SuppressLint("MissingPermission")
    public void connectWithPermissionGranted(){
        hostConnectionConfig.groupOwnerIntent=0;
      /*  reciever.getWifiP2pManager().cancelConnect(reciever.getChannel(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                connectWithClearedPending();
            }

            @Override
            public void onFailure(int i) {
                connectWithClearedPending();
            }
        });*/
        connectWithClearedPending();

    }

    @SuppressLint("MissingPermission")
    private void connectWithClearedPending(){
        reciever.getWifiP2pManager().connect(reciever.getChannel(), hostConnectionConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextChanged.invoke(new TextChangedEventArgs(this,EVT.update_host_name,hostDevice.deviceName));
                //D.log("from deviceNetwork: success,  new host: "+hostDevice.deviceName);

            }

            @Override
            public void onFailure(int i) {
                TextChanged.invoke(new TextChangedEventArgs(this,EVT.update_host_name_failed,"errcode: "+i));
            }
        });
    }

    public WifiP2pDevice getHostDevice() {
        return hostDevice;
    }
}
