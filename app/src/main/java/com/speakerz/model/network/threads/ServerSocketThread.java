package com.speakerz.model.network.threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketThread extends Thread {
    Socket socket;
    ServerSocket serverSocket;

    @Override
    public void run() {
        try{
            serverSocket=new ServerSocket(5050);
            //waiting for someone
            socket=serverSocket.accept();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
