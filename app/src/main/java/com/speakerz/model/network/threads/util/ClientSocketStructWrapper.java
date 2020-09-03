package com.speakerz.model.network.threads.util;

import com.speakerz.model.network.threads.SocketStruct;

public class ClientSocketStructWrapper {
    public Boolean isClientInStream=false;
    public Boolean eofSongReached=false;
    public final Object eofSongReachedLocker=new Object();
    public Boolean isBuffering=false;
  final  public Object eofReceivedFromClientLocker =new Object();
  final  public Object isBufferingLocker =new Object();
  public long timeWhenConnected;

  public ClientSocketStructWrapper(){
      receiverInfoSocket =new SocketStruct();
      senderInfoSocket =new SocketStruct();
      dataSocket=new SocketStruct();
   }
   public SocketStruct receiverInfoSocket;
   public SocketStruct senderInfoSocket;
   public SocketStruct dataSocket;
}
