package com.speakerz.view.recyclerview.songadd.library;

public class libraryItem {
    private String SongName;
    private String Artist;
    private String CoverImagePath;
    private String SongLengthTime;


    public libraryItem(String songName, String artist, String coverImagePath, String songLengthTime) {
        SongName = songName;
        Artist = artist;
        CoverImagePath = coverImagePath;
        SongLengthTime = songLengthTime;
    }

    public String getSongName() {
        return SongName;
    }

    public String getArtist() {
        return Artist;
    }

    public String getCoverImagePath() {
        return CoverImagePath;
    }

    public String getSongLengthTime() {
        return SongLengthTime;
    }
}
