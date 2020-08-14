package com.speakerz.model.network.threads.audio.util.serializable;

import java.io.Serializable;

public class AudioPacket implements Serializable {
    public AudioPacket(int size, byte[] data) {
        this.size = size;
        this.data = data;
    }

    public int size;
    public byte[] data;
}
