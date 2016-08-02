package com.nxg.axismerchant.activity.qr_pay;

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

public class Activity_QRTerms extends AppCompatActivity implements View.OnClickListener {

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

        TextView txtNotification = (TextView) findViewById(R.id.txtNotificationCount);
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notificationArrayList = Constants.retrieveFromDatabase(this, dbHelper);
        if(notificationArrayList.size() > 0)
        {
            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setText(String.valueOf(notificationArrayList.size()));
        }

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
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);

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
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, "", "MVISA ONBOARDING REQUEST", "", "", "", "", "");
            } else {
                new SendRequest().execute(Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, "", "MVISA ONBOARDING REQUEST", "", "", "", "", "");

            }
        } else {
            Constants.showToast(this, "No internet available");
        }
    }


    public class SendRequest extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Activity_QRTerms.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mer_mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.tid), encryptDecrypt.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.service_type), encryptDecrypt.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.prob_details), encryptDecrypt.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.off_days), encryptDecrypt.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.visit_timing), encryptDecrypt.encrypt(arg0[7])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.contact_no), encryptDecrypt.encrypt(arg0[8])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.rolls_required), encryptDecrypt.encrypt(arg0[9])));

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
            try {
                if(!data.equals("")) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);

                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray addServiceRequest = object.getJSONArray("addServiceRequest");

                        for (int i = 0; i < addServiceRequest.length(); i++) {
                            JSONObject object2 = addServiceRequest.getJSONObject(i);
                            String Request_Number = object2.optString("Request_Number");
                            String Call_Status = object2.optString("Call_Status");

                            Request_Number = encryptDecrypt.decrypt(Request_Number);
                            Call_Status = encryptDecrypt.decrypt(Call_Status);

//                            Constants.showToast(Activity_QRTerms.this, "Your request number is "+Request_Number);
//                            onBackPressed();
                            progressDialog.dismiss();
                            ShowDialog2("Success",Request_Number,Call_Status);
                        }
                    }else
                    {
//                        Constants.showToast(Activity_QRTerms.this, "Network error, please try again later");
                        progressDialog.dismiss();
                        ShowDialog2(result, "Fail", "null");
                    }
                }else
                {
                    Constants.showToast(Activity_QRTerms.this, "Network error, please try again later");
                }


            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Constants.showToast(Activity_QRTerms.this, "Network error, please try again later");
            }
            progressDialog.dismiss();
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
            txtTitle.setText("Sorry!");
            txtMsg1.setText("Request cannot be processed at this moment.");
            txtMsg2.setText("Please try after some time.");
            imgResponse.setImageResource(R.mipmap.fail);
            txtConfirm.setText("Ok");
            txtMID.setVisibility(View.GONE);

            // if button is clicked, close the custom dialog
            txtConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Intent intent = new Intent(Activity_QRTerms.this, Activity_Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }else
        {
            txtMsg1.setText("Thank you! Your Request is accepted.");
            txtMsg2.setText("You will get confirmation within 48 hours.");
            txtMsg2.setMaxLines(3);
            txtConfirm.setText("Ok");

            SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString("Validated","pending");
            editor.putString("Status",status);
            editor.putString("Request_ID", reqID);
            editor.apply();

            // if button is clicked, close the custom dialog
            txtConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    Intent intent = new Intent(Activity_QRTerms.this, Activity_Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }


        dialog.show();
    }


}
