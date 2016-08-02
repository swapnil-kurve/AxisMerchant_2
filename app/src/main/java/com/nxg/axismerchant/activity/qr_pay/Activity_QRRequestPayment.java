package com.nxg.axismerchant.activity.qr_pay;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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

public class Activity_QRRequestPayment extends AppCompatActivity implements View.OnClickListener
{
    EditText edtAmount, edtPrimaryId, edtSecondaryId;
    EncryptDecryptRegister encryptDecryptRegister;
    TextView txtViewAllTransactions,txtReqLabel;//, txtStaticQR, txtDynamicQR
    ImageView imgBack, imgNotification, imgProfile,imgEditPrimaryID, imgEditAmount, imgEditSecondaryID;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_requestpayment);

        edtAmount = (EditText) findViewById(R.id.edtqrAmount);
        edtPrimaryId = (EditText) findViewById(R.id.edtPID);
        edtSecondaryId = (EditText) findViewById(R.id.edtSID);

        imgEditAmount = (ImageView) findViewById(R.id.imgEdit);
        imgEditPrimaryID = (ImageView) findViewById(R.id.imgEdit1);
        imgEditSecondaryID = (ImageView) findViewById(R.id.imgEdit2);

        txtViewAllTransactions = (TextView) findViewById(R.id.txtViewAllTransactions);

        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgNotification = (ImageView) findViewById(R.id.imgNotification);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        txtReqLabel = (TextView) findViewById(R.id.txtReq);
        TextView txtNotification = (TextView) findViewById(R.id.txtNotificationCount);
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notificationArrayList = Constants.retrieveFromDatabase(this, dbHelper);
        if(notificationArrayList.size() > 0)
        {
            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setText(String.valueOf(notificationArrayList.size()));
        }

        txtViewAllTransactions.setOnClickListener(this);
//        txtStaticQR.setOnClickListener(this);
//        txtDynamicQR.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgEditAmount.setOnClickListener(this);
        imgEditPrimaryID.setOnClickListener(this);
        imgEditSecondaryID.setOnClickListener(this);

        encryptDecryptRegister = new EncryptDecryptRegister();
        if (Constants.isNetworkConnectionAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                new GetPromotions().executeOnExecutor(AsyncTask
//                        .THREAD_POOL_EXECUTOR, Constants.SERVICE_URI + "getImagesForSlider", Constants.MERCHANT_ID, Constants.MOBILE_NUM);
            } else {
//                new GetPromotions().execute(Constants.SERVICE_URI + "getImagesForSlider", Constants.MERCHANT_ID, Constants.MOBILE_NUM);
            }
        } else {
            Constants.showToast(this, "No internet available");
        }
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.txtViewAllTransactions:
                if(i == 1)
                    startActivity(new Intent(this, Activity_QRCodeGenerated.class));
                else
                    getData();
                break;

           /* case R.id.txtStaticQR:

                break;

            case R.id.txtDynamicQR:

                break;*/

            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

            case R.id.imgEdit:
                i = 0;
                edtAmount.setEnabled(true);
                edtPrimaryId.setEnabled(false);
                edtSecondaryId.setEnabled(false);
                edtAmount.setFocusable(true);
                imgEditAmount.setVisibility(View.GONE);
                imgEditPrimaryID.setVisibility(View.VISIBLE);
                imgEditSecondaryID.setVisibility(View.VISIBLE);
                break;

            case R.id.imgEdit1:
                i = 0;
                edtAmount.setEnabled(false);
                edtSecondaryId.setEnabled(false);
                edtPrimaryId.setEnabled(true);
                edtPrimaryId.setFocusable(true);
                imgEditAmount.setVisibility(View.VISIBLE);
                imgEditPrimaryID.setVisibility(View.GONE);
                imgEditSecondaryID.setVisibility(View.VISIBLE);
                break;

            case R.id.imgEdit2:
                i = 0;
                edtAmount.setEnabled(false);
                edtPrimaryId.setEnabled(false);
                edtSecondaryId.setEnabled(true);
                edtSecondaryId.setFocusable(true);
                imgEditAmount.setVisibility(View.VISIBLE);
                imgEditSecondaryID.setVisibility(View.GONE);
                imgEditPrimaryID.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void getData() {
        String mAmount = edtAmount.getText().toString().trim();
        String mPrimaryID = edtPrimaryId.getText().toString().trim();
        String mSecondaryID = edtSecondaryId.getText().toString();

        SharedPreferences preferences = getSharedPreferences(Constants.QRPaymentData, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        /*if(type.equals("dynamic")) {
            if (mAmount.equals("") && mPrimaryID.equals("") && mSecondaryID.equals("")) {
                Constants.showToast(this, "All fields are mandatory");
            } else if (mAmount.equals("")) {
                Constants.showToast(this, "Please enter amount");
            } else if (mPrimaryID.equals("")) {
                Constants.showToast(this, "Please enter Primary Id");
            } else if (mSecondaryID.equals("")) {
                Constants.showToast(this, "Please enter Secondary Id");
            } else if (Double.parseDouble(mAmount) <= 0) {
                Constants.showToast(this, "Amount should not be 0 or less");
            } else if (Double.parseDouble(mAmount) > 200000) {
                Constants.showToast(this, "Amount should not be more than 200000");
            } else {

                if (i == 1) {
                    Intent intent = new Intent(this, Activity_QRCodeGenerated.class);
                    intent.putExtra("QRType","dynamic");
                    startActivity(intent);

                } else {
                    changeToReview();
                }
            }
        }*/

//        if(type.equals("dynamic")){
            if (!mAmount.equals(""))
                editor.putString("Amount",mAmount);
            if (!mPrimaryID.equals(""))
                editor.putString("Primary_Id",mPrimaryID);
            if (!mSecondaryID.equals(""))
                editor.putString("Secondary_Id",mSecondaryID);
            editor.apply();

            Intent intent = new Intent(this, Activity_QRCodeGenerated.class);
//            intent.putExtra("QRType","dynamic");
            startActivity(intent);
//        }

//            if(type.equals("static")){
//            if(mAmount.equals(""))
//            {
//                Intent intent = new Intent(this, Activity_QRCodeGenerated.class);
//                intent.putExtra("QRType","Static");
//                startActivity(intent);
//            }else
//            {
//                changeToReview();
//            }
//        }

    }

    private void changeToReview() {
        i = 1;
        edtAmount.setEnabled(false);
        edtPrimaryId.setEnabled(false);
        edtSecondaryId.setEnabled(false);

        imgEditAmount.setVisibility(View.VISIBLE);
        imgEditPrimaryID.setVisibility(View.VISIBLE);
        imgEditSecondaryID.setVisibility(View.VISIBLE);

//        txtDynamicQR.setVisibility(View.GONE);
//        txtStaticQR.setVisibility(View.GONE);
        txtViewAllTransactions.setText("Confirm and Proceed");
        txtViewAllTransactions.setVisibility(View.VISIBLE);
        txtReqLabel.setText("Review");
    }


    public class GetPromotions extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        //String ArrURL[] = new String[3];

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_QRRequestPayment.this);
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
                if (data != null) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {

                        progressDialog.dismiss();

                    } else {
                        progressDialog.dismiss();

                    }
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }
        }
    }

}
