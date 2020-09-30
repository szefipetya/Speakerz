package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.Song;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;
import java.net.InetAddress;

public class ChangeCurrentSongRequestBody extends Body implements Serializable {

   public ChangeCurrentSongRequestBody(Integer songId){
       this.songId=songId;
    }
   public ChangeCurrentSongRequestBody(InetAddress sender, Integer songId){
        this.songId=songId;
        this.senderAddress=sender;
    }

    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.MP_CHANGE_SONG;
    }


    @Override
    public void setContent(Object obj) {
        songId=(Integer)obj;
    }

    private Integer songId;

    @Override
    public Integer getContent() {
        return songId;
    }
}
