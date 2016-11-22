package com.axismerchant.activity.service_support;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.Activity_Notification;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Activity_SubLinks_2 extends AppCompatActivity implements View.OnClickListener {

    ImageView imgBack, imgNotification, imgProfile;
    TextView txtHeading;
    TextView txtSubCode,txtCurrentDate, txtMID;
    String MID,MOBILE, mServiceType = "";
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    EditText edtProblemDetails;//, edtVisitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_links_2);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        TextView txtSubmitRequest = (TextView) findViewById(R.id.txtSubmitRequest);
        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgNotification = (ImageView) findViewById(R.id.imgNotification);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        txtMID = (TextView) findViewById(R.id.txtMID);

        txtHeading = (TextView) findViewById(R.id.txtHeading);
        txtCurrentDate = (TextView) findViewById(R.id.txtCurrentDate);

        txtSubCode = (TextView) findViewById(R.id.txtSubcode);
        edtProblemDetails = (EditText) findViewById(R.id.edtProblemDescription);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH:mm");
        String currentDateandTime = sdf.format(new Date());
        Log.e("current Date and time", currentDateandTime + "hrs");
        txtCurrentDate.setText(currentDateandTime + "hrs");

        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);


        edtProblemDetails.setMaxLines(3);

        txtMID.setText(MID);

        Bundle bundle = getIntent().getExtras();
        if(bundle!= null)
        {
            String heading = bundle.getString("Heading");

            if(heading.equalsIgnoreCase("CallIssues")){
                txtSubCode.setText(getString(R.string.call_issues));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "CALL ISSUER";
            }else
            if(heading.equalsIgnoreCase("PaymentInquiry")){
                txtSubCode.setText(getString(R.string.payement_inquiry));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "PAYMENT INQUIRY";
            }else
            if(heading.equalsIgnoreCase("PickUpCard")){
                txtSubCode.setText(getString(R.string.pick_up_card));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "PICK UP CARD";
            }else
            if(heading.equalsIgnoreCase("DeclineCard")){
                txtSubCode.setText(getString(R.string.decline_card));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "DECLINE CARD";
            }else
            if(heading.equalsIgnoreCase("CardSwipeError")){
                txtSubCode.setText(getString(R.string.card_swipe_error));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "CARD SWIPE ERROR";
            }else
            if(heading.equalsIgnoreCase("AxisAccNo")){
                txtSubCode.setText(getString(R.string.axis_no));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "CHANGE IN ACCOUNT MODE (AXIS A/C NO.)";
            }else
            if(heading.equalsIgnoreCase("NeftRtgs")){
                txtSubCode.setText(getString(R.string.neft));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "CHANGE IN ACCOUNT MODE (NEFT/RTGS)";
            }else
            if(heading.equalsIgnoreCase("DbaName")){
                txtSubCode.setText(getString(R.string.dba));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "UPDATE MERCHANT DETAILS (DBA NAME)";
            }else
            if(heading.equalsIgnoreCase("LegalName")){
                txtSubCode.setText(getString(R.string.legal_name));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "UPDATE MERCHANT DETAILS (LEGAL NAME)";
            }else
            if(heading.equalsIgnoreCase("AddressChange")){
                txtSubCode.setText(getString(R.string.address_change));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "UPDATE MERCHANT DETAILS (ADDRESS CHANGE)";
            }else
            if(heading.equalsIgnoreCase("PhoneNo")){
                txtSubCode.setText(getString(R.string.phone_no));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "UPDATE MERCHANT DETAILS (MOBILE NUMBER)";
            }else
            if(heading.equalsIgnoreCase("NewLocation")){
                txtSubCode.setText(getString(R.string.new_loc));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "UPDATE MERCHANT DETAILS (NEW LOCATION)";
            }else
            if(heading.equalsIgnoreCase("AssetSwapping")){
                txtSubCode.setText(getString(R.string.asset_swapping));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "UPDATE MERCHANT DETAILS (ASSET SWAPPING)";
            }else
            if(heading.equalsIgnoreCase("Dcc")){
                txtSubCode.setText(getString(R.string.dcc));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "MERCHANT CREATION – DCC";
            }else
            if(heading.equalsIgnoreCase("AdditionalDcc")){
                txtSubCode.setText(getString(R.string.additional_dcc));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "MERCHANT CREATION – ADDITIONAL DCC";
            }else
            if(heading.equalsIgnoreCase("CashPos")){
                txtSubCode.setText(getString(R.string.cash_pos));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "Cash@PoS";
            }else
            if(heading.equalsIgnoreCase("Apply_mVisa")){
                txtSubCode.setText(getString(R.string.apply_for_mvisa));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "APPLY FOR mVISA / QR CODE PAY";
            }else
            if(heading.equalsIgnoreCase("MprStatmentRequest")){
                txtSubCode.setText(getString(R.string.mpr_request));
                txtHeading.setText(getString(R.string.account) + " " + getString(R.string.management));
                mServiceType = "MPR STATEMENT REQUEST (MERCHANT PAYMENT REPORT)";
            }
        }

        imgBack.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        txtSubmitRequest.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtSubmitRequest:
                sendRequest();
                break;

            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

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


    private void sendRequest() {

        if(edtProblemDetails.getText().toString().trim().length() == 0)
        {
            Constants.showToast(this, getString(R.string.invalid_problem_desc));
            edtProblemDetails.setError("");
        }else {
            callService();
        }
    }


    private void callService() {

        String ProblemDetails = edtProblemDetails.getText().toString().trim();
        String RollRequired = "0";

        String serviceType = mServiceType;

        if (Constants.isNetworkConnectionAvailable(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, "", serviceType, ProblemDetails, "", "", "", RollRequired, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetData().execute(Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, "", serviceType, ProblemDetails, "", "", "", RollRequired, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }

    private void ShowDialogReponse(String response, String request_Number, String docket_id, String response_code) {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_for_sr_request);
        dialog.setCancelable(true);

        TextView txtResponseStatus = (TextView) dialog.findViewById(R.id.txtResponseStatus);
        TextView txtRequestNumber = (TextView) dialog.findViewById(R.id.txtRequestNumber);
        ImageView imgResponse = (ImageView) dialog.findViewById(R.id.imgResponse);
        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);

        if (response.equalsIgnoreCase("Success")) {
            txtResponseStatus.setText("Success");
            txtResponseStatus.setTextColor(Color.GREEN);
            if (docket_id.equals("")) {
                txtRequestNumber.setText(getString(R.string.request_raised) + "\n" + getString(R.string.request_number) + " \n" + request_Number);
            } else {
                txtRequestNumber.setText(getString(R.string.request_raised) + "\n" + getString(R.string.request_number) + " \n" + request_Number + ",\n Docket Id " + docket_id);
            }
            imgResponse.setImageResource(R.drawable.happiness);
        } else {
            txtResponseStatus.setText("Fail");
            txtResponseStatus.setTextColor(Color.RED);
            txtRequestNumber.setText(response_code + "\n" + getString(R.string.try_later));
            imgResponse.setImageResource(R.mipmap.fail);
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

    private void logout() {
        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("KeepLoggedIn", "false");
        editor.apply();
        Intent intent = new Intent(this, Activity_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public class GetData extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Activity_SubLinks_2.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[10])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[11])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[12])));

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
                            String docket_id = object2.optString("docket_id");
                            String responseCode = object2.optString("responseCode");

                            Request_Number = encryptDecrypt.decrypt(Request_Number);
                            Call_Status = encryptDecrypt.decrypt(Call_Status);
                            docket_id = encryptDecrypt.decrypt(docket_id);
                            responseCode = encryptDecrypt.decrypt(responseCode);

                            if(Request_Number.equalsIgnoreCase(""))
                                ShowDialogReponse("Fail", "", "", "");
                            else
                                ShowDialogReponse("Success", Request_Number, docket_id, responseCode);
                        }
                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(Activity_SubLinks_2.this, getString(R.string.session_expired));
                        logout();
                    }else
                    {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray addServiceRequest = object.getJSONArray("addServiceRequest");
                        String responseCode = "";
                        for (int i = 0; i < addServiceRequest.length(); i++) {
                            JSONObject object2 = addServiceRequest.getJSONObject(i);
                            responseCode = object2.optString("result");
                            responseCode = encryptDecrypt.decrypt(responseCode);
                        }
                        ShowDialogReponse("Fail", "", "", responseCode);
                    }
                }else
                {
                    Constants.showToast(Activity_SubLinks_2.this, getString(R.string.network_error));
                }


            } catch (JSONException e) {
                progressDialog.dismiss();
                Constants.showToast(Activity_SubLinks_2.this, getString(R.string.network_error));
            }
            progressDialog.dismiss();
        }
    }


}
