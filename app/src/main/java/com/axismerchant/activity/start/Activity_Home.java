package com.axismerchant.activity.start;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.axismerchant.R;
import com.axismerchant.activity.Activity_FAQ;
import com.axismerchant.activity.Activity_Notification;
import com.axismerchant.activity.Activity_Version;
import com.axismerchant.activity.Activity_VideoDemo;
import com.axismerchant.activity.AppActivity;
import com.axismerchant.activity.analytics.Activity_Analytics;
import com.axismerchant.activity.mis_reports.Activity_MIS_Home;
import com.axismerchant.activity.offers.Activity_OfferDetails;
import com.axismerchant.activity.offers.Activity_OffersNotices;
import com.axismerchant.activity.qr_pay.Activity_QRPayHome;
import com.axismerchant.activity.qr_pay.Activity_QRSignUp;
import com.axismerchant.activity.refer.Activity_ReferHome;
import com.axismerchant.activity.service_support.Activity_ServiceSupport;
import com.axismerchant.activity.sms.Activity_SMSPayHome;
import com.axismerchant.activity.sms.Activity_SMSSignUp;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.classes.HomeBanner;
import com.axismerchant.classes.Notification;
import com.axismerchant.classes.Promotions;
import com.axismerchant.database.DBHelper;
import com.viewpagerindicator.CirclePageIndicator;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

