package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.content.ServerInfo;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;
import java.net.InetAddress;


public class GetServerInfoBody extends Body implements  Serializable {
    public GetServerInfoBody(  ServerInfo info){
        this.info=info;
    }
    public GetServerInfoBody(InetAddress sender, ServerInfo info){
        this.senderAddress=sender;
        this.info=info;
    }


    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.META_GET_SRV_INFO;
    }

    @Override
    public void setContent(Object obj) {
        this.info=(ServerInfo) obj;
    }

   ServerInfo info;

    @Override
    public ServerInfo getContent() {
        return info;
    }


}

