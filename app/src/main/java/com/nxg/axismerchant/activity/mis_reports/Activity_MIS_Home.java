package com.nxg.axismerchant.activity.mis_reports;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;

import java.util.ArrayList;

public class Activity_MIS_Home extends AppCompatActivity implements View.OnClickListener {

    double screenInches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_home);

        setSize();

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


    private void setSize() {
        screenInches = Constants.getRes(this);

        if(screenInches<= 6 && screenInches>= 5)
        {
            Constants.showToast(this, "1");
            setSize(18,24,110,130);
        }
        else if(screenInches<= 5 && screenInches>= 4)
        {
            Constants.showToast(this, "2");
            setSize(16,20,60,80);
        }
        else if(screenInches<= 4 && screenInches>= 3)
        {
            Constants.showToast(this, "3");
            setSize(10,18,30,50);
        }
    }

    private void setSize(int i, int i1, int i2, int i3) {

        ((TextView)findViewById(R.id.txtTitle)).setTextSize(i1);

        ((TextView)findViewById(R.id.title1)).setTextSize(i1);
        ((TextView)findViewById(R.id.subtitle1)).setTextSize(i);


        ((TextView)findViewById(R.id.title2)).setTextSize(i1);
        ((TextView)findViewById(R.id.subtitle2)).setTextSize(i);

        ((TextView)findViewById(R.id.title3)).setTextSize(i1);
        ((TextView)findViewById(R.id.subtitle3)).setTextSize(i);

        ((TextView)findViewById(R.id.title4)).setTextSize(i1);
        ((TextView)findViewById(R.id.subtitle4)).setTextSize(i);

        ((ImageView)findViewById(R.id.imgIcon1)).getLayoutParams().height = i3;
        ((ImageView)findViewById(R.id.imgIcon1)).getLayoutParams().width = i3;

        ((ImageView)findViewById(R.id.imgIcon2)).getLayoutParams().height = i3;
        ((ImageView)findViewById(R.id.imgIcon2)).getLayoutParams().width = i3;

        ((ImageView)findViewById(R.id.imgIcon3)).getLayoutParams().height = i3;
        ((ImageView)findViewById(R.id.imgIcon3)).getLayoutParams().width = i3;

        ((ImageView)findViewById(R.id.imgIcon4)).getLayoutParams().height = i3;
        ((ImageView)findViewById(R.id.imgIcon4)).getLayoutParams().width = i3;

        ((ImageView)findViewById(R.id.imgArrow1)).getLayoutParams().height = i2;
        ((ImageView)findViewById(R.id.imgArrow1)).getLayoutParams().width = i2;

        ((ImageView)findViewById(R.id.imgArrow2)).getLayoutParams().height = i2;
        ((ImageView)findViewById(R.id.imgArrow2)).getLayoutParams().width = i2;

        ((ImageView)findViewById(R.id.imgArrow3)).getLayoutParams().height = i2;
        ((ImageView)findViewById(R.id.imgArrow3)).getLayoutParams().width = i2;

        ((ImageView)findViewById(R.id.imgArrow4)).getLayoutParams().height = i2;
        ((ImageView)findViewById(R.id.imgArrow4)).getLayoutParams().width = i2;

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
