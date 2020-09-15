package com.speakerz.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class Song implements Serializable {

    private Integer id;



    private String data;
    private String title;
    private String album;
    private String artist;

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }
    private int cursorIndex;
    private Long albumId;
    private String owner;
    private String duration;
    byte[] songCoverArt = null;

    public int getCursorIndex() {
        return cursorIndex;
    }

    public Song(int cursorIndex, String data, String title, String album, String artist, String owner, Long albumId, Bitmap songCoverArt) {
        this.cursorIndex=cursorIndex;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.owner = owner;
        this.albumId = albumId;
       this.setSongCoverArt(songCoverArt);
       duration= "0";
    }


    public Bitmap getSongCoverArt() {
        if(songCoverArt!=null){
            return BitmapFactory.decodeByteArray(songCoverArt, 0, songCoverArt.length);

        }
        else return null;
    }

    public void setSongCoverArt(Bitmap songCoverArt1) {
        if(songCoverArt1!=null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            songCoverArt1.compress(Bitmap.CompressFormat.PNG, 100, stream);
            this.songCoverArt = stream.toByteArray();
        }
    }

    public Long getAlbumId() {
        return albumId;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}