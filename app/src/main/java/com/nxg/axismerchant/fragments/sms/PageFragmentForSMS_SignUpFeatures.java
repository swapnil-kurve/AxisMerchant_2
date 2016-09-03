package com.nxg.axismerchant.fragments.sms;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nxg.axismerchant.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageFragmentForSMS_SignUpFeatures extends Fragment {

    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sms_sign_up_features, container, false);

        return view;

    }

}
