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
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.HostAddressEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.threads.ClientControllerSocketThread;
import com.speakerz.model.network.threads.ClientSocketWrapper;
import com.speakerz.model.network.threads.audio.ClientAudioMultiCastReceiverSocketThread;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceNetwork extends BaseNetwork {


    ClientSocketWrapper clientSocketWrapper = new ClientSocketWrapper();
    boolean firstStart = true;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    public DeviceNetwork(WifiBroadcastReciever reciever) {
        super(reciever);
        reciever.setHost(false);

        clientSocketWrapper.controllerSocket = new ClientControllerSocketThread();
        clientSocketWrapper.audioSocket = new ClientAudioMultiCastReceiverSocketThread();
        reciever.HostAddressAvailableEvent.addListener(new EventListener<HostAddressEventArgs>() {
            @Override
            public void action(HostAddressEventArgs args) {
                if (!args.isHost()) {
                    if (!firstStart) {
                        clientSocketWrapper.controllerSocket.shutdown();
                        clientSocketWrapper.audioSocket.shutdown();
                        clientSocketWrapper.audioSocket.stopPlayBack();


                        ClientControllerSocketThread tmp = new ClientControllerSocketThread();
                        tmp.MusicPlayerActionEvent = clientSocketWrapper.controllerSocket.MusicPlayerActionEvent;
                        tmp.MetaInfoReceivedEvent = clientSocketWrapper.controllerSocket.MetaInfoReceivedEvent;
                        tmp.ExceptionEvent = clientSocketWrapper.controllerSocket.ExceptionEvent;
                        tmp.INITDeviceAddressEvent=clientSocketWrapper.controllerSocket.INITDeviceAddressEvent;
                        tmp.NameChangeEvent=clientSocketWrapper.controllerSocket.NameChangeEvent;
                        tmp.NameListInitEvent=clientSocketWrapper.controllerSocket.NameListInitEvent;
                        tmp.DeleteSongEvent=clientSocketWrapper.controllerSocket.DeleteSongEvent;
                        tmp.DeleteSongRequestEvent=clientSocketWrapper.controllerSocket.DeleteSongRequestEvent;
                        clientSocketWrapper.controllerSocket = tmp;
                        ClientAudioMultiCastReceiverSocketThread tmp2 = new ClientAudioMultiCastReceiverSocketThread();
                        tmp2.MusicPlayerActionEvent = tmp.MusicPlayerActionEvent;
                        tmp2.ExceptionEvent = tmp.ExceptionEvent;

                        clientSocketWrapper.audioSocket = tmp2;
                        D.log("its not the first start.");
                    }
                    firstStart = false;
                    clientSocketWrapper.controllerSocket.setAddress(args.getAddress());
                    clientSocketWrapper.controllerSocket.start();
                    clientSocketWrapper.audioSocket.setAddress(args.getAddress());
                    clientSocketWrapper.audioSocket.start();

                }
            }
        });

        initServiceListeners();

        reciever.ConnectionChangedEvent.addListener(new EventListener<BooleanEventArgs>() {
            @Override
            public void action(BooleanEventArgs args) {
                if(!args.getValue()){
                    serviceDevices.clear();
                    ListChanged.invoke(new EventArgs(this));
                }
            }
        });

    }

    DeviceNetwork self = this;



    public void discoverPeers() {
       // serviceDevices.clear();
      //  discoverService();

       // PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,PackageManager.PERMISSION_GRANTED));

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
     // removeGroupIfExists(0);
discoverService();
      /*  peers = new ArrayList<>();
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
        reciever.setPeerListListener(peerListListener);*/
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
     *
     * @param i this param descibed the index of the selected device in the deviceList
     */
    @SuppressLint("MissingPermission")
    private void removeGroupIfExists(final int i) {
        reciever.getWifiP2pManager().requestGroupInfo(reciever.getChannel(), new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                    reciever.getWifiP2pManager().removeGroup(reciever.getChannel(), new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            D.log("group removed");
                          //  connectWithNoGroup(i);
                        }

                        @Override
                        public void onFailure(int reason) {
                            D.log("group removing failed. reason: " + reason);
                        }
                    });
                } else {
                    D.log("no group found");
                   // connectWithNoGroup(i);
                }
            }
        });
    }

    public void connectWithNoGroup(final int i) {
        hostDevice = devices[i];
        hostConnectionConfig = new WifiP2pConfig();
        hostConnectionConfig.deviceAddress = hostDevice.deviceAddress;
        // make sure, this device does not become a groupowner
        hostConnectionConfig.wps.setup = WpsInfo.PBC;

        hostConnectionConfig.groupOwnerIntent = 0;
        connectWithPermissionGranted();
    }

    public void connect(int i) {
       // removeGroupIfExists(i);

        connectP2p(serviceDevices.get(i));
        //send an invoke to the service, to check the FINE_LOCATION access permission


    }

    @SuppressLint("MissingPermission")
    public void connectWithPermissionGranted() {
        hostConnectionConfig.groupOwnerIntent = 0;

        connectWithClearedPending();

    }

    @SuppressLint("MissingPermission")
    private void connectWithClearedPending() {
        reciever.getWifiP2pManager().connect(reciever.getChannel(), hostConnectionConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_host_name, hostDevice.deviceName));
                //D.log("from deviceNetwork: success,  new host: "+hostDevice.deviceName);

            }

            @Override
            public void onFailure(int i) {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_host_name_failed, "errcode: " + i));
            }
        });
    }

    public WifiP2pDevice getHostDevice() {
        return hostDevice;
    }


    final HashMap<String, String> buddies = new HashMap<String, String>();
   public final ArrayList<WifiP2pService> serviceDevices = new ArrayList<>();

    public void createServiceRequest(){
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        reciever.getWifiP2pManager().addServiceRequest(reciever.getChannel(), serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        D.log("Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        D.log("Failed adding service discovery request");
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void discoverService() {
        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */

        // After attaching listeners, create a service request and initiate
        // discovery.
        if(serviceRequest==null) {
            createServiceRequest();
        }else{

        }
        reciever.getWifiP2pManager().discoverServices(reciever.getChannel(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Discovering..."));
                D.log("Service discovery initiated");
            }
            @Override
            public void onFailure(int arg0) {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Discovering failed. errcode: "+arg0));
                if(arg0==WifiP2pManager.NO_SERVICE_REQUESTS)
                D.log("Service discovery failed");
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void connectP2p(WifiP2pService service) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null)
            reciever.getWifiP2pManager().removeServiceRequest(reciever.getChannel(), serviceRequest,
                    new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onFailure(int arg0) {
                        }
                    });
        reciever.getWifiP2pManager().connect(reciever.getChannel(), config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_host_name,latestHostName));
                D.log("Connecting to service");
            }
            @Override
            public void onFailure(int errorCode) {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_host_name_failed, "Connection Failed errcode: "+errorCode));

                D.log("Failed connecting to service");
            }
        });

    }







    private void initServiceListeners() {
        reciever.getWifiP2pManager().setDnsSdResponseListeners(reciever.getChannel(),
                new WifiP2pManager.DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {
                        // A service has been discovered. Is this our app?
                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                            // update the UI and add the item the discovered
                            // device.

                            D.log("srcDevice"+srcDevice);
                            D.log("instanceName"+instanceName);
                            D.log("registrationType"+registrationType);

                            WifiP2pService service = new WifiP2pService();
                            service.hostName=latestHostName;
                            service.device = srcDevice;
                            service.instanceName = instanceName;
                            service.serviceRegistrationType = registrationType;

                            serviceDevices.add(service);
                            ListChanged.invoke(new EventArgs(null));
                            //add to adapter... stb
                        }
                    }

                }, new WifiP2pManager.DnsSdTxtRecordListener() {
                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        latestHostName=(String)record.get("host_name");
                        Log.d("TAG",
                                device.deviceName + " is "
                                        + record.get(TXTRECORD_PROP_AVAILABLE));
                    }
                });

        }
        String latestHostName="host";

}
