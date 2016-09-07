package com.nxg.axismerchant.activity.sms;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.nxg.axismerchant.custom.MoneyValueFilter;
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


public class Activity_SMSPayment extends AppCompatActivity implements View.OnClickListener {

    ImageView imgBack, imgEditMobile, imgEditAmount, imgEditRemark, imgReadContact;
    TextView txtNext, txtReqLabel, txtOptional;
    EditText  edtCustMobile,edtAmount, edtRemarks;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    CheckBox cbFavorites;
    DBHelper dbHelper;
    String MID,MOBILE,mFromFavo = "No";
    int i;
    private String blockCharacterSet = "~#^|$%&*!()-+?,.<>@:;";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epayment_new);

        i = 0;
        initialize();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            if(bundle.containsKey("MobileNo")){
                String MobileNo = bundle.getString("MobileNo");
                mFromFavo = bundle.getString("FromFavo");
                retrieveFromDatabase(MobileNo);
            }
        }

        edtRemarks.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(edtRemarks.hasFocus())
                {
                    txtOptional.setVisibility(View.GONE);
                }else if(!edtRemarks.hasFocus() && edtRemarks.getText().toString().equalsIgnoreCase(""))
                {
                    txtOptional.setVisibility(View.VISIBLE);
                }
            }
        });


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

    private void initialize() {

        InputFilter[] filter = new InputFilter[2];
        filter[0] = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };
        filter[1] = new InputFilter.LengthFilter(10);

        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgEditMobile = (ImageView) findViewById(R.id.imgEdit);
        imgEditAmount = (ImageView) findViewById(R.id.imgEdit1);
        imgEditRemark = (ImageView) findViewById(R.id.imgEdit2);
        imgReadContact = (ImageView) findViewById(R.id.imgReadContact);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        txtOptional = (TextView) findViewById(R.id.txtOptional);

        cbFavorites = (CheckBox) findViewById(R.id.cbFavorites);

        txtNext = (TextView) findViewById(R.id.txtNext);

        edtCustMobile = (EditText) findViewById(R.id.edtCustMobile);
        edtAmount = (EditText) findViewById(R.id.edtAmount);
        edtRemarks = (EditText) findViewById(R.id.edtRemark);
        txtReqLabel = (TextView) findViewById(R.id.txtReq);

        edtCustMobile.setFilters(filter);
        edtAmount.setFilters(new InputFilter[]{new MoneyValueFilter()});

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        imgBack.setOnClickListener(this);
        txtNext.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgEditMobile.setOnClickListener(this);
        imgEditAmount.setOnClickListener(this);
        imgEditRemark.setOnClickListener(this);
        imgReadContact.setOnClickListener(this);
        imgNotification.setOnClickListener(this);


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

            case R.id.txtNext:
                getData();
                break;

            case R.id.imgEdit:
                i = 0;
                edtAmount.setEnabled(false);
                edtRemarks.setEnabled(false);
                edtCustMobile.setEnabled(true);
                edtCustMobile.setFocusable(true);
                imgEditMobile.setVisibility(View.GONE);
                imgEditRemark.setVisibility(View.VISIBLE);
                imgEditAmount.setVisibility(View.VISIBLE);
                imgReadContact.setVisibility(View.VISIBLE);
                break;

            case R.id.imgEdit1:
                i = 0;
                edtRemarks.setEnabled(false);
                edtCustMobile.setEnabled(false);
                edtAmount.setEnabled(true);
                edtAmount.setFocusable(true);
                imgEditMobile.setVisibility(View.VISIBLE);
                imgEditRemark.setVisibility(View.VISIBLE);
                imgEditAmount.setVisibility(View.GONE);
                imgReadContact.setVisibility(View.GONE);
                break;

            case R.id.imgEdit2:
                i = 0;
                edtAmount.setEnabled(false);
                edtCustMobile.setEnabled(true);
                edtRemarks.setEnabled(true);
                edtRemarks.setFocusable(true);
                imgEditMobile.setVisibility(View.VISIBLE);
                imgEditRemark.setVisibility(View.GONE);
                imgEditAmount.setVisibility(View.VISIBLE);
                imgReadContact.setVisibility(View.GONE);
                txtOptional.setVisibility(View.VISIBLE);

                break;

            case R.id.imgReadContact:
                getContactFromPhonebook();
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;
        }
    }

    private void getData() {
        String custMobile = "";
        custMobile = edtCustMobile.getText().toString().trim();
        String amount = edtAmount.getText().toString().trim();
        String remark = edtRemarks.getText().toString();

        if (custMobile.equals("") && amount.equals("")) {
            Constants.showToast(this, getString(R.string.no_mobile_and_amount));
        } else if (custMobile.equals("")) {
            Constants.showToast(this, getString(R.string.no_mobile_number));
        } else if (amount.equals("")) {
            Constants.showToast(this, getString(R.string.no_amount));
        } else if(amount.startsWith(".")){
            Constants.showToast(this, getString(R.string.no_amount));
        } else if (edtCustMobile.getText().length() < 10) {
            Constants.showToast(this, getString(R.string.invalid_mobile_number));
        } else if (Double.parseDouble(amount) <= 0) {
            Constants.showToast(this, getString(R.string.zero_amount));
        } else if (Double.parseDouble(amount) > 200000) {
            Constants.showToast(this, getString(R.string.amount_exceeds_200000));
        } else if (custMobile.startsWith("7") || custMobile.startsWith("8") || custMobile.startsWith("9")) {

            if (i == 1) {

                if (Constants.isNetworkConnectionAvailable(getApplicationContext())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new InsertTransactions().executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "insertTransaction", MID, MOBILE, custMobile, amount, remark);
                    } else {
                        new InsertTransactions().execute(Constants.DEMO_SERVICE + "insertTransaction", MID, MOBILE, custMobile, amount, remark);

                    }
                } else {
                    Constants.showToast(this, getString(R.string.no_internet));
                }
            } else {
                changeToReview();
            }
        }else
        {
            Constants.showToast(this, getString(R.string.enter_valid_mobile));
        }

}

    private void changeToReview() {
        i = 1;
        edtCustMobile.setEnabled(false);
        edtAmount.setEnabled(false);
        edtRemarks.setEnabled(false);

        imgEditMobile.setVisibility(View.VISIBLE);
        imgEditRemark.setVisibility(View.VISIBLE);
        imgEditAmount.setVisibility(View.VISIBLE);
        imgReadContact.setVisibility(View.GONE);
        txtOptional.setVisibility(View.GONE);
        if(mFromFavo.equals("Yes"))
            cbFavorites.setVisibility(View.GONE);
        else
            cbFavorites.setVisibility(View.VISIBLE);

        txtNext.setText("Confirm and Proceed");
        txtReqLabel.setText("Review");
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



    private class InsertTransactions extends AsyncTask<String, Void, String> {

    ProgressDialog progressDialog;
    String custMobile = "",amount = "",remark = "";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(Activity_SMSPayment.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... arg0) {
        String str = "";

        custMobile = arg0[3];
        amount = arg0[4];
        remark = arg0[5];

        try {
            HTTPUtils utils = new HTTPUtils();
            HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
            URI newURI = URI.create(arg0[0]);
            HttpPost httppost = new HttpPost(newURI);

            List<NameValuePair> nameValuePairs = new ArrayList<>(1);
            nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
            nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
            nameValuePairs.add(new BasicNameValuePair(getString(R.string.cust_mobile), encryptDecrypt.encrypt(arg0[3])));
            nameValuePairs.add(new BasicNameValuePair(getString(R.string.cust_amount), encryptDecrypt.encrypt(arg0[4])));
            nameValuePairs.add(new BasicNameValuePair(getString(R.string.remark), encryptDecrypt.encrypt(arg0[5])));

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
                    JSONArray transactionBetDates = object.getJSONArray("insertTransaction");
                    for (int i = 0; i < transactionBetDates.length(); i++) {

                        JSONObject object2 = transactionBetDates.getJSONObject(i);
                        url = object2.optString("url");
                        tid = object2.optString("tId");
                        url = encryptDecrypt.decrypt(url);
                        tid = encryptDecrypt.decrypt(tid);

                        if (custMobile.length() == 10) {
                            custMobile = "+91" + custMobile;

                            //Getting intent and PendingIntent instance
                            Intent intent = new Intent(getApplicationContext(), Activity_SMSPayHome.class);
                            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                            String Message = url;
                            //Get the SmsManager instance and call the sendTextMessage method to send message
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(custMobile, null, Message, pi, null);

                            String isFavo;
                            if (cbFavorites.isChecked())
                                isFavo = "True";
                            else
                                isFavo = "False";

                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                            String currentDateandTime = sdf.format(new Date());

//                            if(mFromFavo.equals("No"))
                                InsertIntoDatabase(custMobile, amount, remark, tid, isFavo, currentDateandTime);//tID is saved instead of Invoice Number
                        }

                    }

                    progressDialog.dismiss();
                    Constants.showToast(Activity_SMSPayment.this, getString(R.string.url_sent));
                    finish();

                } else {
                    JSONObject object = transaction.getJSONObject(1);
                    JSONArray transactionBetDates = object.getJSONArray("insertTransaction");
                    JSONObject object2 = transactionBetDates.getJSONObject(0);
                    result = object2.optString("result");
                    result = encryptDecryptRegister.decrypt(result);
                    if(result.equalsIgnoreCase("Details not added")) {
                        Constants.showToast(Activity_SMSPayment.this, getString(R.string.url_not_sent));
                    }
                    progressDialog.dismiss();
                }
            }else {
                Constants.showToast(Activity_SMSPayment.this,getString(R.string.network_error));
            }
        } catch (JSONException e) {
            progressDialog.dismiss();
            Constants.showToast(Activity_SMSPayment.this,getString(R.string.network_error));
        }

    }
}


    private void retrieveFromDatabase(String mobileNo) {
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor crs;

            crs = db.rawQuery("select DISTINCT " + DBHelper.UID + "," + DBHelper.CUST_MOBILE + ","
                    + DBHelper.AMOUNT + "," + DBHelper.REMARK + ","
                    + DBHelper.INVOICE_NO + "," + DBHelper.STATUS + " from " + DBHelper.TABLE_NAME_E_PAYMENT+
                    " where " + DBHelper.CUST_MOBILE+" = ?", new String[] {mobileNo});

        while (crs.moveToNext()) {
            String mUID = crs.getString(crs.getColumnIndex(DBHelper.UID));
            String mCustMobile = crs.getString(crs.getColumnIndex(DBHelper.CUST_MOBILE));
            String mAmount = crs.getString(crs.getColumnIndex(DBHelper.AMOUNT));
            String mRemark = crs.getString(crs.getColumnIndex(DBHelper.REMARK));
            String mInvoiceNumber = crs.getString(crs.getColumnIndex(DBHelper.INVOICE_NO));
            String mStatus = crs.getString(crs.getColumnIndex(DBHelper.STATUS));

            mCustMobile = mCustMobile.substring(3,mCustMobile.length());
            edtCustMobile.setText(mCustMobile);
            edtAmount.setText(mAmount);
            edtRemarks.setText(mRemark);

            changeToReview();

        }

    }

    final static int RQS_PICK_CONTACT = 1;
    private void getContactFromPhonebook() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, RQS_PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQS_PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                number = number.replaceAll("[^a-zA-Z0-9]+", "");
                number = number.substring(number.length()-10,number.length());
                edtCustMobile.setText(number);
                edtCustMobile.setSelection(edtCustMobile.getText().length());
            }
        }
    }


}
