package com.speakerz.model.network.threads.audio.util;

import java.io.Serializable;


public class AudioMetaDto implements Serializable {
   public short channels;
    public short bitrate;
    public int sampleRate;
    public  short bitsPerSample;

    public  String title;
    public  String album;
}
