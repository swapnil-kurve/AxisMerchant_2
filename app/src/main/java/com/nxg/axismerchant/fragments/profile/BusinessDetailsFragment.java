package com.nxg.axismerchant.fragments.profile;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;

/**
 * A simple {@link Fragment} subclass.
 */
public class BusinessDetailsFragment extends Fragment {

    TextView txtName, txtMobileNo, txtAddress, txtMerchantID;
    EncryptDecryptRegister encryptDecryptRegister;
    SharedPreferences preferences;
    double screenInches;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_details, container, false);

        getInitialize(view);

        setSize(view);
        Constants.retrieveMPINFromDatabase(getActivity());
        Constants.getIMEI(getActivity());
        SharedPreferences pref = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        Constants.MERCHANT_ID = pref.getString("MerchantID", "");
        Constants.MOBILE_NUM = pref.getString("MobileNum", "");

        preferences = getActivity().getSharedPreferences(Constants.ProfileInfo, Context.MODE_PRIVATE);
        if(preferences.contains("merLegalName"))
        {
            txtName.setText(preferences.getString("merLegalName",""));
            txtMobileNo.setText(preferences.getString("merMobileNO",""));
            txtAddress.setText(preferences.getString("regAdd",""));
            txtMerchantID.setText(Constants.MERCHANT_ID);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    private void setSize(View view) {

        if(screenInches<= 6 && screenInches>= 5)
        {
            setSize(view,20,22,200);
        }
        else if(screenInches<= 5 && screenInches>= 4)
        {
            setSize(view, 15,18,45);
        }
        else if(screenInches<= 4 && screenInches>= 3)
        {
            setSize(view, 14,18,70);
        }
    }

    private void setSize(View view, int i, int i1, int i2) {
        ((TextView)view.findViewById(R.id.txtmid)).setTextSize(i);
        ((TextView)view.findViewById(R.id.txtmobile)).setTextSize(i);
        ((TextView)view.findViewById(R.id.txtaddress)).setTextSize(i);

        txtMerchantID.setTextSize(i1);
        txtMobileNo.setTextSize(i1);
        txtAddress.setTextSize(i1);

        ((ImageView)view.findViewById(R.id.imgMid)).getLayoutParams().height = i2;
        ((ImageView)view.findViewById(R.id.imgMid)).getLayoutParams().width = i2;

        ((ImageView)view.findViewById(R.id.imgMobile)).getLayoutParams().height = i2;
        ((ImageView)view.findViewById(R.id.imgMobile)).getLayoutParams().width = i2;

        ((ImageView)view.findViewById(R.id.imgAddress)).getLayoutParams().height = i2;
        ((ImageView)view.findViewById(R.id.imgAddress)).getLayoutParams().width = i2;
    }

    private void getInitialize(View view) {

        txtName = (TextView) getActivity().findViewById(R.id.txtUsername);
        txtMerchantID = (TextView) view.findViewById(R.id.txtMerchantID);
        txtMobileNo = (TextView) view.findViewById(R.id.txtMobileNumber);
        txtAddress = (TextView) view.findViewById(R.id.txtAddress);

        encryptDecryptRegister = new EncryptDecryptRegister();

    }


}
