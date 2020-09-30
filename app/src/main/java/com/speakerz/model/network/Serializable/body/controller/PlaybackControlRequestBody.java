package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.Song;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;
import java.net.InetAddress;

public class PlaybackControlRequestBody extends Body implements Serializable {

    public enum PlaybackControlType{
        Pause,
        Resume,
        SelectSong
    }

    public PlaybackControlType message;
    public Serializable data = null;

    public PlaybackControlRequestBody(PlaybackControlType message){
        this.message = message;
    }
    public PlaybackControlRequestBody(PlaybackControlType message, Serializable data){
        this.message = message;
        this.data = data;
    }
    public PlaybackControlRequestBody(InetAddress sender, PlaybackControlType message){
        this.message = message;
        this.senderAddress=sender;
    }
    public PlaybackControlRequestBody(InetAddress sender, PlaybackControlType message, Serializable data){
        this.message = message;
        this.data = data;
        this.senderAddress=sender;
    }

    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.MP_CONTROL_REQUEST;
    }


    @Override
    public void setContent(Object obj) {
        data = (Serializable) obj;
    }


    @Override
    public Serializable getContent() {
        return data;
    }
}
