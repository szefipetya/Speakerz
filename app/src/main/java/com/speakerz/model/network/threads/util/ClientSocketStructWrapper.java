package com.speakerz.model.network.threads.util;

import com.speakerz.model.network.threads.SocketStruct;

public class ClientSocketStructWrapper {
  public ClientSocketStructWrapper(){
      infoSocket=new SocketStruct();
      dataSocket=new SocketStruct();
   }
   public SocketStruct infoSocket;
   public SocketStruct dataSocket;
}
