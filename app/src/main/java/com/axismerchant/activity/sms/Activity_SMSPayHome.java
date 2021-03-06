package com.axismerchant.activity.sms;

import android.Manifest;
import android.app.Dialog;
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

import com.axismerchant.R;
import com.axismerchant.activity.Activity_Notification;
import com.axismerchant.activity.start.Activity_Home;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.activity.start.Activity_UserProfile;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.classes.Notification;
import com.axismerchant.custom.ProgressDialogue;
import com.axismerchant.database.DBHelper;

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


public class Activity_SMSPayHome extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final static int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 111;
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
    ProgressDialogue progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smspay);

        progressDialog = new ProgressDialogue();
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

        txtRequestNewPayment.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        txtSeeAllTransactions.setOnClickListener(this);
        imgNotification.setOnClickListener(this);


        SharedPreferences pref = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        boolean mCoach = pref.getBoolean("SMSCoach", true);
        if(mCoach)
        {
            int[] coachMarks = {R.drawable.sms_01, R.drawable.sms_02};
            Constants.onCoachMark(this, coachMarks);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("SMSCoach",false);
            editor.apply();
        }


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
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));

        simStatus = Constants.isSimSupport(Activity_SMSPayHome.this);
        isOnAirplane = Constants.isAirplaneModeOn(this);
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
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getLastMerchantUserTrans", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetLastTransactionByMer().execute(Constants.DEMO_SERVICE + "getLastMerchantUserTrans", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }



    @Override
    protected void onResume() {
        getLastTransaction();
        retrieveFromDatabase();

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
            case R.id.txtNewPayment:
                if(!isOnAirplane){
                    if(simStatus == 1) {
                        startActivity(new Intent(this, Activity_SMSPayment.class));
                    }else
                    {
                        ShowDialog(simStatus);
                    }
                }else
                {
                    Constants.showToast(this, getString(R.string.airplane_mode));
                }
                break;

            case R.id.txtSeeAllTransactions:
                startActivity(new Intent(this, Activity_AllTransactions.class));
                finish();
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
        Cursor crs = null;

        try {
            crs = db.rawQuery("select DISTINCT " + DBHelper.CUST_MOBILE + " from " + DBHelper.TABLE_NAME_E_PAYMENT + " where " + DBHelper.IS_FAVORITE + " = ?", new String[]{"True"});

            while (crs.moveToNext()) {

                String mCustMobile = crs.getString(crs.getColumnIndex(DBHelper.CUST_MOBILE));
                favoriteArrayList.add(mCustMobile);
            }

            dataAdapter = new DataAdapter(this, favoriteArrayList);
            listFavorites.setAdapter(dataAdapter);
        }catch (Exception e)
        {}
        finally {
            crs.close();
            db.close();
        }
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
                ShowDialog(simStatus);
            }
        }else {
            Constants.showToast(this, getString(R.string.airplane_mode));
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Activity_Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    private void ShowDialog(int simStatus) {
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

        switch (simStatus) {
            case 100:
                txtMsg1.setText("Please insert sim card to use this feature.");
                break;

            case 200:
                txtMsg1.setText("Your Sim network is locked.");
                break;

            case 300:
                txtMsg1.setText("Your Sim id PIN Locked.");
                break;

            case 400:
                txtMsg1.setText("Your Sim id PUK Locked.");
                break;

            case 600:
                txtMsg1.setText("Unknown sim state.");
                break;

            case 0:
                txtMsg1.setText("Please insert sim card to use this feature.");
                break;
        }

        txtConfirm.setText("Ok");
        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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

    public class GetLastTransactionByMer extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.onCreateDialog(Activity_SMSPayHome.this);
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
                        txtLabel.setText(getString(R.string.no_last_transaction));

                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(Activity_SMSPayHome.this, getString(R.string.session_expired));
                        logout();
                    }else
                    {
                         Constants.showToast(Activity_SMSPayHome.this,getString(R.string.network_error));
                    }
                }else
                {
                    Constants.showToast(Activity_SMSPayHome.this,getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                Constants.showToast(Activity_SMSPayHome.this,getString(R.string.network_error));
            }
        }
    }
}
