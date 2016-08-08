package com.nxg.axismerchant.activity.sms;

import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Activity_TransactionStatusDetails extends AppCompatActivity implements View.OnClickListener {

    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    String custMobile, remark, transAmt,XnID, urlCode,isRefund;//, MPIN,IMEI;
    TextView txtResText;
    int simStatus;
    View refLayout;
    DBHelper dbHelper;
    boolean isOnAirplane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_status_details);

        View resendLink = findViewById(R.id.refundLayout);
        txtResText = (TextView) findViewById(R.id.ref_text);
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        refLayout = findViewById(R.id.refundLayout);

        imgProfile.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        resendLink.setOnClickListener(this);
        imgNotification.setOnClickListener(this);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);
        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        Constants.MERCHANT_ID = preferences.getString("MerchantID","0");
        Constants.MOBILE_NUM = preferences.getString("MobileNum","0");

        simStatus = Constants.isSimSupport(Activity_TransactionStatusDetails.this);
        isOnAirplane = Constants.isAirplaneModeOn(this);

        refLayout.setVisibility(View.GONE);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey("XnID"))
        {
            XnID = bundle.getString("XnID");
            getDetails(XnID);
        }else {
            preferences = getSharedPreferences(Constants.EPaymentData, MODE_PRIVATE);
            if(preferences.contains("Invoice Number"))
            {
                XnID = preferences.getString("Invoice Number","0");
                getDetails(XnID);
            }
        }
    }

    private void getDetails(String xnID) {
        if (Constants.isNetworkConnectionAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetSMSXnDetails().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getMerchantUserTransById", Constants.MERCHANT_ID, Constants.MOBILE_NUM, xnID);
            } else {
                new GetSMSXnDetails().execute(Constants.DEMO_SERVICE + "getMerchantUserTransById", Constants.MERCHANT_ID, Constants.MOBILE_NUM, xnID);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
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
            case R.id.refundLayout:
                if(!isOnAirplane) {
                    if (simStatus == 1) {
                        if (txtResText.getText().toString().equalsIgnoreCase("Resend Link"))
                            callResend();
                        else
                            getConfirm();
                    } else {
                        ShowDialog();
                    }
                }else {
                    Constants.showToast(this, getString(R.string.airplane_mode));
                }

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

    private void callRefund() {
        if (Constants.isNetworkConnectionAvailable(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new RefundTransactions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "refundTransaction", Constants.MERCHANT_ID, Constants.MOBILE_NUM,urlCode );
            } else {
                new RefundTransactions().execute(Constants.DEMO_SERVICE + "refundTransaction", Constants.MERCHANT_ID, Constants.MOBILE_NUM,urlCode );

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }

    private void callResend() {
        if (Constants.isNetworkConnectionAvailable(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new ResendURL().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "resendURL", Constants.MERCHANT_ID, Constants.MOBILE_NUM, XnID, custMobile);
            } else {
                new ResendURL().execute(Constants.DEMO_SERVICE + "resendURL", Constants.MERCHANT_ID, Constants.MOBILE_NUM, XnID, custMobile);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }


    private  void ShowDialog()
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

        txtMID.setVisibility(View.GONE);
        txtTitle.setVisibility(View.GONE);
        imgResponse.setVisibility(View.GONE);
        txtMsg2.setVisibility(View.GONE);

        txtMsg1.setText("Please insert sim card to use this feature.");
        txtConfirm.setText("Ok");
        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }



    private class GetSMSXnDetails extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_TransactionStatusDetails.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.transactionId), encryptDecrypt.encrypt(arg0[3])));
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
                        JSONArray getLatestMerchantUserTrans = object2.getJSONArray("getMerchantUserTransById");

                        JSONObject object1 = getLatestMerchantUserTrans.getJSONObject(0);
                        String transactionId = object1.optString("transactionId");
                        String invoiceNo = object1.optString("invoiceNo");
                        String merchantId = object1.optString("merchantId");
                        custMobile = object1.optString("custMobile");
                        transAmt = object1.optString("transAmt");
                        remark = object1.optString("remark");
                        String linkExpiary = object1.optString("linkExpiary");
                        String urlStatus = object1.optString("urlStatus");
                        String vpc_ReceiptNo = object1.optString("vpc_ReceiptNo");
                        String transStatus = object1.optString("transStatus");
                        urlCode = object1.optString("urlCode");
                        String transDate = object1.optString("transDate");
                        isRefund = object1.optString("isRefund");
                        String transType = object1.optString("transType");

                        transactionId = encryptDecrypt.decrypt(transactionId);
                        invoiceNo = encryptDecrypt.decrypt(invoiceNo);
                        custMobile = encryptDecrypt.decrypt(custMobile);
                        merchantId = encryptDecrypt.decrypt(merchantId);
                        transAmt = encryptDecrypt.decrypt(transAmt);
                        remark = encryptDecrypt.decrypt(remark);
                        linkExpiary = encryptDecrypt.decrypt(linkExpiary);
                        urlStatus = encryptDecrypt.decrypt(urlStatus);
                        vpc_ReceiptNo = encryptDecrypt.decrypt(vpc_ReceiptNo);
                        transStatus = encryptDecrypt.decrypt(transStatus);
                        urlCode = encryptDecrypt.decrypt(urlCode);
                        transDate = encryptDecrypt.decrypt(transDate);
                        isRefund = encryptDecrypt.decrypt(isRefund);
                        transType = encryptDecrypt.decrypt(transType);

