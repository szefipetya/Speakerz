package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.Song;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;

public class GetSongListBody  extends Body implements  Serializable {

    public GetSongListBody(List<Song> items){
        list=items;
    }

    public GetSongListBody(InetAddress sender, List<Song> items){
        this.senderAddress=sender;
        list=items;
    }


    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.MP_GET_LIST;
    }

    @Override
    public void setContent(Object obj) {
        list=(List<Song>)obj;
    }

    List<Song> list;

    @Override
    public List<Song> getContent() {
        return list;
    }
}
