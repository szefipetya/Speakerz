package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.Song;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;

public class PutSongRequestBody  extends Body implements Serializable {

   public PutSongRequestBody( Song item){
        song=item;
    }
   public PutSongRequestBody( String sender, Song item){
        song=item;
        this.senderAddress=sender;
    }

    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.MP_PUT_SONG;
    }


    @Override
    public void setContent(Object obj) {
        song=(Song)obj;
    }

    private Song song;

    @Override
    public Song getContent() {
        return song;
    }
}
