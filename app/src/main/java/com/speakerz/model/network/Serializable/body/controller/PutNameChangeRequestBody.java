package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;

public class PutNameChangeRequestBody extends Body implements Serializable {
    private NameItem nameitem;

    public  PutNameChangeRequestBody( NameItem item){
        this.nameitem=item;
    }


    public  PutNameChangeRequestBody ( String sender, NameItem item){
        nameitem=item;
        this.senderAddress=sender;
    }

    @Override
    public SUBTYPE SUBTYPE() {
        return SUBTYPE.NONE;
    }

    @Override
    public void setContent(Object obj) {
        nameitem=( NameItem)obj;
    }

    @Override
    public NameItem getContent() {
        return nameitem;
    }
}
