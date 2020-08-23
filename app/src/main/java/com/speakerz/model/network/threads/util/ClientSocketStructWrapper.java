package com.speakerz.model.network.threads.util;

import com.speakerz.model.network.threads.SocketStruct;

public class ClientSocketStructWrapper {
  public ClientSocketStructWrapper(){
      receiverInfoSocket =new SocketStruct();
      senderInfoSocket =new SocketStruct();
      dataSocket=new SocketStruct();
   }
   public SocketStruct receiverInfoSocket;
   public SocketStruct senderInfoSocket;
   public SocketStruct dataSocket;
}
