package com.speakerz.model.network.Serializable.body.controller;

import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.Serializable.body.controller.content.NameList;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;

import java.io.Serializable;

public class PutNameListInitRequestBody  extends Body implements Serializable {
    NameList namelist;

    public PutNameListInitRequestBody(NameList namelist) {
        this.namelist = namelist;
    }

    @Override
    public SUBTYPE SUBTYPE() {
        return null;
    }

    @Override
    public void setContent(Object obj) {
        namelist=(NameList)obj;
    }

    @Override
    public NameList getContent() {
        return namelist;
    }
}
