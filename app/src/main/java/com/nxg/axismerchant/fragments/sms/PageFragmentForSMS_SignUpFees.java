package com.nxg.axismerchant.fragments.sms;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.nxg.axismerchant.R;

import java.util.ArrayList;


public class PageFragmentForSMS_SignUpFees extends Fragment {
    public static final String ARG_OBJECT = "object";
    Spinner spinDescription;
    TextView txtRegular01, txtPremium01, txtBlended01, txtSetupFee,txtNetBanking;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_for_sms_sign_up_fees, container, false);

        spinDescription = (Spinner) view.findViewById(R.id.spinnerDescription);

        txtRegular01 = (TextView) view.findViewById(R.id.txtRegular01);
        txtPremium01 = (TextView) view.findViewById(R.id.txtPremium01);
        txtBlended01 = (TextView) view.findViewById(R.id.txtBlended01);
        txtSetupFee = (TextView) view.findViewById(R.id.txtSetupFee);
        txtNetBanking = (TextView) view.findViewById(R.id.txtNetBanking);

        setDescription();
        return view;
    }

    private void setDescription() {
        // Spinner Drop down elements
        ArrayList<String> visitingTime = new ArrayList<String>();
        visitingTime.add("Utility/Government/Insurance/Education/Universities");
        visitingTime.add("All other MCCs");


        ArrayAdapter<String> dataAdapter = adapterForSpinner(visitingTime);

        // attaching data adapter to spinner
        spinDescription.setAdapter(dataAdapter);
    }

    private  ArrayAdapter<String> adapterForSpinner(ArrayList<String> list)
    {
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list)
        {
            @Override
            public boolean isEnabled(int position) {

                if(position == 0)
                {
                    txtRegular01.setText(getString(R.string.fees_domestic_cards_diff11));
                    txtBlended01.setText(getString(R.string.fees_domestic_cards_blended1));
                    txtPremium01.setText(getString(R.string.fees_domestic_cards_diff12));
                    txtSetupFee.setText(getString(R.string.fees_setup1));
                    txtNetBanking.setText(getString(R.string.fees_net_banking1));

                }else if(position == 1){
                    txtRegular01.setText(getString(R.string.fees_domestic_cards_diff21));
                    txtBlended01.setText(getString(R.string.fees_domestic_cards_blended2));
                    txtPremium01.setText(getString(R.string.fees_domestic_cards_diff22));
                    txtSetupFee.setText(getString(R.string.fees_setup2));
                    txtNetBanking.setText(getString(R.string.fees_net_banking2));
                }

                return true;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);


                return view;
            }
        };

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        return dataAdapter;
    }


}
