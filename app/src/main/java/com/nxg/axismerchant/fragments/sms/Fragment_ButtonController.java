package com.nxg.axismerchant.fragments.sms;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nxg.axismerchant.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_ButtonController extends Fragment implements View.OnClickListener {

    OnGraphViewChangedListener mCallback;
    TextView txtTransactions,txtVolume;

    // Container Activity must implement this interface
    public interface OnGraphViewChangedListener {
        public void onViewSelected(int position);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnGraphViewChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_button_controller, container, false);

        txtTransactions = (TextView) view.findViewById(R.id.txtTransactions);
        txtVolume = (TextView) view.findViewById(R.id.txtVolume);

        txtTransactions.setOnClickListener(this);
        txtVolume.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtTransactions:
                txtTransactions.setBackgroundResource(R.drawable.rounded_corner_theme);
                txtVolume.setBackgroundResource(0);
                mCallback.onViewSelected(1);

                break;

            case R.id.txtVolume:
                txtVolume.setBackgroundResource(R.drawable.rounded_corner_theme);
                txtTransactions.setBackgroundResource(0);
                mCallback.onViewSelected(2);

                break;
        }
    }


}
