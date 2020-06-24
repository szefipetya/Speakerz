package com.speakerz;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.speakerz.App.App;
import com.speakerz.model.BaseModel;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.event.CommonModel_ViewEventHandler;
import com.speakerz.R;
import com.speakerz.model.network.HostNetwork;
import com.speakerz.model.network.TextChangedEventArgs;
import com.speakerz.model.network.WirelessStatusChangedEventArgs;
import com.speakerz.util.EventListener;

public class Create extends Activity {
    //REQUIRED_BEG MODEL_Declare
    CommonModel_ViewEventHandler viewEventHandler;

    void initEventListener() {
        viewEventHandler = new CommonModel_ViewEventHandler(this);
    }


    private void subscribeModel(BaseModel model){
        final Activity selfActivity = this;
        // Wireless changed event
        model.getNetwork().getReciever().WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
                App.getTextValueStorage().setTextValue(R.id.wifi_status, args.status() ? "Wifi is on" : "Wifi is off");
                App.getTextValueStorage().autoConfigureTexts(selfActivity);
            }
        });

        model.getNetwork().TextChanged.addListener(new EventListener<TextChangedEventArgs>() {
            @Override
            public void action(TextChangedEventArgs args) {
                App.getTextValueStorage().setTextValue(R.id.discover_status,args.text());
                App.getTextValueStorage().autoConfigureTexts(selfActivity);
            }
        });
    }

    private void initAndStart() {
        initEventListener();
        viewEventHandler = new CommonModel_ViewEventHandler(this);
        Intent intent = new Intent(this, SpeakerzService.class);
        intent.putExtra("isHost", true);
        startService(intent);
        /*BaseModel model = App.initModel(true);
        subscribeModel(model);
        App.autoConfigureTexts(this);
        App.startModel();*/
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
