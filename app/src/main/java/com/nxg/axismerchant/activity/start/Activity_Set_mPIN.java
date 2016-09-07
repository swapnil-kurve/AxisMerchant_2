package com.nxg.axismerchant.activity.start;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
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

public class Activity_Set_mPIN extends AppCompatActivity implements View.OnClickListener {

    EditText edtEnter_mPIN, edtConfirm_mPIN;
    EncryptDecryptRegister encryptDecryptRegister;
    SharedPreferences preferences;
    String regID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_m_pin);

        getInitialize();

        preferences = getSharedPreferences(Constants.UserDetails, Context.MODE_PRIVATE);
        regID = preferences.getString("REG_ID", "");
    }

    private void getInitialize() {

        encryptDecryptRegister = new EncryptDecryptRegister();

        edtEnter_mPIN = (EditText) findViewById(R.id.edt_mPIN);
        edtConfirm_mPIN = (EditText) findViewById(R.id.edtConfirm);
        TextView txtSubmit = (TextView) findViewById(R.id.txtSubmit);

        txtSubmit.setOnClickListener(this);
    }



    private void getUserMPIN()
    {
        String mMPIN, mConfirmedMPIN;

        mMPIN = edtEnter_mPIN.getText().toString().trim();
        mConfirmedMPIN = edtConfirm_mPIN.getText().toString().trim();

        if(mMPIN.equals(""))
        {
            Constants.showToast(this, getString(R.string.mpin_not_provide));
        }else if(mMPIN.length()<4)
        {
            Constants.showToast(this, getString(R.string.mpin_less_digit));
        }else if(mConfirmedMPIN.equals(""))
        {
            Constants.showToast(this, getString(R.string.confirm_mpin));
        }else if(mMPIN.equals(mConfirmedMPIN))
        {
            Constants.MPIN = mMPIN;
            saveMPINToDB(mMPIN);
            preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("MPIN", mMPIN);
            editor.apply();

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a");
            String currentDateandTime = sdf.format(new Date());

            if (Constants.isNetworkConnectionAvailable(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new SetMPIN().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"setMPin", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.MPIN, regID, currentDateandTime);
                } else {
                    new SetMPIN().execute(Constants.DEMO_SERVICE +"setMPin",Constants.MERCHANT_ID,Constants.MOBILE_NUM, Constants.MPIN, regID, currentDateandTime);

                }
            } else {
                Constants.showToast(this, getString(R.string.no_internet));
            }
        }else
        {
            Constants.showToast(this, getString(R.string.mpin_not_matching));
        }
    }

    private void saveMPINToDB(String mMPIN) {
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.MPIN, mMPIN);

        long id = db.insert(DBHelper.TABLE_NAME_MPIN,null, values);
        Log.v("id", String.valueOf(id));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_m_pin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtSubmit:
                getUserMPIN();
                break;
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private class SetMPIN extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_Set_mPIN.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.verify), encryptDecryptRegister.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.gcm_reg_id), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.currentTime),encryptDecryptRegister.encrypt(arg0[5])));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                int stats = response.getStatusLine().getStatusCode();

                if (stats == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    str = data;
                }
            }catch (ParseException e1) {
                progressDialog.dismiss();
            } catch (IOException e) {
                progressDialog.dismiss();
            }
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if(!s.equalsIgnoreCase("")) {
                    JSONObject object = new JSONObject(s);
                    JSONArray verifyOTP = object.getJSONArray("setMPin");
                    JSONObject object1 = verifyOTP.getJSONObject(0);
                    String result = object1.optString("result");

                    result = encryptDecryptRegister.decrypt(result);

                    progressDialog.dismiss();
                    if (result.equals("Success")) {

                        String lastLogin = object1.optString("lastLogin");
                        lastLogin = encryptDecryptRegister.decrypt(lastLogin);

                        preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("LoggedIn", "true");
                        editor.putString("LastLogin", lastLogin);
                        int flag = preferences.getInt("KeepFlag",0);
                        if (flag == 1)
                            editor.putString("KeepLoggedIn", "true");
                        editor.apply();

                        Intent intent = new Intent(Activity_Set_mPIN.this, Activity_Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Constants.showToast(Activity_Set_mPIN.this, getString(R.string.network_error));
                    }
                }else {
                    Constants.showToast(Activity_Set_mPIN.this, getString(R.string.network_error));
                }
            } catch (JSONException e) {
            }

        }
    }
}
