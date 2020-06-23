package com.speakerz;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.speakerz.App.App;
import com.speakerz.model.event.CommonModel_ViewEventHandler;
import com.speakerz.R;

public class Create extends Activity {
    //REQUIRED_BEG MODEL_Declare
    CommonModel_ViewEventHandler viewEventHandler;

    void initEventListener() {
        viewEventHandler = new CommonModel_ViewEventHandler(this);
    }


    private void initAndStart() {
        initEventListener();
        viewEventHandler = new CommonModel_ViewEventHandler(this);
        App.initModel(true);
        App.addUpdateEventListener(viewEventHandler);
        App.autoConfigureTexts(this);
        App.startModel();
    }
    //REQUIRED_END MODEL_Declare


    @Override
    protected void onResume() {
        super.onResume();
        App.autoConfigureTexts(this);
        registerReceiver(App.getWifiBroadcastReciever(), App.getIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(App.getWifiBroadcastReciever());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        //REQUIRED_BEG MODEL
        initAndStart();
        //REQUIRED_END MODEL

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
                //  Act2.putExtra("Hello","Hello World");
                //  startActivity(Act2);
                finish();

            }

        });

        Button buttonMusicPlayer = (Button) findViewById(R.id.Musicplayer);
        buttonMusicPlayer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent Act2 = new Intent(getApplicationContext(), MusicPlayer.class);
                Act2.putExtra("Hello", "Hello World");
                startActivity(Act2);

            }

        });

        Button startSession = (Button) findViewById(R.id.btn_start_session);
        startSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                App.hStartAdvertising();

            }

        });

    }
}
