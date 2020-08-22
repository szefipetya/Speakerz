package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.content.SongItem;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;
import java.util.List;

public class SendSongListBody  extends Body implements  Serializable {

    public SendSongListBody(List<SongItem> items){
        list=items;
    }

    public SendSongListBody(String sender, List<SongItem> items){
        this.senderAddress=sender;
        list=items;
    }


    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.MP_SEND_LIST;
    }

    @Override
    public void setContent(Object obj) {
        list=(List<SongItem>)obj;
    }

    List<SongItem> list;

    @Override
    public List<SongItem> getContent() {
        return list;
    }
}

