package com.speakerz.view.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.BaseModel;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventListener;

public class NameListDialog extends AppCompatDialogFragment {
    BaseModel model;
    NameListDialog(BaseModel model){
        super();
        this.model = model;
    }
    private HashAdapter nameListAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.namelist_dialog, null);
        final ListView nameList = (ListView) view.findViewById(R.id.name_list) ;
        nameListAdapter = new HashAdapter(model.NickNames);
        nameList.setAdapter(nameListAdapter);

        builder.setView(view)
                .setNegativeButton("back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dismiss(); // <---- closes the dialog
                    }
                });

        model.DeviceListChangedEvent.addListener(new EventListener<EventArgs1<Body>>() {
            @Override
            public void action(EventArgs1<Body> args) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        nameListAdapter.notifyDataSetChanged();
                        nameList.invalidateViews();
                    }
                });
            }
        });


        return builder.create();
    }
NameListDialog self=this;

}


