package com.speakerz.view.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.speakerz.HashAdapter;
import com.speakerz.R;
import com.speakerz.model.BaseModel;

public class NameListDialog extends AppCompatDialogFragment {
    BaseModel model;
    NameListDialog(BaseModel model){
        super();
        this.model = model;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.namelist_dialog, null);
        ListView nameList = (ListView) view.findViewById(R.id.name_list) ;
        HashAdapter nameListAdapter = new HashAdapter(model.NickNames);
        nameList.setAdapter(nameListAdapter);

        builder.setView(view)
                .setNegativeButton("back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });



        return builder.create();
    }


}
