package com.speakerz.model.network.Serializable;

import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.Serializable.enums.TYPE;

import java.io.Serializable;

public class WelcomeChObject implements ChannelObject , Serializable {

    String hostNickName;
    String welcomeMessage;
    String hostAddress;

    public String getHostNickName() {
        return hostNickName;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public WelcomeChObject(String hostNickName, String welcomeMessage, String hostAddress) {
        this.hostNickName = hostNickName;
        this.welcomeMessage = welcomeMessage;
        this.hostAddress = hostAddress;
    }

    @Override
    public WelcomeChObject getObj() {
        return this;
    }

    @Override
    public TYPE getType() {
        return TYPE.WELCOME;
    }

    @Override
    public SUBTYPE getSubType() {
        return null;
    }
}
