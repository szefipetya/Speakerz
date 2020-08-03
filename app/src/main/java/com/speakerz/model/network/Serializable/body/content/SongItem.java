package com.speakerz.model.network.Serializable.body.content;

import java.io.Serializable;

public class SongItem implements Serializable {

    public String link="test_link";
   public String title ="Test_title";
  public  String sender="Test_sender";

  public   Object avatar=null;

    public SongItem(String title, String sender,String link) {
        this.link = link;
        this.title = title;
        this.sender = sender;
    }


}
