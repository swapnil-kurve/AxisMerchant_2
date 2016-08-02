package com.nxg.axismerchant.activity.sms;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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


public class Activity_SMSPayHome extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    ArrayList<String> favoriteArrayList;
    DBHelper dbHelper;
    ListView listFavorites;
    DataAdapter dataAdapter;
    TextView txtMobileNo, txtDate, txtAmount, txtRemark, txtLabel;
    ImageView imgStatus;
    View lyBottom;
    String MID,MOBILE;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    int simStatus;
    boolean isOnAirplane;
    SQLiteDatabase mDatabase;
    private final static int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smspay);

        TextView txtRequestNewPayment = (TextView) findViewById(R.id.txtNewPayment);
        TextView txtSeeAllTransactions = (TextView) findViewById(R.id.txtSeeAllTransactions);
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        txtMobileNo = (TextView) findViewById(R.id.txtMobileNo);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtAmount = (TextView) findViewById(R.id.txtAmount);
        txtRemark = (TextView) findViewById(R.id.txtDetails);
        imgStatus = (ImageView) findViewById(R.id.imgResult);
        listFavorites = (ListView) findViewById(R.id.listFavorite);

        txtLabel = (TextView) findViewById(R.id.txtLabel);
        lyBottom = findViewById(R.id.lyBottom);
        TextView txtNotification = (TextView) findViewById(R.id.txtNotificationCount);
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notificationArrayList = Constants.retrieveFromDatabase(this, dbHelper);
        if(notificationArrayList.size() > 0)
        {
            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setText(String.valueOf(notificationArrayList.size()));
        }

        txtRequestNewPayment.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        txtSeeAllTransactions.setOnClickListener(this);
        imgNotification.setOnClickListener(this);

        /**
         * Run time permissions for Android M
         */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int hasSMSPermission = checkSelfPermission( Manifest.permission.SEND_SMS );

            List<String> permissions = new ArrayList<>();
            if( hasSMSPermission != PackageManager.PERMISSION_GRANTED ) {
                permissions.add( Manifest.permission.SEND_SMS );
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_SOME_FEATURES_PERMISSIONS);
            }

        }
        /************************/

        favoriteArrayList = new ArrayList<>();

        listFavorites.setOnItemClickListener(this);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        simStatus = Constants.isSimSupport(Activity_SMSPayHome.this);
        isOnAirplane = Constants.isAirplaneModeOn(this);

        if (!isTableExists()) {
            if (Constants.isNetworkConnectionAvailable(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new GetEPayData().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getLatestMerchantUserTrans", MID, MOBILE, "All");//arrTitle[pageNO]);
                } else {
                    new GetEPayData().execute(Constants.DEMO_SERVICE + "getLatestMerchantUserTrans", MID, MOBILE, "All");//arrTitle[pageNO]);

                }
            } else {
                Constants.showToast(this, "No internet available");
            }

        } else {

            retrieveFromDatabase();
        }

        getLastTransaction();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_CODE_SOME_FEATURES_PERMISSIONS: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    private void getLastTransaction() {
        if (Constants.isNetworkConnectionAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetLastTransactionByMer().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getLastMerchantUserTrans", MID, MOBILE);
            } else {
                new GetLastTransactionByMer().execute(Constants.DEMO_SERVICE + "getLastMerchantUserTrans", MID, MOBILE);

            }
        } else {
            Constants.showToast(this, "No internet available");
        }
    }


    public boolean isTableExists() {
        dbHelper = new DBHelper(this);
        mDatabase = dbHelper.getReadableDatabase();

        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = dbHelper.getReadableDatabase();
        }

        if (!mDatabase.isReadOnly()) {
            mDatabase.close();
            mDatabase = dbHelper.getReadableDatabase();
        }

        Cursor cursor = mDatabase.rawQuery("select * from "+DBHelper.TABLE_NAME_E_PAYMENT, null);
        if (cursor != null) {
            Log.e("Data Count", "" + cursor.getCount());
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        retrieveFromDatabase();

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtNewPayment:
//                if(!isOnAirplane){
//                    if(simStatus == 1) {
                        startActivity(new Intent(this, Activity_SMSPayment.class));
//                    }else
//                    {
//                        ShowDialog();
//                    }
//                }else
//                {
//                    Constants.showToast(this,"You are on Airplane Mode");
//                }
                break;

            case R.id.txtSeeAllTransactions:
                startActivity(new Intent(this, Activity_AllTransactions.class));
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


    private void retrieveFromDatabase() {
        favoriteArrayList.clear();
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor crs;

        crs = db.rawQuery("select DISTINCT " + DBHelper.CUST_MOBILE + " from " + DBHelper.TABLE_NAME_E_PAYMENT+ " where " + DBHelper.IS_FAVORITE+" = ?", new String[] {"True"});

        while (crs.moveToNext()) {

            String mCustMobile = crs.getString(crs.getColumnIndex(DBHelper.CUST_MOBILE));
            favoriteArrayList.add(mCustMobile);
        }

        dataAdapter = new DataAdapter(this, favoriteArrayList);
        listFavorites.setAdapter(dataAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(!isOnAirplane) {
            if (simStatus == 1) {
                if (parent.getId() == R.id.listFavorite) {
                    Intent intent = new Intent(this, Activity_SMSPayment.class);
                    intent.putExtra("MobileNo", favoriteArrayList.get(position));
                    intent.putExtra("FromFavo", "Yes");
                    startActivity(intent);
                }
            } else {
                ShowDialog();
            }
        }else {
            Constants.showToast(this, "You are on Airplane Mode");
        }
    }


    private class DataAdapter extends BaseAdapter {
        ArrayList<String> favoriteArrayList;
        Context context;

        public DataAdapter(Context context, ArrayList<String> statusArrayList) {
            this.context = context;
            this.favoriteArrayList = statusArrayList;
        }

        @Override
        public int getCount() {
            return favoriteArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return favoriteArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_layout_for_favorite, null);

            TextView txtMobileNo = (TextView) view.findViewById(R.id.txtMobile);

            txtMobileNo.setText(favoriteArrayList.get(position));

            return view;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Activity_Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    public class GetLastTransactionByMer extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_SMSPayHome.this);
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
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray getImagesForSlider = object.getJSONArray("getLastMerchantUserTrans");

                        JSONObject object2 = getImagesForSlider.getJSONObject(0);
                        String custMobile = object2.optString("custMobile");
                        String transDate = object2.optString("transDate");
                        String transAmt = object2.optString("transAmt");
                        String remark = object2.optString("remark");
                        String transStatus = object2.optString("transStatus");

                        custMobile = encryptDecrypt.decrypt(custMobile);
                        transDate = encryptDecrypt.decrypt(transDate);
                        transAmt = encryptDecrypt.decrypt(transAmt);
                        remark = encryptDecrypt.decrypt(remark);
                        transStatus = encryptDecrypt.decrypt(transStatus);

                        if(transStatus.equalsIgnoreCase("Success"))
                        {
                            imgStatus.setImageResource(R.mipmap.successfull);
                        }else if(transStatus.equalsIgnoreCase("Pending"))
                        {
                            imgStatus.setImageResource(R.mipmap.pending);
                        }else if(transStatus.equalsIgnoreCase("Failed"))
                        {
                            imgStatus.setImageResource(R.mipmap.fail);
                        }

                        transDate = transDate.split("\\s+")[0];

                        txtMobileNo.setText(custMobile);
                        txtRemark.setText(remark);
                        txtDate.setText(transDate);
                        txtAmount.setText(transAmt);


                        progressDialog.dismiss();

                    } else if(result.equalsIgnoreCase("Failure")){
                        progressDialog.dismiss();
                        lyBottom.setVisibility(View.GONE);
                        txtLabel.setText("No Last Transaction Found");

                    }else
                    {
                         Constants.showToast(Activity_SMSPayHome.this,"Network error occurred. Please try again later");
                    }
                }else
                {
                    Constants.showToast(Activity_SMSPayHome.this,"Network error occurred. Please try again later");
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
                Constants.showToast(Activity_SMSPayHome.this,"Network error occurred. Please try again later");
            }
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


    private class GetEPayData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_SMSPayHome.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.transStatus), encryptDecryptRegister.encrypt(arg0[3])));
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
                        JSONArray getLatestMerchantUserTrans = object2.getJSONArray("getLatestMerchantUserTrans");

                        for (int i = 0; i < getLatestMerchantUserTrans.length(); i++) {
                            JSONObject object1 = getLatestMerchantUserTrans.getJSONObject(i);
                            String transactionId = object1.optString("transactionId");
                            String custMobile = object1.optString("custMobile");
                            String transAmt = object1.optString("transAmt");
                            String remark = object1.optString("remark");
                            String transStatus = object1.optString("transStatus");
                            String transDate = object1.optString("transDate");
                            String isRefund = object1.optString("isRefund");

                            transactionId = encryptDecrypt.decrypt(transactionId);
                            custMobile = encryptDecrypt.decrypt(custMobile);
                            transAmt = encryptDecrypt.decrypt(transAmt);
                            remark = encryptDecrypt.decrypt(remark);
                            transStatus = encryptDecrypt.decrypt(transStatus);
                            transDate = encryptDecrypt.decrypt(transDate);
                            isRefund = encryptDecrypt.decrypt(isRefund);

                            InsertIntoDatabase(custMobile, transAmt, remark, transactionId, transStatus,transDate,isRefund);
                        }

                        retrieveFromDatabase();
                    } else {
//                        Constants.showToast(Activity_SMSPayHome.this, "No details found.");

                    }
                    progressDialog.dismiss();
                }else {
                    progressDialog.dismiss();
                    Constants.showToast(Activity_SMSPayHome.this, "Network error occurred. Please try again later");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Constants.showToast(Activity_SMSPayHome.this,"Network error occurred. Please try again later");
            }

        }
    }


    private void InsertIntoDatabase(String custMobile, String amount, String remark, String invoiceNum, String status, String transDate, String isRefund) {
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.CUST_MOBILE, custMobile);
        values.put(DBHelper.AMOUNT, amount);
        values.put(DBHelper.REMARK, remark);
        values.put(DBHelper.INVOICE_NO, invoiceNum);
        values.put(DBHelper.STATUS, status);
        values.put(DBHelper.IS_REFUND, isRefund);
        values.put(DBHelper.TRANS_DATE, transDate);

        long id = db.insert(DBHelper.TABLE_NAME_E_PAYMENT, null, values);
        Log.v("id", String.valueOf(id));
    }

}
