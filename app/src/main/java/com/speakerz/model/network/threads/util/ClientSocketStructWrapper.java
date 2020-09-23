package com.speakerz.model.network.threads.util;

import com.speakerz.model.network.threads.SocketStruct;
import com.speakerz.model.network.threads.audio.util.serializable.AudioPacket;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientSocketStructWrapper {
    public Boolean isClientInStream=false;
    public Boolean eofSongReached=false;
    public final Object eofSongReachedLocker=new Object();
    public Boolean isBuffering=false;
  final  public Object eofReceivedFromClientLocker =new Object();
  final  public Object isBufferingLocker =new Object();
  public long timeWhenConnected;
    public Iterator<AudioPacket> bufferItr;
    public ConcurrentLinkedQueue<AudioPacket> bufferQueue=new ConcurrentLinkedQueue<>();

  public int bufferHeadPosition=0;
    public boolean eofSongReachedFromClient=false;


    public ClientSocketStructWrapper(){
      receiverInfoSocket =new SocketStruct();
      senderInfoSocket =new SocketStruct();
      dataSocket=new SocketStruct();
   }
   public SocketStruct receiverInfoSocket;
   public SocketStruct senderInfoSocket;
   public final SocketStruct dataSocket;
}
