package com.lu.qa.lut.Fragment;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lu.qa.lut.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainTab01 extends Fragment {


    public MainTab01() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_tab01, container, false);
    }


}
