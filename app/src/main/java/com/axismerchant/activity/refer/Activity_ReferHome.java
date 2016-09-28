package com.axismerchant.activity.refer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.Activity_Notification;
import com.axismerchant.activity.AppActivity;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.activity.start.Activity_UserProfile;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.classes.Notification;
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
import java.util.ArrayList;
import java.util.List;

public class Activity_ReferHome extends AppActivity implements View.OnClickListener {

    EditText edtIndividualName, edtBusinessEntityName, edtTypeOfBusiness, edtMobileNumber, edtLandlineNumber, edtAddress;
    String MOBILE, MID;
    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    private String blockNumberSet = "1234567890~#^|$%&*!()-+?,.<>@:;";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_home);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        TextView txtSubmit = (TextView) findViewById(R.id.txtSubmit);

        edtIndividualName = (EditText) findViewById(R.id.edtIndividualName);
        edtBusinessEntityName = (EditText) findViewById(R.id.edtBusinessEntityName);
        edtTypeOfBusiness = (EditText) findViewById(R.id.edtTypeOFBusiness);
        edtMobileNumber = (EditText) findViewById(R.id.edtMobileNumber);
        edtLandlineNumber = (EditText) findViewById(R.id.edtLandlineNumber);
        edtAddress = (EditText) findViewById(R.id.edtAddress);

        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        txtSubmit.setOnClickListener(this);
        imgNotification.setOnClickListener(this);

        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));

        InputFilter[] numFilter = new InputFilter[1];
        numFilter[0] = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockNumberSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };

        edtIndividualName.setFilters(numFilter);
        edtBusinessEntityName.setFilters(numFilter);

        edtAddress.setFilters(new InputFilter[] {new InputFilter.LengthFilter(500)});
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.txtSubmit:
                getData();
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;
        }
    }

    private void getData() {
        if(edtIndividualName.getText().toString().equals("") || edtBusinessEntityName.getText().toString().equals("") ||
               edtAddress.getText().toString().equals("") || edtMobileNumber.getText().toString().equals("") )
        {
            Constants.showToast(this, getString(R.string.mandatory_details));
        }else if(edtMobileNumber.getText().toString().trim().length() < 10){
            Constants.showToast(this, getString(R.string.invalid_mobile_number));
        }else
        {
            String Name,BusinessName, typeOfBusiness, Address, MobileNumber, LandlineNumber;

            Name = edtIndividualName.getText().toString().trim();
            BusinessName = edtBusinessEntityName.getText().toString().trim();
            typeOfBusiness = edtTypeOfBusiness.getText().toString().trim();
            Address = edtAddress.getText().toString().trim();
            MobileNumber = edtMobileNumber.getText().toString().trim();
            LandlineNumber = edtLandlineNumber.getText().toString().trim();

            if (Constants.isNetworkConnectionAvailable(Activity_ReferHome.this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new SetReferral().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"addReferralMerchant", MID, MOBILE, Name,BusinessName,typeOfBusiness,Address,MobileNumber,LandlineNumber,Constants.SecretKey, Constants.AuthToken, Constants.IMEI );
                } else {
                    new SetReferral().execute(Constants.DEMO_SERVICE+"addReferralMerchant", MID, MOBILE, Name,BusinessName,typeOfBusiness,Address,MobileNumber,LandlineNumber, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

                }
            } else {
                Constants.showToast(this, getString(R.string.no_internet));
            }
        }
    }

    private void ShowDialog(int i) {
        // custom dialog
        final Dialog dialog = new Dialog(Activity_ReferHome.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_for_refer_merchant);

        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);
        TextView text = (TextView) dialog.findViewById(R.id.text);
        ImageView icon = (ImageView) dialog.findViewById(R.id.imgResponse);
        if(i == 0)
        {
            text.setText("Request not sent\nPlease try after some time");
            icon.setVisibility(View.GONE);
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

    private class SetReferral extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_ReferHome.this);
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
                String mID = encryptDecryptRegister.encrypt(arg0[1]);
                String mobile = encryptDecryptRegister.encrypt(arg0[2]);
                String mName = encryptDecrypt.encrypt(arg0[3]);
                String mBusinessName = encryptDecrypt.encrypt(arg0[4]);
                String mBusinessType = encryptDecrypt.encrypt(arg0[5]);
                String mAdress = encryptDecrypt.encrypt(arg0[6]);
                String mMobile = encryptDecrypt.encrypt(arg0[7]);
                String mLandline = encryptDecrypt.encrypt(arg0[8]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mer_mobile_no), mobile));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.rName), mName));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.busiEntityName), mBusinessName));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.busiType), mBusinessType));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.rAddress), mAdress));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.rMobileNo), mMobile));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.rLandlineNo), mLandline));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[9])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[10])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[11])));

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
            return str;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            try{
                if(!data.equals("")){
                JSONArray transaction = new JSONArray(data);
                JSONObject object1 = transaction.getJSONObject(0);

                JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                JSONObject obj = rowResponse.getJSONObject(0);
                String result = obj.optString("result");

                result = encryptDecryptRegister.decrypt(result);
                if(result.equals("Success"))
                {
                    ShowDialog(1);
                    progressDialog.dismiss();
                }else if(result.equals("Fail")){
                    progressDialog.dismiss();
                    ShowDialog(0);

                }else if(result.equalsIgnoreCase("SessionFailure")){
                    Constants.showToast(Activity_ReferHome.this, getString(R.string.session_expired));
                    logout();
                }
                }else {
                    Constants.showToast(Activity_ReferHome.this, getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                Constants.showToast(Activity_ReferHome.this, getString(R.string.network_error));
            }

        }
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
