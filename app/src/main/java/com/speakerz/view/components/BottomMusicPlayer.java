package com.speakerz.view.components;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;
import com.speakerz.view.ExtendedPlayerActivity;

public class BottomMusicPlayer {
    AppCompatActivity activity;
    TextView titleSongTV;
    TextView detailsTV;
    ImageButton playButton;
    SeekBar seekBar;
    MusicPlayerModel mpModel = null;

    // Event listeners
    final EventListener<EventArgs2<Integer, Integer>> playbackDurationChanged = new EventListener<EventArgs2<Integer, Integer>>() {
        @Override
        public void action(EventArgs2<Integer, Integer> args) {
            int current = args.arg1();
            int total = args.arg2();

            seekBar.setMax(total);
            seekBar.setProgress(current);
            D.log("dur " + current + " " + total );
        }
    };


    final EventListener<EventArgs1<Boolean>> playbackStateChangedListener = new EventListener<EventArgs1<Boolean>>() {
        @Override
        public void action(EventArgs1<Boolean> args) {
            if(args.arg1()){
                // TODO configure seekbar when media is started

                D.log("state " + args.arg1() );
            }
            final boolean _isPlaying = args.arg1();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPlayIcon(_isPlaying);
                }
            });
        }
    };


    final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try{
                if(mpModel == null) return;
                if(fromUser) {
                    // TODO SEEK
                }
                else{
                    seekBar.setProgress(progress);
                }
            }
            catch (Exception e) {}
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    View.OnClickListener openExtendedPlayer=(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent Act2 = new Intent(activity, ExtendedPlayerActivity.class);
            activity.startActivity(Act2);

        }
    });

    public BottomMusicPlayer(AppCompatActivity activity) {
        this.activity = activity;

        titleSongTV= (TextView)activity.findViewById(R.id.titleSong);
        titleSongTV.setOnClickListener(openExtendedPlayer);

        detailsTV=(TextView)activity.findViewById(R.id.details);
        detailsTV.setOnClickListener(openExtendedPlayer);

        seekBar = (SeekBar) activity.findViewById(R.id.playerSeekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        playButton = (ImageButton) activity.findViewById(R.id.button_pause_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpModel.togglePause();
            }
        });
    }


    private void setPlayIcon(boolean isPlaying){
        playButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_m_playbutton);
    }

    public void initModel(MusicPlayerModel model) {
        if(mpModel != null) throw new RuntimeException("A MusicPlayerModel is already registered");
        mpModel = model;
        setPlayIcon(mpModel.isPlaying());

        mpModel.playbackDurationChanged.addListener(playbackDurationChanged);
        mpModel.playbackStateChanged.addListener(playbackStateChangedListener);

    }

    public void releaseModel() {
        if(mpModel != null){
            mpModel.playbackDurationChanged.removeListener(playbackDurationChanged);
            mpModel.playbackStateChanged.removeListener(playbackStateChangedListener);

            mpModel = null;
        }
    }
}
