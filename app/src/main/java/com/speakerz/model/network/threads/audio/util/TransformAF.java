package com.speakerz.model.network.threads.audio.util;

import android.media.AudioFormat;

public class TransformAF {
    public static int channel(int count){
        if(count==2)
            return AudioFormat.CHANNEL_OUT_STEREO;
        else
            return AudioFormat.CHANNEL_OUT_MONO;
    }
}
