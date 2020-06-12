package com.app.speakerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.speakerz.App.App;
import com.app.speakerz.debug.D;
import com.app.speakerz.model.BaseModel;
import com.app.speakerz.model.DeviceModel;
import com.app.speakerz.model.HostModel;
import com.app.speakerz.model.event.CommonViewEventHandler;
import com.example.speakerz.R;
//REQUIRED means: it needs to be in every Activity.
public class MainActivity extends AppCompatActivity{
    //REQUIRED_BEG MODEL
    CommonViewEventHandler viewEventHandler;
    private void onCreateInit(){
        //TODO: EZ ÍGY NAGYON CSÚNYA
        setTextValuesFromStorage();
        App.setWifiManager(((WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE)));

        if(!App.getWifiManager().isWifiEnabled()){
            ((TextView)findViewById(R.id.wifi_status)).setText("Wifi is off");
        }else{
            ((TextView)findViewById(R.id.wifi_status)).setText("Wifi is on");
        }
    }

    private void initModelAfterDecision(){
        initEventListener();
        App.addUpdateEventListener(viewEventHandler);
        App.setWifiManager((WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE));
    }
    void initEventListener() {
        viewEventHandler = new CommonViewEventHandler(this);
    }
    void setTextValuesFromStorage(){
      //  ((TextView)findViewById(R.id.wifi_status)).setText(new String(App.getTextFromStorage(R.id.wifi_status)));
        App.autoConfigureTexts(this);
    }
    //REQUIRED_END MODEL

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //set the viewEventhandler to handle events from model


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //REQUIRED_BEGIN MODEL
        onCreateInit();
        //REQUIRED_END MODEL

        Toast.makeText(this,"oncreate- main",Toast.LENGTH_LONG).show();
        D.log("oncreate_main");

        Button buttonJoin = (Button) findViewById(R.id.join);
        buttonJoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                App.initModel(false);
                initModelAfterDecision();
                Intent Act2 = new Intent(getApplicationContext(),Join.class);
                //TODO: Set model for activity Join
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);


            }

        });

        Button buttonCreate = (Button) findViewById(R.id.create);
        buttonCreate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                App.initModel(false);
                initModelAfterDecision();
                Intent Act2 = new Intent(getApplicationContext(),Create.class);
                //TODO: Set model for activity Create

                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });


        Button buttonOptions = (Button) findViewById(R.id.options);
        buttonOptions.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),Options.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });

    }





}
