package com.speakerz.model;

import android.net.Uri;

import java.io.Serializable;

public class Song implements Serializable {

    private String data;
    private String title;
    private String album;
    private String artist;
    private String owner;

    public Song(String data, String title, String album, String artist,String owner) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }


}