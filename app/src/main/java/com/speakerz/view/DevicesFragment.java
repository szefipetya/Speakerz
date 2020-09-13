package com.speakerz.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.speakerz.R;
import com.speakerz.view.recyclerview.songadd.library.AdapterDevice;
import com.speakerz.view.recyclerview.songadd.library.deviceItem;
import com.speakerz.view.recyclerview.songadd.library.libraryItem;

import java.util.ArrayList;

public class DevicesFragment extends Fragment {

    private RecyclerView recyclerViewDevices;

    private ArrayList<deviceItem> listDevices;
    private AdapterDevice adapterDevices;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.fragment_devices, container, false);

        recyclerViewDevices =mView.findViewById(R.id.recyclerView_devices);
        recyclerViewDevices.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewDevices.setLayoutManager(mLinearLayoutManager);


        listDevices = new ArrayList<>();
        listDevices.add(new deviceItem("AnyuTelója", "ANyu"));

        adapterDevices = new AdapterDevice(mView.getContext(), listDevices);

        recyclerViewDevices.setAdapter(adapterDevices);
        return mView;
    }
}