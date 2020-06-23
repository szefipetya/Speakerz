package com.speakerz.debug;

import android.util.Log;
import android.widget.Toast;

public class D {
   public static void log(String msg){
        Log.d("APP_",msg);
    }
  public  static void log(String tag,String msg){
        Log.d(tag,msg);
    }



}
