package com.speakerz.model.network.Serializable.body.audio.content;

import java.io.Serializable;


public class AudioMetaDto implements Serializable {
    public int songId;
    public int port;

   public short channels;
    public int sampleRate;
    public  short bitsPerSample;
    public long fullLengthInBytes;
    public boolean isBitRateVariable;

    public int packageSize;
  //Song object
    public int actualBufferedPackageNumber;
    public Long maxTimeInSeconds;
}
