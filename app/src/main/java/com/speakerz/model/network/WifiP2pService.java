package com.speakerz.model.network;

import android.net.wifi.p2p.WifiP2pDevice;

public class WifiP2pService {
    public static final int SERVICE_STATUS_IDLE=-1;
    public static final int SERVICE_STATUS_DISCONNECTED=0;
    public static final int SERVICE_STATUS_CONNECTING=1;
    public static final int SERVICE_STATUS_CONNECTED=2;
    public static final int SERVICE_STATUS_CONNECTION_FAILED=3;
    public static final int SERVICE_STATUS_CONNECTION_FAILED_WAIT =4 ;

    public int getId() {
        return id;
    }

    private int id;
    public String hostName=null;
    public int connectionStatus=SERVICE_STATUS_IDLE;
    public String modelName;
    WifiP2pDevice device;
    String instanceName = null;
    String serviceRegistrationType = null;

    public WifiP2pService(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return
                hostName+" - "+modelName
               ;
    }
}