package com.axismerchant.activity.start;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.custom.ProgressDialogue;
import com.axismerchant.database.DBHelper;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Activity_Set_mPIN extends AppCompatActivity implements View.OnClickListener {

    EditText edtEnter_mPIN, edtConfirm_mPIN;
    EncryptDecryptRegister encryptDecryptRegister;
    SharedPreferences preferences;
    String regID;
    ProgressDialogue progressDialog;
    ArrayList<String> mvisaArrayList;
    String MID, MOBILE, isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_m_pin);

        getInitialize();

        preferences = getSharedPreferences(Constants.UserDetails, Context.MODE_PRIVATE);
        regID = preferences.getString("REG_ID", "");
    }

    private void getInitialize() {

        progressDialog = new ProgressDialogue();
        encryptDecryptRegister = new EncryptDecryptRegister();

        edtEnter_mPIN = (EditText) findViewById(R.id.edt_mPIN);
        edtConfirm_mPIN = (EditText) findViewById(R.id.edtConfirm);
        TextView txtSubmit = (TextView) findViewById(R.id.txtSubmit);

        txtSubmit.setOnClickListener(this);
        mvisaArrayList = new ArrayList<>();
    }



    private void getUserMPIN()
    {
        String mMPIN, mConfirmedMPIN;

        mMPIN = edtEnter_mPIN.getText().toString().trim();
        mConfirmedMPIN = edtConfirm_mPIN.getText().toString().trim();

        if(mMPIN.equals(""))
        {
            Constants.showToast(this, getString(R.string.mpin_not_provide));
        }else if(mMPIN.length()<4)
        {
            Constants.showToast(this, getString(R.string.mpin_less_digit));
        }else if(mConfirmedMPIN.equals(""))
        {
            Constants.showToast(this, getString(R.string.confirm_mpin));
        }else if(mMPIN.equals(mConfirmedMPIN))
        {
            Constants.MPIN = mMPIN;
            MID = Constants.MERCHANT_ID;
            MOBILE = Constants.MOBILE_NUM;
            
            saveMPINToDB(encryptDecryptRegister.encrypt(mMPIN));
            preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
            isAdmin = encryptDecryptRegister.decrypt(preferences.getString("isAdmin", ""));
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("MPIN", encryptDecryptRegister.encrypt(mMPIN));
            editor.apply();

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
            String currentDateandTime = sdf.format(new Date());

            if (Constants.isNetworkConnectionAvailable(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new SetMPIN().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"setMPin", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.MPIN, regID, currentDateandTime, Constants.IMEI, "secretKey", Constants.AuthToken);
                } else {
                    new SetMPIN().execute(Constants.DEMO_SERVICE +"setMPin",Constants.MERCHANT_ID,Constants.MOBILE_NUM, Constants.MPIN, regID, currentDateandTime , Constants.IMEI, "secretKey", Constants.AuthToken);

                }
            } else {
                Constants.showToast(this, getString(R.string.no_internet));
            }
        }else
        {
            Constants.showToast(this, getString(R.string.mpin_not_matching));
        }
    }

    private void saveMPINToDB(String mMPIN) {
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            values.put(DBHelper.MPIN, mMPIN);

            long id = db.insert(DBHelper.TABLE_NAME_MPIN, null, values);
            Log.v("id", String.valueOf(id));
        }catch (Exception e)
        {}finally {
            db.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_m_pin, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtSubmit:
                getUserMPIN();
                break;
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void logout() {
        preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("KeepLoggedIn", "false");
        editor.apply();
        Intent intent = new Intent(this, Activity_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void getMerchantDetails() {
        if (Constants.isNetworkConnectionAvailable(Activity_Set_mPIN.this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetProfileDetails().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getProfileDetails", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetProfileDetails().execute(Constants.DEMO_SERVICE + "getProfileDetails", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(Activity_Set_mPIN.this, getString(R.string.no_internet));
        }
    }

    private void getMVisaIDs() {
        if (Constants.isNetworkConnectionAvailable(Activity_Set_mPIN.this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetmVisaIds().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getAllMvisaIds", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetmVisaIds().execute(Constants.DEMO_SERVICE + "getAllMvisaIds", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(Activity_Set_mPIN.this, getString(R.string.no_internet));
        }
    }

    private void gotoHome() {
        Intent intent = new Intent(Activity_Set_mPIN.this, Activity_Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private class SetMPIN extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.onCreateDialog(Activity_Set_mPIN.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.verify), encryptDecryptRegister.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.gcm_reg_id), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.currentTime),encryptDecryptRegister.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[7])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken),encryptDecryptRegister.encrypt(arg0[8])));
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
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if(!s.equalsIgnoreCase("")) {
                    JSONObject object = new JSONObject(s);
                    JSONArray verifyOTP = object.getJSONArray("setMPin");
                    JSONObject object1 = verifyOTP.getJSONObject(0);
                    String result = object1.optString("result");

                    result = encryptDecryptRegister.decrypt(result);

                    progressDialog.dismiss();
                    if (result.equals("Success")) {

                        String lastLogin = object1.optString("lastLogin");
                        lastLogin = encryptDecryptRegister.decrypt(lastLogin);

                        preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
                        isAdmin = encryptDecryptRegister.decrypt(preferences.getString("isAdmin", ""));
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("LoggedIn", "true");
                        editor.putString("LastLogin", encryptDecryptRegister.encrypt(lastLogin));
                        int flag = preferences.getInt("KeepFlag",0);
                        if (flag == 1)
                            editor.putString("KeepLoggedIn", "true");
                        editor.apply();

                        /*Intent intent = new Intent(Activity_Set_mPIN.this, Activity_Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();*/

                        getMerchantDetails();


                    } else if(result.equalsIgnoreCase("Session Time Out")) {
                        Constants.showToast(Activity_Set_mPIN.this, getString(R.string.session_expired));
                        logout();
                    }else{
                            Constants.showToast(Activity_Set_mPIN.this, getString(R.string.network_error));
                        }
                }else {
                    Constants.showToast(Activity_Set_mPIN.this, getString(R.string.network_error));
                }
            } catch (JSONException e) {
            }

        }
    }

    private class GetProfileDetails extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.onCreateDialog(Activity_Set_mPIN.this);
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
            } catch (ParseException e1) {
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
                if (!s.equals("")) {
                    JSONArray jsonArray = new JSONArray(s);
                    JSONObject object = jsonArray.getJSONObject(0);
                    JSONArray rowsResponse = object.getJSONArray("rowsResponse");
                    JSONObject obj = rowsResponse.getJSONObject(0);
                    String result = obj.getString("result");

                    result = encryptDecryptRegister.decrypt(result);

                    if (result.equals("Success")) {

                        JSONObject object1 = jsonArray.getJSONObject(1);
                        JSONArray getMerchantDetails = object1.getJSONArray("getProfileDetails");
                        JSONObject object2 = getMerchantDetails.getJSONObject(0);

                        preferences = Activity_Set_mPIN.this.getSharedPreferences(Constants.ProfileInfo, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        editor.putString("merchantId", object2.optString("merchantId"));
                        editor.putString("username", object2.optString("username"));
                        editor.putString("mobileNo", object2.optString("mobileNo"));
                        editor.putString("regAdd", object2.optString("regAdd"));
                        editor.putString("mCity", object2.optString("mCity"));
                        editor.putString("state", object2.optString("state"));
                        editor.putString("merchantCountry", object2.optString("merchantCountry"));
                        editor.putString("mEmailId", object2.optString("mEmailId"));
                        editor.putString("COUNTRY_Code", object2.optString("COUNTRY_Code"));
                        editor.putString("mvisaId", object2.optString("mvisaId"));
                        editor.putString("mcc", object2.optString("mcc"));
                        editor.putString("merLegalName", object2.optString("merLegalName"));
                        editor.putString("merMobileNO", object2.optString("merMobileNO"));
                        editor.putString("currencyCode", object2.optString("currencyCode"));
                        editor.putString("mvisaStatus", object2.optString("mvisaStatus"));

                        String cc = encryptDecryptRegister.decrypt(object2.optString("currencyCode"));

                        editor.apply();

                    } else if (result.equalsIgnoreCase("SessionFailure")) {
                        Constants.showToast(Activity_Set_mPIN.this, getString(R.string.no_details));
                    } else {
                        Constants.showToast(Activity_Set_mPIN.this, getString(R.string.no_details));
                    }
                    progressDialog.dismiss();
                    if (isAdmin.equalsIgnoreCase("True"))
                        getMVisaIDs();
                    else {
                        gotoHome();
                    }
                } else {
                    Constants.showToast(Activity_Set_mPIN.this, getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }

    public class GetmVisaIds extends AsyncTask<String, Void, String> {

        String ArrURL[];

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.onCreateDialog(Activity_Set_mPIN.this);
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

                            mvisaArrayList.add(mvisa_mid);
                        }

                        SharedPreferences preferences = Activity_Set_mPIN.this.getSharedPreferences(Constants.ProfileInfo, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        //Set the values
                        Set<String> set = new HashSet<String>();
                        set.addAll(mvisaArrayList);
                        editor.putStringSet("mVisaIds", set);
                        editor.apply();

                        progressDialog.dismiss();

                    } else if (result.equalsIgnoreCase("SessionFailure")) {
                    } else {
                        progressDialog.dismiss();
                    }
                }
                progressDialog.dismiss();
                gotoHome();
            } catch (JSONException e) {
                progressDialog.dismiss();

            }
        }
    }
}
