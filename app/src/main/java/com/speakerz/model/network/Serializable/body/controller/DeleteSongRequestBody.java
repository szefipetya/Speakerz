package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;

public class DeleteSongRequestBody extends Body implements Serializable {

    int songNum;

    public DeleteSongRequestBody(int songNum){
        this.songNum=songNum;
    }

    @Override
    public SUBTYPE SUBTYPE() {
        return null;
    }

    @Override
    public void setContent(Object obj) {

    }

    @Override
    public Integer getContent() {
        return songNum;
    }
}
