package com.nxg.axismerchant.fragments.sms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nxg.axismerchant.R;


public class PageFragmentForSMS_SignUpFees extends Fragment {
    public static final String ARG_OBJECT = "object";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_for_sms_sign_up_fees, container, false);
        return view;
    }

}
