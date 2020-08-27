package com.speakerz.model;

import java.io.File;

public class SongChangedInfo {
    public SongChangedInfo(File file, Integer songId) {
        this.file = file;
        this.songId = songId;
    }

    public File file;
    public Integer songId;
}
