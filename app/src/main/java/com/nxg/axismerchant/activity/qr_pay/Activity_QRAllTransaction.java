package com.nxg.axismerchant.activity.qr_pay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;

public class Activity_QRAllTransaction extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_all_transaction);

        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);

        imgBack.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
        imgProfile.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;
        }
    }
}
