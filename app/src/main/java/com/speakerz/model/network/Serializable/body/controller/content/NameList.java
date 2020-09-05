package com.speakerz.model.network.Serializable.body.controller.content;

import java.io.Serializable;
import java.util.HashMap;

public class NameList implements Serializable {
    public HashMap<String,String> namelist;

    public NameList(HashMap<String,String> namelist) {
        this.namelist = namelist;
    }
}
