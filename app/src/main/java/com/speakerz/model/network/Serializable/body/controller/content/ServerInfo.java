package com.speakerz.model.network.Serializable.body.controller.content;

import java.io.Serializable;

public class ServerInfo implements Serializable {
    public ServerInfo(String hostName, String message) {
        this.hostName = hostName;
        this.message = message;
    }

    private String hostName;
    private String message;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
