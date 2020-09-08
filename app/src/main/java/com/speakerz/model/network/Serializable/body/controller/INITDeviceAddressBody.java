package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;
import java.net.InetAddress;

public class INITDeviceAddressBody extends Body implements Serializable {
    private InetAddress address;

    public  INITDeviceAddressBody( InetAddress address){
        this.address=address;
    }


    public  INITDeviceAddressBody (InetAddress sender, InetAddress address){
        this.address=address;
        this.senderAddress=sender;
    }

    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.NONE;
    }

    @Override
    public void setContent(Object obj) {
        address=( InetAddress)obj;
    }

    @Override
    public InetAddress getContent() {
        return address;
    }
}
