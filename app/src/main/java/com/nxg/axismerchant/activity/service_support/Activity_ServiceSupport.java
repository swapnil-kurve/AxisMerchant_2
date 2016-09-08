package com.nxg.axismerchant.activity.service_support;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;
import com.nxg.axismerchant.fragments.service_support.ServiceSupportFragment;
import com.nxg.axismerchant.fragments.service_support.TrackStatusFragment;

import java.util.ArrayList;

/**
 * Created by vismita.jain on 6/30/16.
 */
public class Activity_ServiceSupport extends Activity implements View.OnClickListener
{
    LinearLayout lyServiceSupport, lyTrackStatus;
    View viewServiceSupport, viewTrackStatus;
    ImageView imgBack, imgNotification, imgProfile;
    private int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_support);

        lyServiceSupport = (LinearLayout) findViewById(R.id.lyServiceSupport);
        lyTrackStatus = (LinearLayout) findViewById(R.id.lyTrackStatus);

        viewServiceSupport = findViewById(R.id.viewServiceSupport);
        viewTrackStatus = findViewById(R.id.viewTrackStatus);

        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgNotification = (ImageView) findViewById(R.id.imgNotification);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);

        imgBack.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        lyServiceSupport.setOnClickListener(this);
        lyTrackStatus.setOnClickListener(this);

        changeToServiceSupport();

        int[] coachMarks = {R.drawable.service_support_01, R.drawable.service_support_2};
        Constants.onCoachMark(this, coachMarks);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.lyServiceSupport:
                changeToServiceSupport();
                break;

            case R.id.lyTrackStatus:
                changeToTrackStatus();
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
        }
    }
    private void changeToServiceSupport()
    {
        flag = 0;
        viewServiceSupport.setBackgroundColor(Color.WHITE);
        viewTrackStatus.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        ServiceSupportFragment serviceSupportFragment = new ServiceSupportFragment();
        getFragmentManager().beginTransaction().replace(R.id.xnContainer,serviceSupportFragment).commit();
    }

    private void changeToTrackStatus()
    {
        flag = 1;
        viewTrackStatus.setBackgroundColor(Color.WHITE);
        viewServiceSupport.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        TrackStatusFragment trackStatusFragment = new TrackStatusFragment();
        getFragmentManager().beginTransaction().replace(R.id.xnContainer, trackStatusFragment).commit();
    }

    @Override
    public void onBackPressed() {
//        if(flag == 1)
//        {
//            changeToServiceSupport();
//        }else {
            super.onBackPressed();
//        }
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
