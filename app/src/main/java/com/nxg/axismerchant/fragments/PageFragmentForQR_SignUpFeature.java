package com.nxg.axismerchant.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nxg.axismerchant.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageFragmentForQR_SignUpFeature extends Fragment {

    public static final String ARG_OBJECT = "object";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_page_for_qr__sign_up_feature, container, false);
    }

}
