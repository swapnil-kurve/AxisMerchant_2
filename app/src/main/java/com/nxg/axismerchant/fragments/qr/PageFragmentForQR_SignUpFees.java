package com.nxg.axismerchant.fragments.qr;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.nxg.axismerchant.R;


public class PageFragmentForQR_SignUpFees extends Fragment {
    public static final String ARG_OBJECT = "object";
    Spinner spinDescription;
    TextView txtRegular01, txtPremium01, txtBlended01, txtSetupFee,txtNetBanking;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_for_sms_qr_up_fees, container, false);

        return view;
    }


}
