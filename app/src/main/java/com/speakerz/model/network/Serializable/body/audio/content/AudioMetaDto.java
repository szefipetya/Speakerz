package com.speakerz.model.network.Serializable.body.audio.content;

import java.io.Serializable;


public class AudioMetaDto implements Serializable {
 public int port;

   public short channels;
    public short bitrate;
    public int sampleRate;
    public  short bitsPerSample;

    public int packageSize;
  //Song object
    public int actualBufferedPackageNumber;
}
