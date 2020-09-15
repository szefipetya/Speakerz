package com.speakerz.view.recyclerview.songadd.library;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.common.collect.AbstractIterator;

public class libraryItem {
    private String SongName;
    private String Artist;
    private Bitmap img;
    private String SongLengthTime;


    public libraryItem(String songName, String artist, Bitmap img, String songLengthTime) {
        SongName = songName;
        Artist = artist;
        this.img = img;
        SongLengthTime = songLengthTime;
    }

    public String getSongName() {
        return SongName;
    }

    public String getArtist() {
        return Artist;
    }

    public Bitmap getCoverImage() {
        return img;
    }

    public String getSongLengthTime() {
        return SongLengthTime;
    }
}
