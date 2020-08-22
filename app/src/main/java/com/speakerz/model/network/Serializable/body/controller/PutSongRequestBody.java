package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.content.SongItem;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;

public class PutSongRequestBody  extends Body implements Serializable {

   public PutSongRequestBody( SongItem item){
        songItem=item;
    }
   public PutSongRequestBody( String sender, SongItem item){
        songItem=item;
        this.senderAddress=sender;
    }

    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.MP_PUT_SONG;
    }


    @Override
    public void setContent(Object obj) {
        songItem=(SongItem)obj;
    }

    private SongItem songItem;

    @Override
    public SongItem getContent() {
        return songItem;
    }
}
