package com.app.speakerz.model;

import androidx.appcompat.app.AppCompatActivity;
import com.app.speakerz.model.network.*;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.Strategy;

public class HostModel extends BaseModel {
@Override
    public void init(){
        network=new HostNetwork();
        network.init(this);
    }


    @Override
    public void start() {
    network.start();
    }

    HostNetwork network;
    public HostModel(){
        super();
    }



}
