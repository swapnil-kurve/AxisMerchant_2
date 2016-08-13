package com.nxg.axismerchant.activity.qr_pay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.custom.MoneyValueFilter;
import com.nxg.axismerchant.database.DBHelper;

import java.util.ArrayList;

public class Activity_QRRequestPayment extends AppCompatActivity implements View.OnClickListener
{
    EditText edtAmount, edtPrimaryId, edtSecondaryId;
    EncryptDecryptRegister encryptDecryptRegister;
    TextView txtViewAllTransactions,txtReqLabel;
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

        encryptDecryptRegister = new EncryptDecryptRegister();
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.txtViewAllTransactions:
                if(i == 1)
                    startActivity(new Intent(this, Activity_QRCodeGenerated.class));
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
            intent.putExtra("Secondary_Id", mSecondaryID);
            startActivity(intent);
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
