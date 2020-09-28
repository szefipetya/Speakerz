package com.speakerz.view.recyclerview.songadd.library;

import android.graphics.Bitmap;

public class libraryItem {
    private String title;
    private String artist;
    private Bitmap img;
    private String SongLengthTime;


    public libraryItem(String title, String artist, Bitmap img, String songLengthTime) {
        this.title = title;
        this.artist = artist;
        this.img = img;
        SongLengthTime = songLengthTime;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Bitmap getCoverImage() {
        return img;
    }

    public String getSongLengthTime() {
        return SongLengthTime;
    }
}
