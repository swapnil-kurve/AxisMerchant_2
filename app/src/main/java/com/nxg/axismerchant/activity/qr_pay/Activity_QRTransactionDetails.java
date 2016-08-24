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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Activity_QRTransactionDetails extends AppCompatActivity implements View.OnClickListener {

    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    String XnID,val ;
    TextView txtResText;
    View refLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_transaction_details);

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

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey("XnId"))
        {
            XnID = bundle.getString("XnId");
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
                new GetQRXnDetails().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getMvisaTransactionById", Constants.MERCHANT_ID, Constants.MOBILE_NUM, xnID);
            } else {
                new GetQRXnDetails().execute(Constants.DEMO_SERVICE + "getMvisaTransactionById", Constants.MERCHANT_ID, Constants.MOBILE_NUM, xnID);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
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
                callRefund(val);
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
                getConfirm();
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


    private void callRefund(String val) {
        if (Constants.isNetworkConnectionAvailable(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new RefundTransactions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR,  val );
            } else {
                new RefundTransactions().execute( val );

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }


    private class RefundTransactions extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_QRTransactionDetails.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected String doInBackground(String... params) {
            String str = "";
            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            HttpPost myConnection = new HttpPost("http://merchantportal.paycraftsol.com/MerchantApp/API/Merchant/Refund");

            try {
//                myConnection.setHeader("Content-type", "application/json");
                myConnection.setHeader("Accept", "application/json");
                myConnection.setHeader("Content-type", "application/json");
                myConnection.setHeader("Accept-Encoding", "gzip");

                StringEntity entity = new StringEntity(params[0]);
                myConnection.setEntity(entity);

                response = myClient.execute(myConnection);
                str = EntityUtils.toString(response.getEntity(), "UTF-8");

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return str;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            try {

                if(!data.equals("")) {
                    JSONObject object1 = new JSONObject(data);

                    String result = object1.optString("Result");
                    String ReturnDescription = object1.optString("ReturnDescription");
                    if (result.equals("SUCCESS")) {

                        Constants.showToast(Activity_QRTransactionDetails.this, getString(R.string.refund_successful));
                        finish();


                    }else if(result.equals("FAILED")){
                        if(ReturnDescription.equalsIgnoreCase("Cannot Refund. Transaction does not exist"))
                        {
                            Constants.showToast(Activity_QRTransactionDetails.this, getString(R.string.cannot_refund));
                        }else {

                            Constants.showToast(Activity_QRTransactionDetails.this, getString(R.string.network_error));

                        }
                        progressDialog.dismiss();
                    }
                }else
                {
                    Constants.showToast(Activity_QRTransactionDetails.this,getString(R.string.network_error));
                    progressDialog.dismiss();
                }

            } catch (Exception e) {
                progressDialog.dismiss();
                e.printStackTrace();
                Constants.showToast(Activity_QRTransactionDetails.this,getString(R.string.network_error));
            }

        }
    }


    private class GetQRXnDetails extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_QRTransactionDetails.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.id), encryptDecrypt.encrypt(arg0[3])));
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
                        JSONArray getMvisaTransactionById = object2.getJSONArray("getMvisaTransactionById");

                        JSONObject object1 = getMvisaTransactionById.getJSONObject(0);
                        String mvisa_merchant_id = object1.optString("mvisa_merchant_id");
                        String merchantId = object1.optString("merchantId");
                        String txn_currency = object1.optString("txn_currency");
                        String transaction_type = object1.optString("transaction_type");
                        String customer_name = object1.optString("customer_name");
                        String secondary_id = object1.optString("secondary_id");
                        String bank_code = object1.optString("bank_code");
                        String primary_id = object1.optString("primary_id");
                        String customer_pan = object1.optString("customer_pan");
                        String auth_code = object1.optString("auth_code");
                        String ref_no = object1.optString("ref_no");
                        String settlement_amount = object1.optString("settlement_amount");
                        String txn_amount = object1.optString("txn_amount");
                        String time_stamp = object1.optString("time_stamp");
                        String onDate = object1.optString("onDate");
                        String mvisa_pan = object1.optString("mvisa_pan");
                        String tipadj_flg = object1.optString("tipadj_flg");
                        String refund_flg = object1.optString("refund_flg");
                        String terminal_status = object1.optString("terminal_status");
                        String id = object1.optString("id");
                        String transStatus = object1.optString("transStatus");
                        String isRefund = object1.optString("isRefund");
                        String tid = object1.optString("tid");

                        mvisa_merchant_id = encryptDecrypt.decrypt(mvisa_merchant_id);
                        merchantId = encryptDecrypt.decrypt(merchantId);
                        txn_currency = encryptDecrypt.decrypt(txn_currency);
                        transaction_type = encryptDecrypt.decrypt(transaction_type);
                        customer_name = encryptDecrypt.decrypt(customer_name);
                        secondary_id = encryptDecrypt.decrypt(secondary_id);
                        bank_code = encryptDecrypt.decrypt(bank_code);
                        primary_id = encryptDecrypt.decrypt(primary_id);
                        customer_pan = encryptDecrypt.decrypt(customer_pan);
                        auth_code = encryptDecrypt.decrypt(auth_code);
                        ref_no = encryptDecrypt.decrypt(ref_no);
                        settlement_amount = encryptDecrypt.decrypt(settlement_amount);
                        txn_amount = encryptDecrypt.decrypt(txn_amount);
                        time_stamp = encryptDecrypt.decrypt(time_stamp);
                        onDate = encryptDecrypt.decrypt(onDate);
                        mvisa_pan = encryptDecrypt.decrypt(mvisa_pan);
                        tipadj_flg = encryptDecrypt.decrypt(tipadj_flg);
                        refund_flg = encryptDecrypt.decrypt(refund_flg);
                        terminal_status = encryptDecrypt.decrypt(terminal_status);
                        id = encryptDecrypt.decrypt(id);
                        transStatus = encryptDecrypt.decrypt(transStatus);
                        isRefund = encryptDecrypt.decrypt(isRefund);
                        tid = encryptDecrypt.decrypt(tid);

                        if(onDate.contains("-"))
                            onDate.replace("-","/");

                        val = "[{'bank_code':'00031','session_id':'12341234','username':'8898626498','mvisa_merchant_id':'"+mvisa_merchant_id+"','password':'a01610228fe998f515a72dd730294d87','auth_code':'"+auth_code+"','ref_no':'"+ref_no+"','tid':'"+tid+"','class_name':'AllTransaction','function':'refundSR'}]";

                        Log.e("Val", val);

                        ((TextView)findViewById(R.id.txtName)).setText(customer_name);
                        ((TextView)findViewById(R.id.txtDate)).setText(onDate.split("\\s+")[0]);
                        ((TextView)findViewById(R.id.txtDate)).setText(onDate);
                        ((TextView)findViewById(R.id.txtmVisaID)).setText(mvisa_merchant_id);
                        ((TextView)findViewById(R.id.txtAuthCode)).setText(auth_code);
                        ((TextView)findViewById(R.id.rrnNo)).setText(ref_no);
                        ((TextView)findViewById(R.id.amount)).setText(getString(R.string.Rs)+txn_amount);
                        ((TextView)findViewById(R.id.remark1)).setText(primary_id);
                        ((TextView)findViewById(R.id.remark2)).setText(secondary_id);

                        if (isRefund.equals("0"))
                            refLayout.setVisibility(View.VISIBLE);
                        else {
                            txtResText.setText("Already Refunded");
                            refLayout.setEnabled(false);
                        }

                    } else {
                        Constants.showToast(Activity_QRTransactionDetails.this, getString(R.string.no_details));
                    }
                    progressDialog.dismiss();
                }else {
                    progressDialog.dismiss();
                    Constants.showToast(Activity_QRTransactionDetails.this,getString(R.string.network_error));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Constants.showToast(Activity_QRTransactionDetails.this,getString(R.string.network_error));
            }

        }
    }



}
