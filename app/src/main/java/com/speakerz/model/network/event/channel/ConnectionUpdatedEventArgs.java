package com.speakerz.model.network.event.channel;

import com.speakerz.util.EventArgs;

public class ConnectionUpdatedEventArgs extends EventArgs {
    private String hostNickName;
    private String message;
    private String address;

    public String getHostNickName() {
        return hostNickName;
    }

    public String getAddress() {
        return address;
    }

    public String getMessage() {
        return message;
    }

    public ConnectionUpdatedEventArgs(Object _sender, String hostNickName, String message, String address) {
        super(_sender);
        this.hostNickName = hostNickName;
        this.message = message;
        this.address = address;
    }

    public ConnectionUpdatedEventArgs(Object _sender, String hostNickName) {
        super(_sender);
        this.hostNickName = hostNickName;
    }

    public ConnectionUpdatedEventArgs(Object _sender) {
        super(_sender);
    }
}
