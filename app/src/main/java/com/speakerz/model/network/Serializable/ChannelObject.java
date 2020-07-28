package com.speakerz.model.network.Serializable;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.threads.SocketStruct;

import java.io.Serializable;

public class ChannelObject implements Serializable {
    public Body body;
    public TYPE TYPE;

    public ChannelObject(Body body,TYPE TYPE) {
        this.body = body;
        this.TYPE = TYPE;
    }

}