public class Activity_Home extends AppActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    AutoScrollViewPager viewPager;
    SharedPreferences preferences;
    TextView txtUserName, txtLastLogin, txtNotification;
    DBHelper dbHelper;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private NavigationItemAdapter navigationItemAdapter;
    private String[] navigationItemList;
    EncryptDecrypt encryptDecrypt;
    ArrayList<Promotions> promotionsArrayList;
    Promotions promotions;
    String MID,MOBILE;
    ImageView imgMenu;
    ArrayList<HomeBanner> homeBanners;
    ArrayList<String> mvisaArrayList;
    HomeBanner banner;
    private int session = 1;
    EncryptDecryptRegister encryptDecryptRegister;
    private int[] images = {R.mipmap.smspay_menu, R.mipmap.qrpay_menu, R.mipmap.service_support_menu, R.mipmap.reports_menu,
            R.mipmap.analytics, R.mipmap.offers, R.mipmap.profile_menu, R.mipmap.refer, R.mipmap.translation, R.mipmap.faq, R.mipmap.demo_video,R.mipmap.ver, R.mipmap.logout};

    private int[] homeCoach = {R.drawable.home_profile, R.drawable.home_smspay, R.drawable.home_qr, R.drawable.home_reports, R.drawable.home_service_support};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_navigation);

        getInitialize();

        preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        String user = preferences.getString("Username", "");
        String LastLogin = preferences.getString("LastLogin", "");
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        boolean mHomeCoach = preferences.getBoolean("HomeCoach", true);
        if(mHomeCoach)
        {
            Constants.onCoachMark(this,homeCoach);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("HomeCoach",false);
            editor.apply();
        }

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);

        SharedPreferences preferences = getSharedPreferences(Constants.LanguageData, Context.MODE_PRIVATE);
        int languageSelected = preferences.getInt("Selected_Language",0);

        if (LastLogin.equals("firstLogin")) {
            if(languageSelected == 1)
                txtLastLogin.setText(getString(R.string.last_login)+" : "+getString(R.string.first_login));
            else if(languageSelected == 2)
                txtLastLogin.setText(getString(R.string.hindi_last_login)+" : "+getString(R.string.hindi_first_login));
            else if(languageSelected == 3)
                txtLastLogin.setText(getString(R.string.tamil_last_login)+" : "+getString(R.string.tamil_first_login));
            else if(languageSelected == 4)
                txtLastLogin.setText(getString(R.string.telugu_last_login)+" : "+getString(R.string.telugu_first_login));
            else if(languageSelected == 5)
                txtLastLogin.setText(getString(R.string.bengali_last_login)+" : "+getString(R.string.bengali_first_login));
            else if(languageSelected == 6)
                txtLastLogin.setText(getString(R.string.kannada_last_login)+" : "+getString(R.string.kannada_first_login));
        } else {
            if(languageSelected == 1)
                txtLastLogin.setText(getString(R.string.last_login)+" : " + LastLogin);
            else if(languageSelected == 2)
                txtLastLogin.setText(getString(R.string.hindi_last_login)+" : " + LastLogin);
            else if(languageSelected == 3)
                txtLastLogin.setText(getString(R.string.tamil_last_login)+" : "+LastLogin);
            else if(languageSelected == 4)
                txtLastLogin.setText(getString(R.string.telugu_last_login)+" : "+LastLogin);
            else if(languageSelected == 5)
                txtLastLogin.setText(getString(R.string.bengali_last_login)+" : "+LastLogin);
            else if(languageSelected == 6)
                txtLastLogin.setText(getString(R.string.kannada_last_login)+" : "+LastLogin);
        }

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        switch (languageSelected) {
            case 1:   if (timeOfDay >= 0 && timeOfDay < 12) {
                            txtUserName.setText(getString(R.string.morning_text) + " " + user + "!");
                        } else if (timeOfDay >= 12 && timeOfDay < 16) {
                            txtUserName.setText(getString(R.string.afternoon_text) + " " + user + "!");
                        } else if (timeOfDay >= 16 && timeOfDay < 24) {
                            txtUserName.setText(getString(R.string.evening_text) + " " + user + "!");
                        }
                navigationItemList = getResources().getStringArray(R.array.navigation_menu);
                break;

            case 2:
                txtUserName.setText(getString(R.string.hindi_welcome_text) + " " + user + "!");
                navigationItemList = getResources().getStringArray(R.array.hindi_navigation_menu);
                break;

            case 3:
                txtUserName.setText(getString(R.string.tamil_welcome_text) + " " + user + "!");
                navigationItemList = getResources().getStringArray(R.array.tamil_navigation_menu);
                break;

            case 4:
                txtUserName.setText(getString(R.string.telugu_welcome_text) + " " + user + "!");
                navigationItemList = getResources().getStringArray(R.array.telugu_navigation_menu);
                break;

            case 5:
                txtUserName.setText(getString(R.string.bengali_welcome_text) + " " + user + "!");
                navigationItemList = getResources().getStringArray(R.array.bengali_navigation_menu);
                break;

            case 6:
                txtUserName.setText(getString(R.string.kannada_welcome_text) + " " + user + "!");
                navigationItemList = getResources().getStringArray(R.array.kannada_navigation_menu);
                break;
        }

        navigationItemAdapter = new NavigationItemAdapter();
        mDrawerList.setAdapter(navigationItemAdapter);

        getMerchantDetails();

    }

    private void getMVisaIDs() {
        if (Constants.isNetworkConnectionAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetmVisaIds().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getAllMvisaIds", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetmVisaIds().execute(Constants.DEMO_SERVICE + "getAllMvisaIds", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }

    private void getPromotionImages() {
        if (Constants.isNetworkConnectionAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetPromotions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getImagesForSlider", MID, MOBILE, Constants.SecretKey, Constants.AuthToken,Constants.IMEI);
            } else {
                new GetPromotions().execute(Constants.DEMO_SERVICE + "getImagesForSlider", MID, MOBILE, Constants.SecretKey, Constants.AuthToken,Constants.IMEI);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }


    private void getInitialize() {
        txtNotification = (TextView) findViewById(R.id.txtNotificationCount);
        View lySMSPay = findViewById(R.id.smsPay);
        View lyQRCodePay = findViewById(R.id.qrCodePayment);
        View lyReportsAndMIS = findViewById(R.id.reportsAndMIS);
        View lyServiceSupport = findViewById(R.id.serviceSupport);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        imgMenu = (ImageView) findViewById(R.id.imgMenu);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);

        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtLastLogin = (TextView) findViewById(R.id.txtLastLogin);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        viewPager = (AutoScrollViewPager) findViewById(R.id.pager);

        lySMSPay.setOnClickListener(this);
        lyQRCodePay.setOnClickListener(this);
        lyReportsAndMIS.setOnClickListener(this);
        lyServiceSupport.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgMenu.setOnClickListener(this);
        imgNotification.setOnClickListener(this);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();
        mDrawerList.setOnItemClickListener(this);
        viewPager.startAutoScroll();
        viewPager.setInterval(4000);

        promotionsArrayList = new ArrayList<>(3);
        homeBanners = new ArrayList<>();
        mvisaArrayList = new ArrayList<>();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        dbHelper = new DBHelper(this);
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
        switch (v.getId()) {
            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;

            case R.id.imgMenu:
                mDrawerLayout.openDrawer(mDrawerList);
                break;

            case R.id.smsPay:
                gotoSMS();
                break;

            case R.id.reportsAndMIS:
                startActivity(new Intent(this, Activity_MIS_Home.class));
                break;

            case R.id.serviceSupport:
                startActivity(new Intent(this, Activity_ServiceSupport.class));
                break;

            case R.id.qrCodePayment:
                gotoQR();
                break;

        }
    }

    private void gotoQR() {
        SharedPreferences preferences = getSharedPreferences(Constants.ProfileInfo,MODE_PRIVATE);
        if(preferences.contains("mvisaId"))
        {
            String mVisaId = preferences.getString("mvisaId","");
            if(mVisaId.equals("")  ||  mVisaId.equalsIgnoreCase("Null"))
            {
                startActivity(new Intent(this, Activity_QRSignUp.class));
            }else
            {
                startActivity(new Intent(this, Activity_QRPayHome.class));
            }
        }else
        {
            startActivity(new Intent(this, Activity_QRSignUp.class));
        }
    }

    private void gotoSMS() {
        SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
        String res = preferences.getString("SMSRequestValidated","No");
        /*if(res.equalsIgnoreCase("Active"))
        {
            startActivity(new Intent(this, Activity_SMSPayHome.class));
        }else if(res.equalsIgnoreCase("No"))
            {*/
                if (Constants.isNetworkConnectionAvailable(this)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new CheckStatus().executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "checkRequestStatus", MID, MOBILE, "SMSPay", Constants.SecretKey, Constants.AuthToken,Constants.IMEI);
                    } else {
                        new CheckStatus().execute(Constants.DEMO_SERVICE + "checkRequestStatus", MID, MOBILE, "SMSPay", Constants.SecretKey, Constants.AuthToken,Constants.IMEI);

                    }
                } else {
                    Constants.showToast(this, getString(R.string.no_internet));
                }
           /* }else if(res.equalsIgnoreCase("Pending")){
            Intent intent = new Intent(this, Activity_SMSSignUp.class);
            intent.putExtra("SMSRequestValidated","Pending");
            startActivity(intent);
        }else
        {
            startActivity(new Intent(this, Activity_SMSSignUp.class));
        }*/
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.left_drawer)
        {
            switch (position)
            {
                case 0:
                    gotoSMS();
                    break;

                case 1:
                    gotoQR();
                    break;

                case 2:
                    startActivity(new Intent(this, Activity_ServiceSupport.class));
                    break;

                case 3:
                    startActivity(new Intent(this, Activity_MIS_Home.class));
                    break;

                case 4:
                    startActivity(new Intent(this, Activity_Analytics.class));
                    break;

                case 5:
                    startActivity(new Intent(this, Activity_OffersNotices.class));
                    break;

                case 6:
                    startActivity(new Intent(this, Activity_UserProfile.class));
                    break;

                case 7:
                    startActivity(new Intent(this, Activity_ReferHome.class));
                    break;

                case 8:
                    Intent intent = new Intent(this, Activity_Language.class);
                    intent.putExtra("FromHome",true);
                    startActivity(intent);
                    break;

                case 9:
                    startActivity(new Intent(this, Activity_FAQ.class));
                    break;

                case 10:
//                    startActivity(new Intent(this, Activity_VideoDemo.class));
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/1jVBpT0m8Wg")));
                    break;

                case 11:
                    startActivity(new Intent(this, Activity_Version.class));
                    break;
                case 12:
                    logout(1);
                    break;

            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(mDrawerList))
            mDrawerLayout.closeDrawer(mDrawerList);
        else {
            super.onBackPressed();
//            System.exit(0);
        }
    }



    public class GetPromotions extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String ArrURL[];

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_Home.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            String str = null;
            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[5])));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                int stats = response.getStatusLine().getStatusCode();

                if (stats == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    str = data;
                }
            } catch (org.apache.http.ParseException e1) {
                progressDialog.dismiss();
            } catch (IOException e) {
                progressDialog.dismiss();
            }
            CustomizedExceptionHandler.writeToFile(str);
            return str;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            try {
                if (data != null) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray getImagesForSlider = object.getJSONArray("getImagesForSlider");
                        ArrURL = new String[getImagesForSlider.length()];
                        for (int i = 0; i < getImagesForSlider.length(); i++) {

                            JSONObject object2 = getImagesForSlider.getJSONObject(i);
                            String pImg = object2.optString("pImg");
                            String stype = object2.optString("stype");
                            String pID = object2.optString("pID");
                            String sPriority = object2.optString("sPriority");

                            pImg = encryptDecryptRegister.decrypt(pImg);
                            stype = encryptDecryptRegister.decrypt(stype);
                            pID = encryptDecryptRegister.decrypt(pID);
                            sPriority = encryptDecryptRegister.decrypt(sPriority);

                            ArrURL[i] = pImg;
                            banner = new HomeBanner(pImg,stype,pID,sPriority);
                            homeBanners.add(banner);
                        }

                        CustomPagerAdapter customPagerAdapter = new CustomPagerAdapter(Activity_Home.this, ArrURL);
                        viewPager.setAdapter(customPagerAdapter);

                        CirclePageIndicator pageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
                        pageIndicator.setViewPager(viewPager);

                        progressDialog.dismiss();

                    }
                    getMVisaIDs();
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
            }finally {
                progressDialog.dismiss();
            }
        }
    }


    class CustomPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;
        String[] ArrUrl;

        public CustomPagerAdapter(Activity_Home activity_home, String[] arrURL) {
            mContext = activity_home;
            this.ArrUrl = arrURL;
            mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return ArrUrl.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
            Glide.with(mContext).load(ArrUrl[position]).into(imageView);

            container.addView(itemView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        redirectUser(position);
                }
            });
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

    private void redirectUser(int position) {
        String type = homeBanners.get(position).getStype();

        if(type.equalsIgnoreCase("OFFER"))
        {
            Intent intent = new Intent(this, Activity_OfferDetails.class);
            intent.putExtra("PromotionId", homeBanners.get(position).getpID());
            intent.putExtra("PromoImg", homeBanners.get(position).getpImg());
            startActivity(intent);
        }else
        if(type.equalsIgnoreCase("SMS"))
        {
            gotoSMS();
        }else
        if(type.equalsIgnoreCase("QR"))
        {
            gotoQR();
        }else
        if(type.equalsIgnoreCase("REFER"))
        {
            startActivity(new Intent(this, Activity_ReferHome.class));
        }

    }


    public class GetmVisaIds extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String ArrURL[];

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_Home.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            String str = null;
            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[5])));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                int stats = response.getStatusLine().getStatusCode();

                if (stats == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    str = data;
                }
            } catch (org.apache.http.ParseException e1) {
                progressDialog.dismiss();

            } catch (IOException e) {
                progressDialog.dismiss();

            }
            CustomizedExceptionHandler.writeToFile(str);
            return str;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            try {
                if (data != null) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray getImagesForSlider = object.getJSONArray("getAllMvisaIds");
                        ArrURL = new String[getImagesForSlider.length()];
                        for (int i = 0; i < getImagesForSlider.length(); i++) {

                            JSONObject object2 = getImagesForSlider.getJSONObject(i);
                            String mvisa_mid = object2.optString("mvisa_mid");

                            mvisa_mid = encryptDecrypt.decrypt(mvisa_mid);

                            mvisaArrayList.add(mvisa_mid);
                        }

                        SharedPreferences preferences = getSharedPreferences(Constants.ProfileInfo, MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        //Set the values
                        Set<String> set = new HashSet<String>();
                        set.addAll(mvisaArrayList);
                        editor.putStringSet("mVisaIds", set);
                        editor.apply();

                        progressDialog.dismiss();

                    } else if(result.equalsIgnoreCase("SessionFailure")){
                        session = 0;
                    }else {
                        progressDialog.dismiss();

                    }
                }
                checkSessionVariable();
            } catch (JSONException e) {
                progressDialog.dismiss();

            }
        }
    }

    private void checkSessionVariable() {
        if(session == 0)
            logout(0);
    }


    private class NavigationItemAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return navigationItemList.length;
        }

        @Override
        public Object getItem(int position) {
            return navigationItemList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_navigation_menu,null);

            TextView txtNavMenu = (TextView) convertView.findViewById(R.id.txtNavMenu);
            ImageView imgNavMenu = (ImageView) convertView.findViewById(R.id.imgNavMenu);

            txtNavMenu.setText(navigationItemList[position]);
            imgNavMenu.setImageResource(images[position]);

            return convertView;
        }
    }


    private void retrieveFromDatabase() {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if(promotionsArrayList.size()>0)
            promotionsArrayList.clear();

        Cursor crs = db.rawQuery("select DISTINCT "+ DBHelper.UID + ","+ DBHelper.PROMOTION_ID +"," + DBHelper.TITLE +","+ DBHelper.SUB_TITLE +","
                + DBHelper.MESSAGE +"," + DBHelper.IMG_URL +","
                + DBHelper.PROMOTION_TYPE+"," + DBHelper.WITH_OPTION +"," + DBHelper.READ_STATUS +"," +DBHelper.STATUS + " from " + DBHelper.TABLE_NAME_PROMOTIONS, null);


        while (crs.moveToNext()) {
            String mUID = crs.getString(crs.getColumnIndex(DBHelper.UID));
            String mPromotionID = crs.getString(crs.getColumnIndex(DBHelper.PROMOTION_ID));
            String mTitle = crs.getString(crs.getColumnIndex(DBHelper.TITLE));
            String mSubTitle = crs.getString(crs.getColumnIndex(DBHelper.SUB_TITLE));
            String mMessage = crs.getString(crs.getColumnIndex(DBHelper.MESSAGE));
            String mImgUrl = crs.getString(crs.getColumnIndex(DBHelper.IMG_URL));
            String mPromotionType = crs.getString(crs.getColumnIndex(DBHelper.PROMOTION_TYPE));
            String mWithOption = crs.getString(crs.getColumnIndex(DBHelper.WITH_OPTION));
            String mStatus = crs.getString(crs.getColumnIndex(DBHelper.STATUS));
            String mReadStatus = crs.getString(crs.getColumnIndex(DBHelper.READ_STATUS));
            String mOnDate ="";
            promotions = new Promotions(mUID,mTitle,mMessage,mSubTitle,mImgUrl,mPromotionType,mPromotionID,mWithOption,mStatus,mReadStatus,mOnDate);
            promotionsArrayList.add(promotions);
        }
    }


    public class CheckStatus extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_Home.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            String str = null;
            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.requestFor), encryptDecrypt.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[6])));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                int stats = response.getStatusLine().getStatusCode();

                if (stats == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    str = data;
                }
            } catch (org.apache.http.ParseException e1) {
                progressDialog.dismiss();

            } catch (IOException e) {
                progressDialog.dismiss();

            }
            return str;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            try {
                if (data != null) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray getImagesForSlider = object.getJSONArray("checkRequestStatus");

                        JSONObject object2 = getImagesForSlider.getJSONObject(0);
                        String status = object2.optString("status");

                        status = encryptDecrypt.decrypt(status);

                        SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        if(status.equalsIgnoreCase("Active"))
                        {
                            editor.putString("SMSRequestValidated","Active");
                            editor.apply();

                            progressDialog.dismiss();

                            startActivity(new Intent(Activity_Home.this, Activity_SMSPayHome.class));
                            finish();
                        }else if(status.equalsIgnoreCase("Pending"))
                        {
                            editor.putString("SMSRequestValidated","Pending");
                            editor.apply();

                            Intent intent = new Intent(Activity_Home.this, Activity_SMSSignUp.class);
                            intent.putExtra("SMSRequestValidated","Pending");
                            startActivity(intent);
                        }else
                        {
                            startActivity(new Intent(Activity_Home.this, Activity_SMSSignUp.class));
                        }
                        progressDialog.dismiss();

                    } else if(result.equalsIgnoreCase("SessionFailure")){
                        session = 0;
                    }else {
                        progressDialog.dismiss();
                        startActivity(new Intent(Activity_Home.this, Activity_SMSSignUp.class));
                    }
                }
            } catch (JSONException e) {
                progressDialog.dismiss();

            }
        }
    }


    private void getMerchantDetails() {
        if (Constants.isNetworkConnectionAvailable(Activity_Home.this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetProfileDetails().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getProfileDetails", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetProfileDetails().execute(Constants.DEMO_SERVICE+"getProfileDetails", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(Activity_Home.this, getString(R.string.no_internet));
        }
    }


    private class GetProfileDetails extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_Home.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            String str = "";
            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[5])));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                int stats = response.getStatusLine().getStatusCode();

                if (stats == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    str = data;
                }
            }catch (ParseException e1) {
                progressDialog.dismiss();
            } catch (IOException e) {
                progressDialog.dismiss();
            }
            CustomizedExceptionHandler.writeToFile(str);
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if(!s.equals("")) {
                JSONArray jsonArray = new JSONArray(s);
                JSONObject object = jsonArray.getJSONObject(0);
                JSONArray rowsResponse = object.getJSONArray("rowsResponse");
                JSONObject obj = rowsResponse.getJSONObject(0);
                String result = obj.getString("result");

                result = encryptDecryptRegister.decrypt(result);

                if(result.equals("Success"))
                {

                    JSONObject object1 = jsonArray.getJSONObject(1);
                    JSONArray getMerchantDetails = object1.getJSONArray("getProfileDetails");
                    JSONObject object2 = getMerchantDetails.getJSONObject(0);

                    preferences = getSharedPreferences(Constants.ProfileInfo, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putString("merchantId", encryptDecryptRegister.decrypt(object2.optString("merchantId")));
                    editor.putString("username", encryptDecryptRegister.decrypt(object2.optString("username")));
                    editor.putString("mobileNo", encryptDecryptRegister.decrypt(object2.optString("mobileNo")));
                    editor.putString("regAdd", encryptDecryptRegister.decrypt(object2.optString("regAdd")));
                    editor.putString("mCity", encryptDecryptRegister.decrypt(object2.optString("mCity")));
                    editor.putString("state", encryptDecryptRegister.decrypt(object2.optString("state")));
                    editor.putString("merchantCountry", encryptDecryptRegister.decrypt(object2.optString("merchantCountry")));
                    editor.putString("mEmailId", encryptDecryptRegister.decrypt(object2.optString("mEmailId")));
                    editor.putString("COUNTRY_Code", encryptDecryptRegister.decrypt(object2.optString("COUNTRY_Code")));
                    editor.putString("mvisaId", encryptDecryptRegister.decrypt(object2.optString("mvisaId")));
                    editor.putString("mcc", encryptDecryptRegister.decrypt(object2.optString("mcc")));
                    editor.putString("merLegalName", encryptDecryptRegister.decrypt(object2.optString("merLegalName")));
                    editor.putString("merMobileNO", encryptDecryptRegister.decrypt(object2.optString("merMobileNO")));
                    editor.putString("currencyCode", encryptDecryptRegister.decrypt(object2.optString("currencyCode")));

                    editor.apply();

                }else if(result.equalsIgnoreCase("SessionFailure")){
                    session = 0;
                }else {
                    Constants.showToast(Activity_Home.this, getString(R.string.no_details));
                }
                progressDialog.dismiss();
                    getPromotionImages();
                }else {
                    Constants.showToast(Activity_Home.this,getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }

    private void logout(int i)
    {
        if(i == 1)
            Constants.showToast(Activity_Home.this, getString(R.string.sign_out));
        else
            Constants.showToast(Activity_Home.this, getString(R.string.session_expired));
        preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("KeepLoggedIn", "false");
        editor.apply();
        Intent intent = new Intent(this, Activity_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
