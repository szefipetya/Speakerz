package com.speakerz.model.network.event;

import com.speakerz.util.EventArgs;

import java.net.InetAddress;

public class HostAddressEventArgs extends EventArgs {
    InetAddress address;
    boolean isHost;

    public InetAddress getAddress() {
        return address;
    }

    public boolean isHost() {
        return isHost;
    }

    public HostAddressEventArgs(Object _sender, InetAddress address, boolean isHost) {
        super(_sender);
        this.address = address;
        this.isHost=isHost;
    }

    public HostAddressEventArgs(Object _sender) {
        super(_sender);
    }
}
