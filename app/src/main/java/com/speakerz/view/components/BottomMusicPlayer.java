package com.speakerz.view.components;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.speakerz.R;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.view.ExtendedPlayerActivity;

public class BottomMusicPlayer {
    AppCompatActivity activity;
    TextView titleSongTV;
    TextView detailsTV;
    MusicPlayerModel mpModel = null;

  public   BottomMusicPlayer(AppCompatActivity activity){
        this.activity = activity;
    }
    public void setButtons(){
        titleSongTV= (TextView)activity.findViewById(R.id.titleSong);
        detailsTV=(TextView)activity.findViewById(R.id.details);

    }

    View.OnClickListener openExtendedPlayer=(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent Act2 = new Intent(activity, ExtendedPlayerActivity.class);
           activity.startActivity(Act2);

        }
    });;
    public void addListeners(){
        titleSongTV.setOnClickListener(openExtendedPlayer);
        detailsTV.setOnClickListener(openExtendedPlayer);
    }


    public void initModel(MusicPlayerModel model) {
        if(mpModel != null) throw new RuntimeException("A MusicPlayerModel is already registered");
        mpModel = model;
        // TODO: register events

    }

    public void releaseModel() {
        if(mpModel != null){
            // TODO: unregister events

            mpModel = null;
        }
    }
}
