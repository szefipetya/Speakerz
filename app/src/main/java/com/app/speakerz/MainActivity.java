package com.app.speakerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.speakerz.model.BaseModel;
import com.app.speakerz.model.DeviceModel;
import com.app.speakerz.model.HostModel;
import com.app.speakerz.model.event.CommonViewEventHandler;
import com.example.speakerz.R;
//REQUIRED means: it needs to be in every Activity.
public class MainActivity extends AppCompatActivity{
    //REQUIRED_BEG MODEL
    CommonViewEventHandler viewEventhandler;
    BaseModel model;
    void initModel(boolean isHost) {
        if (isHost){
            model=new HostModel();
        }else {
            model = new DeviceModel();
        }
        model.init();
        if(viewEventhandler!=null)
        model.addUpdateEventListener(viewEventhandler);
        else{
            initEventListener();
            viewEventhandler.toast("Main : viewEventHandler was null");
            model.addUpdateEventListener(viewEventhandler);
        }
        model.setWifiManager((WifiManager)this.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        model.start();
    }

    //REQUIRED
    void initEventListener(){
        viewEventhandler=new CommonViewEventHandler(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set the viewEventhandler to handle events from model
        initEventListener();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button buttonJoin = (Button) findViewById(R.id.join);
        buttonJoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                    initModel(false);

                Intent Act2 = new Intent(getApplicationContext(),Join.class);
                //TODO: Set model for activity Join
              //  Act2.putExtra()
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);


            }

        });

        Button buttonCreate = (Button) findViewById(R.id.create);
        buttonCreate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                initModel(true);
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
