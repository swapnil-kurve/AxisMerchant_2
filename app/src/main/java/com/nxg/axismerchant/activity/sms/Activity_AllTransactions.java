package com.nxg.axismerchant.activity.sms;

import android.content.Intent;
import android.graphics.Color;
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
import com.nxg.axismerchant.fragments.sms.Fragment_ButtonController;
import com.nxg.axismerchant.fragments.sms.Fragment_SMSTransactionReport;
import com.nxg.axismerchant.fragments.sms.Fragment_TransactionsStatus;

import java.util.ArrayList;

public class Activity_AllTransactions extends AppCompatActivity implements View.OnClickListener, Fragment_ButtonController.OnGraphViewChangedListener{

    View viewStatus, viewReport, lyButtonController;
    int flag = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);

        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        View lyTransactionStatus = findViewById(R.id.lyTransactionStatus);
        View lyTransactionReport = findViewById(R.id.lyTransactionReport);
        lyButtonController = findViewById(R.id.buttonContainer);
        viewStatus = findViewById(R.id.viewStatus);
        viewReport = findViewById(R.id.viewReport);
        TextView txtNotification = (TextView) findViewById(R.id.txtNotificationCount);
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notificationArrayList = Constants.retrieveFromDatabase(this, dbHelper);
        if(notificationArrayList.size() > 0)
        {
            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setText(String.valueOf(notificationArrayList.size()));
        }

        lyTransactionReport.setOnClickListener(this);
        lyTransactionStatus.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        imgNotification.setOnClickListener(this);

        changeToXnStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);
    }

    private void changeToXnStatus() {
        Fragment_TransactionsStatus transactionsStatus = new Fragment_TransactionsStatus();
        getSupportFragmentManager().beginTransaction().replace(R.id.xnContainer,transactionsStatus).commit();
        viewStatus.setVisibility(View.VISIBLE);
        viewStatus.setBackgroundColor(Color.WHITE);
        viewReport.setVisibility(View.GONE);
        lyButtonController.setVisibility(View.GONE);
    }

    Fragment_SMSTransactionReport report_fragment;
    private void changeToXnReport() {
        report_fragment = new Fragment_SMSTransactionReport();
        Bundle bundle = new Bundle();
        bundle.putInt(Fragment_SMSTransactionReport.ARG_OBJECT,1);
        report_fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.xnContainer,report_fragment).commit();

        Fragment_ButtonController buttonController = new Fragment_ButtonController();
        getSupportFragmentManager().beginTransaction().replace(R.id.buttonContainer,buttonController).commit();

        viewStatus.setVisibility(View.GONE);
        viewReport.setBackgroundColor(Color.WHITE);
        viewReport.setVisibility(View.VISIBLE);
        lyButtonController.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.lyTransactionStatus:
                if(flag == 0) {
                    changeToXnStatus();
                    flag = 1;
                }
                break;

            case R.id.lyTransactionReport:
                if(flag == 1) {
                    changeToXnReport();
                    flag = 0;
                }
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;
        }
    }

    @Override
    public void onViewSelected(int type) {
        if(type == 1) {
//            report_fragment.showBarChartXn();
            report_fragment = new Fragment_SMSTransactionReport();
            Bundle bundle = new Bundle();
            bundle.putInt(Fragment_SMSTransactionReport.ARG_OBJECT,type);
            report_fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.xnContainer,report_fragment).commit();
        }
        else {
//            report_fragment.showBarChartVolume();
            report_fragment = new Fragment_SMSTransactionReport();
            Bundle bundle = new Bundle();
            bundle.putInt(Fragment_SMSTransactionReport.ARG_OBJECT,type);
            report_fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.xnContainer,report_fragment).commit();
        }
    }
}
