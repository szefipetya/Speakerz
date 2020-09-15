package com.speakerz.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.speakerz.R;
import com.speakerz.view.recyclerview.join.AdapterJoin;
import com.speakerz.view.recyclerview.join.itemJoin;


import java.util.ArrayList;

public class JoinFragment extends Fragment {

    private RecyclerView recyclerViewJoin;

    private ArrayList<itemJoin> listJoin;
    private AdapterJoin adapterJoin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.layout_join, container, false);

        recyclerViewJoin =mView.findViewById(R.id.recyclerView_devices);
        recyclerViewJoin.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewJoin.setLayoutManager(mLinearLayoutManager);


        listJoin = new ArrayList<>();
        listJoin.add(new itemJoin("AnyuTeloja"));

        adapterJoin = new AdapterJoin(listJoin, mView.getContext());
        recyclerViewJoin.setAdapter(adapterJoin);

        return mView;
    }
}