package com.app.speakerz.model;

import androidx.appcompat.app.AppCompatActivity;
import com.app.speakerz.model.network.*;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.Strategy;

public class HostModel extends BaseModel {
    @Override
    public void init(){
        STRATEGY = Strategy.P2P_STAR;
        network.setStrategy(STRATEGY);
        network.init();
    }

    @Override
    public void start() {
    network.start();
    }

    HostNetwork network;
    public HostModel(){
        super();
    }
public HostModel(AppCompatActivity a){
    super(a);

   network=new HostNetwork(a);

}



}
