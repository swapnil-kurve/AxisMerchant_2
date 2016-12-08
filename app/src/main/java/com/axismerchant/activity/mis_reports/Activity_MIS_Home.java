package com.axismerchant.activity.mis_reports;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.Activity_Notification;
import com.axismerchant.activity.start.Activity_UserProfile;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.Notification;
import com.axismerchant.database.DBHelper;

import java.util.ArrayList;

public class Activity_MIS_Home extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_home);

        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        View mprView = findViewById(R.id.ly_mpr);
        View XnView = findViewById(R.id.ly_Xn);
        View posView = findViewById(R.id.ly_pos);
        View refundView = findViewById(R.id.ly_refund);

        mprView.setOnClickListener(this);
        XnView.setOnClickListener(this);
        posView.setOnClickListener(this);
        refundView.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
    }

    @Override
    protected void onResume() {

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);

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

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId())
        {
            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

            case R.id.ly_mpr:
                intent = new Intent(this, Activity_MISReports.class);
                intent.putExtra("Position",0);
                startActivity(intent);
                break;

            case R.id.ly_Xn:
                intent = new Intent(this, Activity_MISReports.class);
                intent.putExtra("Position",1);
                startActivity(intent);
                break;

            case R.id.ly_pos:
                intent = new Intent(this, Activity_MISReports.class);
                intent.putExtra("Position",2);
                startActivity(intent);
                break;

            case R.id.ly_refund:
                intent = new Intent(this, Activity_MISReports.class);
                intent.putExtra("Position",3);
                startActivity(intent);
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;
        }
    }
}
