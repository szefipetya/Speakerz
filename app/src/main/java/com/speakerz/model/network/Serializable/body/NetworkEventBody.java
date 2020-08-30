package com.speakerz.model.network.Serializable.body;


import com.speakerz.model.network.Serializable.body.controller.content.ServerInfo;
import com.speakerz.model.network.Serializable.enums.NET_EVT;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;

public class NetworkEventBody extends Body implements Serializable {
    public NetworkEventBody(  NET_EVT netEvt){
        this.netEvt =netEvt;
    }
    public NetworkEventBody(String sender, NET_EVT netEvt){
        this.senderAddress=sender;
        this.netEvt =netEvt;
    }


    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.NONE;
    }

    @Override
    public void setContent(Object obj) {
        this.netEvt =(NET_EVT) obj;
    }

    NET_EVT netEvt;

    @Override
    public NET_EVT getContent() {
        return netEvt;
    }


}
