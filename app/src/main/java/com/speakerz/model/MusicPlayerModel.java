package com.speakerz.model;

import android.media.MediaPlayer;

import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.WifiBroadcastReciever;

import java.util.ArrayList;

public class MusicPlayerModel{

    public ArrayList<String> songQueue = new ArrayList<String>();
    public MediaPlayer mediaPlayer;

}
