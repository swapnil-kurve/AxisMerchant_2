package com.nxg.axismerchant.activity.mis_reports;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;
import com.nxg.axismerchant.fragments.reports.Fragment_MPRDetails;
import com.nxg.axismerchant.fragments.reports.Fragment_RefundTransactions;
import com.nxg.axismerchant.fragments.reports.Fragment_for_MPR;
import com.nxg.axismerchant.fragments.reports.Fragment_for_TransactionReport;

import java.util.ArrayList;

public class Activity_MISReports extends AppCompatActivity implements View.OnClickListener {

    ViewPager viewPager;
    public static String[] tabs ;
    PagerSlidingTabStrip tabsStrip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_reports);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Futura_LightBold.ttf");

        tabs = getResources().getStringArray(R.array.report_type_array);
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);

        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgNotification.setOnClickListener(this);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setTypeface(typeFace , 0);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

        setSize();
        int pos = 0;
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("Position"))
        {
            pos = intent.getIntExtra("Position",0);
        }

        viewPager.setCurrentItem(pos);
    }


    private void setSize() {
        double screenInches = Constants.getRes(this);

        if(screenInches<= 6 && screenInches>= 5)
        {
            Constants.showToast(this, "1");
            setSize(32,24);
        }
        else if(screenInches<= 5 && screenInches>= 4)
        {
            Constants.showToast(this, "2");
            setSize(28,20);
        }
        else if(screenInches<= 4 && screenInches>= 3)
        {
            Constants.showToast(this, "3");
            setSize(22,18);
        }
    }

    private void setSize(int i, int i1) {

        ((TextView) findViewById(R.id.txtTitle)).setTextSize(i1);
        tabsStrip.setTextSize(i);

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
        switch (v.getId())
        {
            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;
        }
    }


    public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

        public SampleFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                Fragment_for_MPR fragment_for_mpr = new Fragment_for_MPR();
                Bundle bundle = new Bundle();
                bundle.putInt(Fragment_for_MPR.ARG_OBJECT, position);
                fragment_for_mpr.setArguments(bundle);
                return fragment_for_mpr;
            }else if(position == 1)
            {
                Fragment_for_TransactionReport fragment_for_transactionReport = new Fragment_for_TransactionReport();
                Bundle bundle = new Bundle();
                bundle.putInt(Fragment_for_TransactionReport.ARG_OBJECT, position);
                fragment_for_transactionReport.setArguments(bundle);
                return fragment_for_transactionReport;
            }else if(position == 2)
            {
                Fragment_for_MPR unsettledPOS = new Fragment_for_MPR();
                Bundle bundle = new Bundle();
                bundle.putInt(Fragment_for_MPR.ARG_OBJECT, position);
                unsettledPOS.setArguments(bundle);
                return unsettledPOS;
            }else if(position == 3)
            {
                Fragment_RefundTransactions refundTransactions = new Fragment_RefundTransactions();
                Bundle bundle = new Bundle();
                bundle.putInt(Fragment_RefundTransactions.ARG_OBJECT, position);
                refundTransactions.setArguments(bundle);
                return refundTransactions;
            }else{
                return null;
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabs[position];
        }
    }


    @Override
    public void onBackPressed() {
        if(Fragment_MPRDetails.flag == 1)
            Fragment_MPRDetails.flag = 0;
        super.onBackPressed();
    }
}
