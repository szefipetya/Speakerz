package com.speakerz.viewModel;

import android.util.Pair;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import android.app.Activity;

import com.speakerz.debug.D;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TextValueStorage implements Serializable {
   private Map<Integer,String> valueMap;
    public TextValueStorage(){
        valueMap= new  HashMap<Integer,String>();
    }
    public String getTextValue(Integer id){
        if(valueMap.containsKey(id))
            return valueMap.get(id);
        else
        return "missing key: "+id.toString();
    }
    public void setTextValue(Integer id, String text){
        valueMap.put(id,text);
    }
    //sajnos a lambdát nem támogatja a version
    public void autoConfigureTexts(Activity act) {
        for (Map.Entry<Integer,String> entry : valueMap.entrySet())
           if(act.findViewById(entry.getKey())!=null)
                ((TextView)act.findViewById(entry.getKey())).setText(entry.getValue());
    }
}

