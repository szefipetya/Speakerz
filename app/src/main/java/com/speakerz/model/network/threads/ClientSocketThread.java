package com.speakerz.model.network.threads;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocketThread extends Thread {
    Socket socket;
    String hostAddress;
    public ClientSocketThread(InetAddress hostAddress){
        this.hostAddress=hostAddress.getHostAddress();
        socket=new Socket();
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(hostAddress,5050),500);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //send and recieve
    }
    Handler handler=new Handler(new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_REA
            }
        }
    });
}
