package com.app.speakerz.model;

import androidx.appcompat.app.AppCompatActivity;

import com.app.speakerz.debug.D;
import com.app.speakerz.model.network.BaseNetwork;
import com.app.speakerz.model.network.DeviceNetwork;
import com.app.speakerz.model.network.HostNetwork;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Strategy;

public class DeviceModel extends BaseModel {
@Override
    public void init(){
        network=new DeviceNetwork();
        network.init();
        network.addUpdateEventListener(this);
    }



    @Override
    public void start() {

        network.start();
    }

    public DeviceModel(){
        super();
    }

}
