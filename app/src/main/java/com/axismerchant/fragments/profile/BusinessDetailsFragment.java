package com.axismerchant.fragments.profile;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecryptRegister;

/**
 * A simple {@link Fragment} subclass.
 */
public class BusinessDetailsFragment extends Fragment {

    TextView txtName, txtMobileNo, txtAddress, txtMerchantID;
    EncryptDecryptRegister encryptDecryptRegister;
    SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_details, container, false);

        getInitialize(view);

        Constants.retrieveMPINFromDatabase(getActivity());
        Constants.getIMEI(getActivity());
        SharedPreferences pref = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        Constants.MERCHANT_ID = encryptDecryptRegister.decrypt(pref.getString("MerchantID", ""));
        Constants.MOBILE_NUM = encryptDecryptRegister.decrypt(pref.getString("MobileNum", ""));

        preferences = getActivity().getSharedPreferences(Constants.ProfileInfo, Context.MODE_PRIVATE);
        if(preferences.contains("merLegalName"))
        {
            txtName.setText(encryptDecryptRegister.decrypt(preferences.getString("merLegalName","")));
            txtMobileNo.setText(encryptDecryptRegister.decrypt(preferences.getString("merMobileNO","")));
            txtAddress.setText(encryptDecryptRegister.decrypt(preferences.getString("regAdd","")));
            txtMerchantID.setText(Constants.MERCHANT_ID);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();


    }


    private void getInitialize(View view) {

        txtName = (TextView) getActivity().findViewById(R.id.txtUsername);
        txtMerchantID = (TextView) view.findViewById(R.id.txtMerchantID);
        txtMobileNo = (TextView) view.findViewById(R.id.txtMobileNumber);
        txtAddress = (TextView) view.findViewById(R.id.txtAddress);

        encryptDecryptRegister = new EncryptDecryptRegister();

    }


}
