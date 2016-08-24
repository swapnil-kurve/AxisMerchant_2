package com.nxg.axismerchant.activity.start;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;

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

public class Activity_SetOTP extends AppCompatActivity implements View.OnClickListener {

    EditText edtOTP;
    SharedPreferences preferences;
    EncryptDecryptRegister encryptDecryptRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        getInitialize();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey("OTP"))
        {
            String otp = bundle.getString("OTP");
            ((TextView)findViewById(R.id.dummyText)).setText(otp);
        }

        edtOTP.setOnTouchListener(otl);

        edtOTP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(edtOTP.getText().toString().length() == 6)
                {
                    getOTP();
                }
                edtOTP.setSelection(edtOTP.getText().length());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private View.OnTouchListener otl = new View.OnTouchListener() {
        public boolean onTouch (View v, MotionEvent event) {
            return true; // the listener has consumed the event
        }
    };

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void getInitialize() {

        edtOTP = (EditText) findViewById(R.id.edtOtp);
        TextView txtZero = (TextView) findViewById(R.id.zero);
        TextView txtOne = (TextView) findViewById(R.id.one);
        TextView txtTwo = (TextView) findViewById(R.id.two);
        TextView txtThree = (TextView) findViewById(R.id.three);
        TextView txtFour = (TextView) findViewById(R.id.four);
        TextView txtFive = (TextView) findViewById(R.id.five);
        TextView txtSix = (TextView) findViewById(R.id.six);
        TextView txtSeven = (TextView) findViewById(R.id.seven);
        TextView txtEight = (TextView) findViewById(R.id.eight);
        TextView txtNine = (TextView) findViewById(R.id.nine);
        ImageView imgBackspace = (ImageView) findViewById(R.id.txtBackSpace);
        TextView txtResendOTP = (TextView) findViewById(R.id.txtResend);

        txtZero.setOnClickListener(this);
        txtOne.setOnClickListener(this);
        txtTwo.setOnClickListener(this);
        txtThree.setOnClickListener(this);
        txtFour.setOnClickListener(this);
        txtFive.setOnClickListener(this);
        txtSix.setOnClickListener(this);
        txtSeven.setOnClickListener(this);
        txtEight.setOnClickListener(this);
        txtNine.setOnClickListener(this);
        imgBackspace.setOnClickListener(this);
        txtResendOTP.setOnClickListener(this);

        encryptDecryptRegister = new EncryptDecryptRegister();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_otp, menu);
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
            case R.id.zero:
                setText("0");
                break;

            case R.id.one:
                setText("1");
                break;

            case R.id.two:
                setText("2");
                break;

            case R.id.three:
                setText("3");
                break;

            case R.id.four:
                setText("4");
                break;

            case R.id.five:
                setText("5");
                break;

            case R.id.six:
                setText("6");
                break;

            case R.id.seven:
                setText("7");
                break;

            case R.id.eight:
                setText("8");
                break;

            case R.id.nine:
                setText("9");
                break;

            case R.id.txtBackSpace:
                String otp = edtOTP.getText().toString().trim();
                if(otp.length()>0)
                    edtOTP.setText(otp.substring(0,otp.length()-1));
                break;

            case R.id.txtResend:

                if (Constants.isNetworkConnectionAvailable(this)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new LoginProcess().executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "registerAndSendVeriCode", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.IMEI, "android");
                    } else {
                        new LoginProcess().execute(Constants.DEMO_SERVICE + "registerAndSendVeriCode", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.IMEI, "android");

                    }
                } else {
                    Constants.showToast(this, "No internet available");
                }

                break;
        }
    }

    private void setText(String data)
    {
        if(edtOTP.getText().toString().length() == 0)
        {
            edtOTP.setText(data);
        }else
        {
            String text = edtOTP.getText().toString();
            edtOTP.setText(text + data);
        }
    }

    private void getOTP() {
        String mOTP = edtOTP.getText().toString();

        if(mOTP.equals(""))
        {
            Constants.showToast(this, getString(R.string.enter_otp));
        }
        else
        {
            if (Constants.isNetworkConnectionAvailable(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new VerifyOTP().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"verifyOTP", Constants.MERCHANT_ID, Constants.MOBILE_NUM, mOTP);
                } else {
                    new VerifyOTP().execute(Constants.DEMO_SERVICE+"verifyOTP",Constants.MERCHANT_ID,Constants.MOBILE_NUM,mOTP);

                }
            } else {
                Constants.showToast(this, "No internet available");
            }
        }

    }


    private class VerifyOTP extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_SetOTP.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.verify), encryptDecryptRegister.encrypt(arg0[3])));
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
                e1.printStackTrace();
            } catch (IOException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if(s != null){
                JSONObject object = new JSONObject(s);
                JSONArray verifyOTP = object.getJSONArray("verifyOTP");
                JSONObject object1 = verifyOTP.getJSONObject(0);
                String result = object1.optString("result");

                result = encryptDecryptRegister.decrypt(result);
                progressDialog.dismiss();

                if(result.equals("Success"))
                {
                   startActivity(new Intent(Activity_SetOTP.this, Activity_Set_mPIN.class));
                }
                else
                {
                    Constants.showToast(Activity_SetOTP.this, getString(R.string.incorrect_otp));
                }
                }else {
                    Constants.showToast(Activity_SetOTP.this, getString(R.string.network_error));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }



    private class LoginProcess extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_SetOTP.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.deviceType), encryptDecryptRegister.encrypt(arg0[4])));
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
                if(data != null){
                JSONObject object = new JSONObject(data);
                JSONArray verifyOTP = object.getJSONArray("registerAndSendVeriCode");
                JSONObject object1 = verifyOTP.getJSONObject(0);
                String result = object1.optString("result");
                String email = object1.optString("email");
                String username = object1.optString("username");
                String isAdmin = object1.optString("isAdmin");

                result = encryptDecryptRegister.decrypt(result);
                progressDialog.dismiss();

                if(result.equals("Success") || result.equals("AlreadyRegistered"))
                {
                    email = encryptDecryptRegister.decrypt(email);
                    username = encryptDecryptRegister.decrypt(username);
                    isAdmin = encryptDecryptRegister.decrypt(isAdmin);

                    preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("MerchantEmail",email);
                    editor.putString("Username",username);
                    editor.putString("isAdmin",isAdmin);
                    editor.apply();
                }
                else
                {
                    Constants.showToast(Activity_SetOTP.this, "Details entered are not valid");
                }
                }else {
                    Constants.showToast(Activity_SetOTP.this, getString(R.string.network_error));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


}
