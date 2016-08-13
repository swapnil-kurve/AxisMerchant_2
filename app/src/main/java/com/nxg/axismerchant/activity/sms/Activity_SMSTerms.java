package com.nxg.axismerchant.activity.sms;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_Home;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;

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

public class Activity_SMSTerms extends AppCompatActivity implements View.OnClickListener {

    int flag = 0;
    ImageView imgAccept;
    TextView txtProceed;
    String MID,MOBILE;//, MPIN,IMEI;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsterms);

        imgAccept = (ImageView) findViewById(R.id.imgAccept);
        txtProceed = (TextView) findViewById(R.id.txtProceed);
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);

        imgAccept.setOnClickListener(this);
        txtProceed.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        txtProceed.setEnabled(false);
        imgNotification.setOnClickListener(this);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

    }

    @Override
    protected void onResume() {
        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

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
            case R.id.imgAccept:
                if(flag == 0)
                {
                    imgAccept.setImageResource(R.mipmap.login_tick);
                    txtProceed.setTextColor(getResources().getColor(R.color.colorPrimary));
                    txtProceed.setEnabled(true);
                    flag = 1;
                }else
                {
                    imgAccept.setImageResource(0);
                    txtProceed.setTextColor(getResources().getColor(R.color.dark_gray));
                    txtProceed.setEnabled(false);
                    flag = 0;
                }
                break;

            case R.id.txtProceed:
                    sendRequest();
                break;

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

    private void sendRequest() {
        if (Constants.isNetworkConnectionAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new SendRequest().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"addRequest", MID, MOBILE, "SMSPay");
            } else {
                new SendRequest().execute(Constants.DEMO_SERVICE+"addRequest", MID, MOBILE, "SMSPay");

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }


    private class SendRequest extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_SMSTerms.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.requestFor), encryptDecrypt.encrypt(arg0[3])));
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
                e1.printStackTrace();
            } catch (IOException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }
            return str;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            try{
                if(!data.equals("")) {
                    JSONArray jsonArray = new JSONArray(data);
                    JSONObject object = jsonArray.getJSONObject(0);
                    JSONArray rowsResponse = object.getJSONArray("rowsResponse");
                    JSONObject obj = rowsResponse.getJSONObject(0);
                    String result = obj.optString("result");
                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object2 = jsonArray.getJSONObject(1);
                        JSONArray getLatestMerchantUserTrans = object2.getJSONArray("addRequest");

                        JSONObject object1 = getLatestMerchantUserTrans.getJSONObject(0);
                        String status = object1.optString("status");
                        String reqId = object1.optString("reqId");

                        status = encryptDecrypt.decrypt(status);
                        reqId = encryptDecrypt.decrypt(reqId);
                        ShowDialog2("Success",status,reqId);
                    } else {
                        progressDialog.dismiss();
                        ShowDialog2(result, "Fail", "null");
                    }
                    progressDialog.dismiss();

                }
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }

        }
    }


    private void ShowDialog2(String result, String status, String reqID)
    {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_message_for_sms);
        dialog.setCancelable(false);

        TextView txtMID = (TextView) dialog.findViewById(R.id.txtMID);
        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);

        TextView txtTitle = (TextView) dialog.findViewById(R.id.txtTitle);
        ImageView imgResponse = (ImageView) dialog.findViewById(R.id.imgResponse);
        TextView txtMsg1 = (TextView) dialog.findViewById(R.id.txtMessage);
        TextView txtMsg2 = (TextView) dialog.findViewById(R.id.msg1);

        if(!result.equals("Success"))
        {
            txtMsg1.setText(getString(R.string.sms_on_boarding_pop_up_submit_fail));
            imgResponse.setImageResource(R.mipmap.fail);
            txtConfirm.setText("Ok");
            txtMID.setVisibility(View.GONE);

            // if button is clicked, close the custom dialog
            txtConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Intent intent = new Intent(Activity_SMSTerms.this, Activity_Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }else
        {
            txtMsg2.setVisibility(View.GONE);
            txtMsg1.setText(getString(R.string.sms_on_boarding_pop_up_submit));
            txtConfirm.setText("Ok");

            SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString("SMSRequestValidated","pending");
            editor.putString("Status",status);
            editor.putString("Request_ID", reqID);
            editor.apply();

            // if button is clicked, close the custom dialog
            txtConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    Intent intent = new Intent(Activity_SMSTerms.this, Activity_Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }


        dialog.show();
    }


}
