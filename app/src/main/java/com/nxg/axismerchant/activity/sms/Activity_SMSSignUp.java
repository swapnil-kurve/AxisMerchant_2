package com.nxg.axismerchant.activity.sms;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;
import com.nxg.axismerchant.fragments.sms.PageFragmentForSMS_SignUpFeatures;
import com.nxg.axismerchant.fragments.sms.PageFragmentForSMS_SignUpFees;

import java.util.ArrayList;

public class Activity_SMSSignUp extends AppCompatActivity implements View.OnClickListener {

    ViewPager viewPager;
    private String[] tabs ;
    String MID,MOBILE;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_sign_up);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Futura_LightBold.ttf");

        tabs = getResources().getStringArray(R.array.sms_pay_sign_up);
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        TextView txtProceed = (TextView) findViewById(R.id.txtProceed);
        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        txtProceed.setOnClickListener(this);
        imgNotification.setOnClickListener(this);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setTypeface(typeFace,0);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        SharedPreferences pref = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = pref.getString("MerchantID","0");
        MOBILE = pref.getString("MobileNum","0");

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);

        SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
        String res = preferences.getString("Validated","No");
        if(res.equalsIgnoreCase("pending"))
        {
            ShowDialog2("pending");
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

            case R.id.txtProceed:
                startActivity(new Intent(this, Activity_SMSTerms.class));
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;
        }
    }


    private void ShowDialog2(String status)
    {
     if(!status.equalsIgnoreCase("Not Requested")) {
         // custom dialog
         final Dialog dialog = new Dialog(this);
         dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         dialog.setContentView(R.layout.dialog_layout_message_for_sms);
         dialog.setCancelable(false);

         TextView txtMID = (TextView) dialog.findViewById(R.id.txtMID);
         TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);
         ImageView imgResponse = (ImageView) dialog.findViewById(R.id.imgResponse);
         TextView txtTitle = (TextView) dialog.findViewById(R.id.txtTitle);
         TextView txtMsg1 = (TextView) dialog.findViewById(R.id.txtMessage);
         TextView txtMsg2 = (TextView) dialog.findViewById(R.id.msg1);

         txtMsg1.setVisibility(View.VISIBLE);
         txtMsg2.setVisibility(View.VISIBLE);

         if (status.equalsIgnoreCase("Pending")) {
             txtTitle.setVisibility(View.GONE);
             txtMsg1.setText("Your Request is pending.");
             txtMsg2.setText("Our Relationship Manager will contact you soon.");
             txtMsg2.setMaxLines(3);
             txtConfirm.setText("Ok");
             SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
             SharedPreferences.Editor editor = preferences.edit();
             editor.putString("Validated", "pending");
             editor.apply();
         } else if (status.equalsIgnoreCase("Block")) {
             txtTitle.setVisibility(View.GONE);
             txtMsg1.setText("Your are not authorised for this service.");
             txtMsg2.setText("Contact your branch manager to avail this service.");
             txtMsg2.setMaxLines(3);
             imgResponse.setImageResource(R.mipmap.fail);
             txtConfirm.setText("Ok");
             SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
             SharedPreferences.Editor editor = preferences.edit();
             editor.putString("Validated", "pending");
             editor.apply();
         }
         // if button is clicked, close the custom dialog
         txtConfirm.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 dialog.dismiss();
                 onBackPressed();
             }
         });

         dialog.show();
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
                PageFragmentForSMS_SignUpFeatures pageFragmentForSMSSignUp = new PageFragmentForSMS_SignUpFeatures();
                Bundle bundle = new Bundle();
                bundle.putInt(PageFragmentForSMS_SignUpFeatures.ARG_OBJECT, position);
                pageFragmentForSMSSignUp.setArguments(bundle);
                return pageFragmentForSMSSignUp;
            }else if(position == 1)
            {
                PageFragmentForSMS_SignUpFees sms_signUpFees = new PageFragmentForSMS_SignUpFees();
                Bundle bundle = new Bundle();
                bundle.putInt(PageFragmentForSMS_SignUpFees.ARG_OBJECT, position);
                sms_signUpFees.setArguments(bundle);
                return sms_signUpFees;
            }else
            {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabs[position];
        }
    }

}
