package com.nxg.axismerchant.activity.offers;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
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
import com.nxg.axismerchant.activity.start.Activity_Main;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;
import com.nxg.axismerchant.fragments.PageFragment_for_OfferFeatures;
import com.nxg.axismerchant.offer_alarm.ScheduleClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Activity_OfferDetails extends AppCompatActivity implements View.OnClickListener {

    ViewPager viewPager;
    private String[] tabs ;
    String mPromotionId="";
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    DBHelper dbHelper;
    String MID,MOBILE;
    // This is a handle so that we can call methods on our service
    private ScheduleClient scheduleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_details);

        encryptDecryptRegister = new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();

        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        tabs = getResources().getStringArray(R.array.offers_notices);
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgOfferBanner = (ImageView) findViewById(R.id.imgOfferBanner);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);

        TextView txtRemindLater = (TextView) findViewById(R.id.txtRemindLater);
        TextView txtYes = (TextView) findViewById(R.id.txtYes);

        txtRemindLater.setOnClickListener(this);
        txtYes.setOnClickListener(this);

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

        // Create a new service client and bind our activity to this service
        scheduleClient = new ScheduleClient(this);
        scheduleClient.doBindService();

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

            case R.id.txtRemindLater:
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH,1);
                // Ask our service to set an alarm for that date, this activity talks to the client that talks to the service
                scheduleClient.setAlarmForNotification(calendar);
                // Notify the user what they just did
                Constants.showToast(this, getString(R.string.offer_remind_later));
                onBackPressed();
                break;

            case R.id.txtYes:
                setResponse(mPromotionId,"Accepted");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            bundle.putString("Position",String.valueOf(position));
            bundle.putString("PromotionId",mPromotionId);
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



    private void setResponse(String promotionID, String status) {
        if (Constants.isNetworkConnectionAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new SetResponse().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"addPromotionResponse",  MID, MOBILE, promotionID, status,Constants.SecretKey, Constants.AuthToken,Constants.IMEI );
            } else {
                new SetResponse().execute(Constants.DEMO_SERVICE+"addPromotionResponse", MID, MOBILE, promotionID, status,Constants.SecretKey, Constants.AuthToken,Constants.IMEI);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }


    private class SetResponse extends AsyncTask<String, Void, String>
    {
        String pResponse, promotionID;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            String str = null;
            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);
                pResponse = arg0[3];
                promotionID = arg0[2];

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.promotion_id), encryptDecryptRegister.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.promotion_response), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[7])));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                int stats = response.getStatusLine().getStatusCode();

                if (stats == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    str = data;
                }
            }catch (ParseException e1) {
            } catch (IOException e) {
            }
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if(s != null){
                    JSONObject object = new JSONObject(s);
                    JSONArray verifyOTP = object.getJSONArray("addPromotionResponse");
                    JSONObject object1 = verifyOTP.getJSONObject(0);
                    String result = object1.optString("result");

                    result = encryptDecryptRegister.decrypt(result);

                    if(result.equals("Success"))
                    {
                        Constants.showToast(Activity_OfferDetails.this, getString(R.string.offer_accepted));
                        updateStatus(pResponse,promotionID);
                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(Activity_OfferDetails.this, getString(R.string.session_expired));
                        logout();
                    }
                    else
                    {
                        onBackPressed();
                    }
                }else {
                    Constants.showToast(Activity_OfferDetails.this, getString(R.string.network_error));
                }

            } catch (JSONException e) {

            }

        }
    }

    private void updateStatus(String status, String promotionID)
    {
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.STATUS, status);

        long id = db.update(DBHelper.TABLE_NAME_PROMOTIONS,values, DBHelper.PROMOTION_ID +" = "+promotionID, null);

        onBackPressed();
    }

    @Override
    public void onDestroy() {

        // When our activity is stopped ensure we also stop the connection to the service
        // this stops us leaking our activity into the system *bad*
        if(scheduleClient != null)
            scheduleClient.doUnbindService();
        super.onDestroy();
    }


    private void logout()
    {
        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("KeepLoggedIn", "false");
        editor.apply();
        Intent intent = new Intent(this, Activity_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
