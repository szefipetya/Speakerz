package com.speakerz.model.network.Serializable.body;

import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.threads.SocketStruct;

import java.io.Serializable;
import java.net.InetAddress;

public abstract class Body implements Serializable {

    public String senderAddress;
    abstract  public SUBTYPE SUBTYPE();
    abstract  public void setContent(Object obj);
    abstract  public Object getContent();
}
