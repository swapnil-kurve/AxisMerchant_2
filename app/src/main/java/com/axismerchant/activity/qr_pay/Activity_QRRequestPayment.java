package com.axismerchant.activity.qr_pay;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.Activity_Notification;
import com.axismerchant.activity.start.Activity_UserProfile;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.Notification;
import com.axismerchant.custom.MoneyValueFilter;
import com.axismerchant.database.DBHelper;

import java.util.ArrayList;

public class Activity_QRRequestPayment extends AppCompatActivity implements View.OnClickListener
{
    EditText edtAmount, edtPrimaryId, edtSecondaryId;
    EncryptDecryptRegister encryptDecryptRegister;
    TextView txtViewAllTransactions,txtReqLabel, txtPIDOptional, txtSIDOptional;
    ImageView imgBack, imgNotification, imgProfile,imgEditPrimaryID, imgEditAmount, imgEditSecondaryID;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_requestpayment);

        edtAmount = (EditText) findViewById(R.id.edtqrAmount);
        edtPrimaryId = (EditText) findViewById(R.id.edtPID);
        edtSecondaryId = (EditText) findViewById(R.id.edtSID);

        imgEditAmount = (ImageView) findViewById(R.id.imgEdit);
        imgEditPrimaryID = (ImageView) findViewById(R.id.imgEdit1);
        imgEditSecondaryID = (ImageView) findViewById(R.id.imgEdit2);

        txtPIDOptional = (TextView) findViewById(R.id.txtPIDOptional);
        txtSIDOptional = (TextView) findViewById(R.id.txtSIDOptional);

        txtViewAllTransactions = (TextView) findViewById(R.id.txtViewAllTransactions);

        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgNotification = (ImageView) findViewById(R.id.imgNotification);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        txtReqLabel = (TextView) findViewById(R.id.txtReq);


        edtAmount.setFilters(new InputFilter[]{new MoneyValueFilter()});

        txtViewAllTransactions.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgEditAmount.setOnClickListener(this);
        imgEditPrimaryID.setOnClickListener(this);
        imgEditSecondaryID.setOnClickListener(this);


        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        boolean mQRCoach = preferences.getBoolean("QRCoach1", true);
        if(mQRCoach)
        {
            int[] coachMarks = {R.drawable.qr_pay_3};
            Constants.onCoachMark(this, coachMarks);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("QRCoach1",false);
            editor.apply();
        }


        encryptDecryptRegister = new EncryptDecryptRegister();

        edtPrimaryId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(edtPrimaryId.hasFocus())
                {
                    txtPIDOptional.setVisibility(View.GONE);
                }else if(!edtPrimaryId.hasFocus() && edtPrimaryId.getText().toString().equalsIgnoreCase(""))
                {
                    txtPIDOptional.setVisibility(View.VISIBLE);
                }
            }
        });

        edtSecondaryId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(edtSecondaryId.hasFocus())
                {
                    txtSIDOptional.setVisibility(View.GONE);
                }else if(!edtSecondaryId.hasFocus() && edtSecondaryId.getText().toString().equalsIgnoreCase(""))
                {
                    txtSIDOptional.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.txtViewAllTransactions:
                if(i == 1) {
                    startActivity(new Intent(this, Activity_QRCodeGenerated.class));
                    finish();
                }
                else
                    getData();
                break;

            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

            case R.id.imgEdit:
                i = 0;
                edtAmount.setEnabled(true);
                edtPrimaryId.setEnabled(false);
                edtSecondaryId.setEnabled(false);
                edtAmount.setFocusable(true);
                imgEditAmount.setVisibility(View.GONE);
                imgEditPrimaryID.setVisibility(View.VISIBLE);
                imgEditSecondaryID.setVisibility(View.VISIBLE);
                break;

            case R.id.imgEdit1:
                i = 0;
                edtAmount.setEnabled(false);
                edtSecondaryId.setEnabled(false);
                edtPrimaryId.setEnabled(true);
                edtPrimaryId.setFocusable(true);
                imgEditAmount.setVisibility(View.VISIBLE);
                imgEditPrimaryID.setVisibility(View.GONE);
                imgEditSecondaryID.setVisibility(View.VISIBLE);
                break;

            case R.id.imgEdit2:
                i = 0;
                edtAmount.setEnabled(false);
                edtPrimaryId.setEnabled(false);
                edtSecondaryId.setEnabled(true);
                edtSecondaryId.setFocusable(true);
                imgEditAmount.setVisibility(View.VISIBLE);
                imgEditSecondaryID.setVisibility(View.GONE);
                imgEditPrimaryID.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void getData() {
        String mAmount = edtAmount.getText().toString().trim();
        String mPrimaryID = edtPrimaryId.getText().toString().trim();
        String mSecondaryID = edtSecondaryId.getText().toString();

        if(mAmount.equalsIgnoreCase(""))
        {
            Constants.showToast(this, getString(R.string.enter_amount));
        }else if(mAmount.startsWith(".")){
            Constants.showToast(this, getString(R.string.no_amount));
        }else if (Double.parseDouble(mAmount) <= 0) {
            Constants.showToast(this, getString(R.string.zero_amount));
        }else if(Double.parseDouble(mAmount) > 200000)
        {
            Constants.showToast(this, getString(R.string.amount_exceeds_200000));
        }else{
            mAmount = edtAmount.getText().toString().trim();

            Intent intent = new Intent(this, Activity_QRCodeGenerated.class);
            intent.putExtra("Amount",mAmount);
            intent.putExtra("Primary_Id", mPrimaryID);
//            intent.putExtra("Primary_Id", "PAYCRAFTQR");
            intent.putExtra("Secondary_Id", mSecondaryID);
            startActivity(intent);
            finish();
        }

    }


    @Override
    protected void onResume() {
        TextView txtNotification = (TextView) findViewById(R.id.txtNotificationCount);

        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notificationArrayList = Constants.retrieveFromDatabase(this, dbHelper);
        if(notificationArrayList.size() > 0)
        {
            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setText(String.valueOf(notificationArrayList.size()));
        }else
        {
            txtNotification.setVisibility(View.GONE);
        }
        super.onResume();
    }

}
