package com.speakerz.model.network.threads;

import com.speakerz.debug.D;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.GetServerInfoBody;
import com.speakerz.model.network.Serializable.body.GetSongListBody;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.PutSongRequestBody;
import com.speakerz.model.network.Serializable.body.content.ServerInfo;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs1;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.util.LinkedList;

public class ServerControllerSocketThread extends Thread implements SocketThread{
    LinkedList<SocketStruct> socketList=new LinkedList<>();

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    ServerSocket serverSocket=null;
    //dependency injection
    public Event<EventArgs1<Body>> MusicPlayerActionEvent =null;
    public Event<EventArgs1<Body>> MetaInfoEvent = null;



    InputStream inputStream;

    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(5050));
            D.log("server address: "+serverSocket.getInetAddress());
            D.log("localsocketaddress : "+serverSocket.getLocalSocketAddress());
            //serverSocket.bind(new InetSocketAddress(5048));
            //waiting for someone
            D.log("server running");

            while(serverSocket!=null&&!serverSocket.isClosed()) {
                ServerSocketChannel channel =  serverSocket.getChannel();
                final Socket socket = serverSocket.accept();
                if(socket == null){
                    D.log("nonblocking");
                    continue;
                }
                final SocketStruct struct = new SocketStruct();
                struct.socket = socket;
                struct.objectInputStream = new ObjectInputStream(socket.getInputStream());
                struct.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                recentStruct=struct;
                socketList.add(struct);
                writeWelcome(struct);


                D.log("client connected: "+socket.getInetAddress());

                //új szálon elindítjuk a socketet, hogy hallgassuk a bejövő adatokat.
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            // az új szál külön hallgatja az objektumot.
                            listen(struct);
                        } catch (IOException e) {
                            // Váratlan IO hiba a kapcsolat során
                            // - Socket és streamek lezárása
                            // - Socket struck eltávolítás
                            e.printStackTrace();
                            try {
                                struct.objectInputStream.close();
                            }
                            catch (IOException e2) {}
                            try {
                                struct.objectOutputStream.close();
                            }
                            catch (IOException e2) {}
                            try {
                                struct.socket.close();
                            }
                            catch (IOException e2) {}
                            socketList.remove(struct);

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            if (e.getMessage() != null) {
                                D.log(e.getMessage());
                            } else D.log("[null_message]");
                        }
                    }
                }.start();
            }


            //InputStreamReader in = new InputStreamReader(socket.getInputStream());
            //BufferedReader bf=new BufferedReader(in);

         //   String str=bf.readLine();
           // D.log("client says: "+ str);
            // create a DataInputStream so we can read data from it.

            shutdown();
        }catch (IOException ex){
            ex.printStackTrace();
            if(ex.getMessage()!=null)
                D.log("Exception message: " + ex.getMessage());
            else
                D.log("null");

            shutdown();
        }
    }

    private void writeWelcome(SocketStruct struct) throws IOException {
        struct.objectOutputStream.writeObject(new ChannelObject(new GetServerInfoBody(new ServerInfo("asd","bsd")),TYPE.META));
        struct.objectOutputStream.flush();

        MusicPlayerActionEvent.invoke(new EventArgs1<Body>(this,new GetSongListBody(null)));

        D.log("welcome sent");

    }

    @Override
     public void listen(SocketStruct struct) throws IOException, ClassNotFoundException {
        // read the list of messages from the socket
         while (serverSocket!=null) {
             if(struct.socket.isConnected()&&!struct.socket.isClosed()) {
                 recentStruct=struct;
                 D.log("listening...");

                 handleIncomingObject((ChannelObject) struct.objectInputStream.readObject());
             }
             else{
                 break;
             }
         }
        try {
            currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //be careful with that!
   volatile SocketStruct recentStruct=null;

    private SocketStruct getSocketStructByAddress(String address){
        for(SocketStruct s: socketList){
            if(s.socket.getInetAddress().getHostAddress()==address){
                return s;
            }
        }
        return null;
    }

    //be careful with null value!
    public void send(String address,ChannelObject channelObject) throws IOException{


        if(address==null&& recentStruct!=null){
            recentStruct.objectOutputStream.writeObject(channelObject);
            recentStruct.objectOutputStream.flush();
        }else{
            SocketStruct s=getSocketStructByAddress(address);
            if(s!=null) {
                s.objectOutputStream.writeObject(channelObject);
                s.objectOutputStream.flush();
            }else throw new IOException(this.toString()+" error: No socket found with address "+address);

        }
    }

    public void sendAll(ChannelObject chObject) throws IOException {
        for(SocketStruct s : socketList){
            s.objectOutputStream.writeObject(chObject);
            s.objectOutputStream.flush();
        }

    }

    @Override
    public void handleIncomingObject(ChannelObject chObject) throws IOException {
        D.log("server: got an object: "+chObject.TYPE);
        if(chObject.TYPE== TYPE.MP){
            MusicPlayerActionEvent.invoke(new EventArgs1<Body>(this,chObject.body));



            D.log(" server: MusicPlayerActionEvent Happened: ");

        }
    }
    @Override
    public void shutdown(){
        D.log("Closing server socket");
        try {
            for(SocketStruct s: socketList){
                if(s.objectInputStream!=null)
                    s.objectInputStream.close();
                if(s.objectOutputStream!=null)
                    s.objectOutputStream.close();
                if(s.socket!=null)
                    s.socket.close();
                s=null;
            }
            if(serverSocket!=null){
                serverSocket.close();
                serverSocket=null;
            }

            System.gc();
           // currentThread().join();

        } catch (IOException e) {
            e.printStackTrace();
            D.log(e.getMessage());
        }
        D.log("Server socket closed");
    }

}
