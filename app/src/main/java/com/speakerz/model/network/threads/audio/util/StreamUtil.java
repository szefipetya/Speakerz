package com.speakerz.model.network.threads.audio.util;

import com.speakerz.debug.D;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public  class StreamUtil {
  public  static byte[] encode(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
          return bos.toByteArray();

        } finally {
            try {
                bos.close();

            } catch (IOException ex) {
                // ignore close exception
            }
            return null;
        }
  }

   public static Object decode(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            return o;
        } catch (ClassNotFoundException e) {
            D.log("err did not find class: "+e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

            } catch (IOException ex) {
                // ignore close exception
            }
            return null;
        }
    }

}
