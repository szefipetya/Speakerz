package com.speakerz.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.Serializable;

public class Song implements Serializable {

    private Integer id;



    private String data;
    private String title;
    private String album;
    private String artist;
    private String albumArt;
    private String owner;

    public Song(String data, String title, String album, String artist,String owner,String albumArt) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.owner = owner;
        this.albumArt = albumArt;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public String getOwner() {
        return owner;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public String toString() {
        return title;
    }

    public Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
        return null;
    }
}