package com.speakerz.model.network.Serializable.body.controller.content;

import java.io.Serializable;

public class NameItem implements Serializable {
    public String id = "semmi";
    public String name ="Test_name";
    public  String sender="Test_sender";
    public   Object avatar=null;

    public NameItem(String name, String sender,String id) {
        this.name = name;
        this.sender = sender;
        this.id = id;
    }

}



