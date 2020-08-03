package com.speakerz.model.network.threads;

import com.speakerz.model.network.Serializable.ChannelObject;

import java.io.IOException;

public interface SocketThread {
    void listen(SocketStruct struct) throws IOException, ClassNotFoundException;
    void handleIncomingObject(ChannelObject chObject) throws IOException;
    void shutdown();
}