//                      InsertIntoDatabase(custMobile, transAmt, remark, transactionId, transStatus);

                        UpdateStatusIntoEPay(transactionId,transStatus);

                        String[] tDate = transDate.split("//s+");

                        ((TextView)findViewById(R.id.txtDate)).setText(tDate[0]);
                        ((TextView)findViewById(R.id.transactionID)).setText(transactionId);
                        ((TextView)findViewById(R.id.mobileNumber)).setText(custMobile);
                        ((TextView)findViewById(R.id.rrnNo)).setText(vpc_ReceiptNo);
                        ((TextView)findViewById(R.id.amount)).setText(getString(R.string.Rs)+transAmt);
                        ((TextView)findViewById(R.id.remark)).setText(remark);
                        ((TextView)findViewById(R.id.linkExpiry)).setText(linkExpiary);
                        if(transStatus.equals("Pending")) {
                            ((TextView) findViewById(R.id.txtStatus)).setText(transStatus);
                            ((TextView) findViewById(R.id.txtStatus)).setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                            ((ImageView) findViewById(R.id.imgStatusSMS)).setImageResource(R.mipmap.pending);
                            ((View)findViewById(R.id.refundLayout)).setVisibility(View.VISIBLE);
                            txtResText.setText("Resend Link");
                            refLayout.setVisibility(View.VISIBLE);
                        }else if(transStatus.equals("Success"))
                        {
                            ((TextView) findViewById(R.id.txtStatus)).setText(transStatus);
                            ((TextView) findViewById(R.id.txtStatus)).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            ((ImageView) findViewById(R.id.imgStatusSMS)).setImageResource(R.mipmap.success);
                            ((View)findViewById(R.id.lyExpiry)).setVisibility(View.GONE);
                            ((View)findViewById(R.id.refundLayout)).setVisibility(View.VISIBLE);
                            txtResText.setText("Refund Payment");
                            if(transType.equalsIgnoreCase("sales")) {
                                if (isRefund.equals("0"))
                                    refLayout.setVisibility(View.VISIBLE);
                                else
                                    refLayout.setVisibility(View.GONE);
                            }else{
                                refLayout.setVisibility(View.GONE);
                            }

                        }else
                        {
                            ((TextView) findViewById(R.id.txtStatus)).setText(transStatus);
                            ((TextView) findViewById(R.id.txtStatus)).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            ((ImageView) findViewById(R.id.imgStatusSMS)).setImageResource(R.mipmap.fail);
                            txtResText.setText("Resend Link");
                            refLayout.setVisibility(View.VISIBLE);
                        }

                    } else {
                        Constants.showToast(Activity_TransactionStatusDetails.this, getString(R.string.no_details));
                    }
                    progressDialog.dismiss();
                }else {
                    progressDialog.dismiss();
                    Constants.showToast(Activity_TransactionStatusDetails.this, getString(R.string.network_error));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Constants.showToast(Activity_TransactionStatusDetails.this, getString(R.string.network_error));
            }

        }
    }



    private class ResendURL extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String custMobile = "", XnId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_TransactionStatusDetails.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            String str = "";

            custMobile = arg0[4];
            XnId = arg0[3];

            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.transactionId), encryptDecrypt.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.cust_number), encryptDecrypt.encrypt(arg0[4])));

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

            try {
                if(!data.equals("")) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    String url, tid;
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates = object.getJSONArray("resendURL");
                        for (int i = 0; i < transactionBetDates.length(); i++) {

                            JSONObject object2 = transactionBetDates.getJSONObject(i);
                            url = object2.optString("url");
                            tid = object2.optString("tId");
                            url = encryptDecrypt.decrypt(url);
                            tid = encryptDecrypt.decrypt(tid);

                            if (custMobile.length() == 10) {
                                custMobile = "+91" + custMobile;

                                //Getting intent and PendingIntent instance
                                Intent intent = new Intent(getApplicationContext(), Activity_AllTransactions.class);
                                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                String Message = url;
                                //Get the SmsManager instance and call the sendTextMessage method to send message
                                SmsManager sms = SmsManager.getDefault();
                                sms.sendTextMessage(custMobile, null, Message, pi, null);

                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                                String currentDateandTime = sdf.format(new Date());

                                if(!XnId.equals(tid))
                                InsertIntoDatabase(custMobile, transAmt, remark, tid, "False", currentDateandTime);//tID is saved instead of Invoice Number

                            }

                        }

                        progressDialog.dismiss();
                        Constants.showToast(Activity_TransactionStatusDetails.this, getString(R.string.url_sent));
                        finish();
                    } else {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates = object.getJSONArray("insertTransaction");
                        JSONObject object2 = transactionBetDates.getJSONObject(0);
                        result = object2.optString("result");
                        result = encryptDecryptRegister.decrypt(result);
                        if(result.equalsIgnoreCase("Details Not Added")) {
                            Constants.showToast(Activity_TransactionStatusDetails.this, getString(R.string.url_not_sent));
                        }
                        progressDialog.dismiss();
                    }
                }else
                {
                    Constants.showToast(Activity_TransactionStatusDetails.this,getString(R.string.network_error));
                    progressDialog.dismiss();
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
                Constants.showToast(Activity_TransactionStatusDetails.this,getString(R.string.network_error));
            }

        }
    }


    private void InsertIntoDatabase(String custMobile, String amount, String remark, String invoiceNum, String isFavo, String currentDateandTime) {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.CUST_MOBILE, custMobile);
        values.put(DBHelper.AMOUNT, amount);
        values.put(DBHelper.REMARK, remark);
        values.put(DBHelper.INVOICE_NO, invoiceNum);
        values.put(DBHelper.IS_FAVORITE, isFavo);
        values.put(DBHelper.STATUS, "Pending");
        values.put(DBHelper.TRANS_DATE,currentDateandTime);

        long id = db.insert(DBHelper.TABLE_NAME_E_PAYMENT, null, values);
        Log.v("id", String.valueOf(id));

    }



    private class RefundTransactions extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_TransactionStatusDetails.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.urlCode), encryptDecrypt.encrypt(arg0[3])));

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
                        JSONArray transactionBetDates = object.getJSONArray("refundTransaction");
                        for (int i = 0; i < transactionBetDates.length(); i++) {

                            JSONObject object2 = transactionBetDates.getJSONObject(i);

                        }
                        refLayout.setVisibility(View.GONE);
                        UpdateIntoEPay(XnID, "1");
                        progressDialog.dismiss();
                        Constants.showToast(Activity_TransactionStatusDetails.this, getString(R.string.refund_successful));
                        finish();

                    } else {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates = object.getJSONArray("refundTransaction");
                        JSONObject object2 = transactionBetDates.getJSONObject(0);
                        result = object2.optString("result");
                        result = encryptDecryptRegister.decrypt(result);
                        Constants.showToast(Activity_TransactionStatusDetails.this, getString(R.string.network_error));

                        progressDialog.dismiss();
                    }
                }else
                {
                    Constants.showToast(Activity_TransactionStatusDetails.this, getString(R.string.network_error));
                    progressDialog.dismiss();
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
                Constants.showToast(Activity_TransactionStatusDetails.this, getString(R.string.network_error));
            }

        }
    }


    private void UpdateIntoEPay(String invNo, String refundStatus) {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.IS_REFUND, refundStatus);

        long id = db.update(DBHelper.TABLE_NAME_E_PAYMENT,values,DBHelper.INVOICE_NO +" = "+invNo, null);
    }

    private void UpdateStatusIntoEPay(String invNo, String status) {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.STATUS, status);

        long id = db.update(DBHelper.TABLE_NAME_E_PAYMENT,values,DBHelper.INVOICE_NO +" = "+invNo, null);
    }


    private void getConfirm()
    {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_for_refund_layout);
        dialog.setCancelable(false);

        TextView txtYes = (TextView) dialog.findViewById(R.id.txtYes);
        TextView txtNo = (TextView) dialog.findViewById(R.id.txtNo);

        txtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                callRefund();
            }
        });

        txtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
