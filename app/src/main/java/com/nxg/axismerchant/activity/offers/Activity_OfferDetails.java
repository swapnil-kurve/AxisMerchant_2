package com.nxg.axismerchant.activity.offers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.bumptech.glide.Glide;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;
import com.nxg.axismerchant.fragments.PageFragment_for_OfferFeatures;

import java.util.ArrayList;

public class Activity_OfferDetails extends AppCompatActivity implements View.OnClickListener {

    ViewPager viewPager;
    private String[] tabs ;
    String mPromotionId="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_details);

        tabs = getResources().getStringArray(R.array.offers_notices);
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgOfferBanner = (ImageView) findViewById(R.id.imgOfferBanner);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        TextView txtNotification = (TextView) findViewById(R.id.txtNotificationCount);

        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notificationArrayList = Constants.retrieveFromDatabase(this, dbHelper);
        if(notificationArrayList.size() > 0)
        {
            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setText(String.valueOf(notificationArrayList.size()));
        }

        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgNotification.setOnClickListener(this);


        Intent intent = getIntent();
        String mPromoImg;
        if(intent != null && intent.hasExtra("PromotionId"))
        {
            mPromotionId = intent.getStringExtra("PromotionId");
            mPromoImg = intent.getStringExtra("PromoImg");

            setImgBanner(mPromoImg, imgOfferBanner);
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

        viewPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewPager.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


    }

    private void setImgBanner(String mPromoImg, ImageView imgOfferBanner) {
        Glide.with(this).load(mPromoImg).into(imgOfferBanner);
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

    @Override
    protected void onResume() {
        super.onResume();
        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);
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
            PageFragment_for_OfferFeatures fragmentForOfferFeatures = new PageFragment_for_OfferFeatures();
            Bundle bundle = new Bundle();
            bundle.putString("PromotionId",mPromotionId);
            bundle.putString("Position",String.valueOf(position));
            bundle.putInt(PageFragment_for_OfferFeatures.ARG_OBJECT, position);
            fragmentForOfferFeatures.setArguments(bundle);
            return fragmentForOfferFeatures;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabs[position];
        }
    }

}
