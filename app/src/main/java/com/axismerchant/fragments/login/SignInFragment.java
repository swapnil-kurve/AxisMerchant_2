package com.axismerchant.fragments.login;


import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.start.Activity_Home;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.activity.start.Activity_SetOTP;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment implements View.OnClickListener {

    EditText edtMPIN;
    ImageView imgErrorMPIN, imgSwitch;
    SharedPreferences preferences;
    EncryptDecryptRegister encryptDecryptRegister;
    int flag = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        getInitialize(view);

        edtMPIN.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                imgErrorMPIN.setVisibility(View.GONE);
            }
        });

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        Constants.MERCHANT_ID = preferences.getString("MerchantID", "");
        Constants.MOBILE_NUM = preferences.getString("MobileNum", "");
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());

        return view;
    }

    private void getInitialize(View view) {
        edtMPIN = (EditText) view.findViewById(R.id.edtmPIN);
        imgErrorMPIN = (ImageView) view.findViewById(R.id.imgErrorMPIN);
        imgSwitch = (ImageView) view.findViewById(R.id.imgTick);

        TextView txtSubmit = (TextView) view.findViewById(R.id.txtSubmit);
        TextView txtForgotMPIN = (TextView) view.findViewById(R.id.txtForgotMPIN);

        txtSubmit.setOnClickListener(this);
        txtForgotMPIN.setOnClickListener(this);
        imgSwitch.setOnClickListener(this);

        encryptDecryptRegister = new EncryptDecryptRegister();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtSubmit:
                getMPIN();
                break;

            case R.id.txtForgotMPIN:
                forgotMPIN();
                break;

            case R.id.imgTick:
                if (flag == 0) {
                    imgSwitch.setImageResource(R.mipmap.login_tick);
                    flag = 1;
                } else {
                    imgSwitch.setImageResource(0);
                    flag = 0;
                }
                break;
        }
    }

    private void forgotMPIN() {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new ForgotMPIN().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "forgotpin", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.IMEI, "android",Constants.SecretKey, Constants.AuthToken,Constants.IMEI);
            } else {
                new ForgotMPIN().execute(Constants.DEMO_SERVICE + "forgotpin", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.IMEI, "android",Constants.SecretKey, Constants.AuthToken,Constants.IMEI);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void getMPIN() {
        String mMPIN = edtMPIN.getText().toString().trim();
        if (mMPIN.equals("")) {
            Constants.showToast(getActivity(), getString(R.string.mpin_not_provide));
        } else if (mMPIN.length() < 4) {
            Constants.showToast(getActivity(), getString(R.string.mpin_less_digit));
            imgErrorMPIN.setVisibility(View.VISIBLE);
        } else {
            Constants.getIMEI(getActivity());
            Constants.retrieveMPINFromDatabase(getActivity());
            Constants.MPIN = mMPIN;

            SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("MPIN", mMPIN);
            editor.apply();

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a");
            String currentDateandTime = sdf.format(new Date());

            if (Constants.isNetworkConnectionAvailable(getActivity())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new VerifyMPIN().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "verifyPin", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.MPIN, Constants.IMEI, currentDateandTime,Constants.SecretKey, Constants.AuthToken);
                } else {
                    new VerifyMPIN().execute(Constants.DEMO_SERVICE + "verifyPin", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.MPIN, Constants.IMEI, currentDateandTime,Constants.SecretKey, Constants.AuthToken);

                }
            } else {
                Constants.showToast(getActivity(), getString(R.string.no_internet));
            }
        }
    }


    private class VerifyMPIN extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mpin), encryptDecryptRegister.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no1), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.currentTime), encryptDecryptRegister.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[7])));

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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if (s != null) {
                    JSONObject object = new JSONObject(s);
                    JSONArray verifyOTP = object.getJSONArray("verifyPin");
                    JSONObject object1 = verifyOTP.getJSONObject(0);
                    String result = object1.optString("result");

                    result = encryptDecryptRegister.decrypt(result);

                    progressDialog.dismiss();
                    if (result.equals("Success")) {
                        String lastLogin = object1.optString("lastLogin");
                        String authToken = object1.optString("authToken");

                        lastLogin = encryptDecryptRegister.decrypt(lastLogin);
                        authToken = encryptDecryptRegister.decrypt(authToken);

                        Constants.AuthToken = authToken;

                        preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("LoggedIn", "true");
                        editor.putString("LastLogin", lastLogin);
                        if (flag == 1)
                            editor.putString("KeepLoggedIn", "true");
                        editor.apply();

                        Intent intent = new Intent(getActivity(), Activity_Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();

                    } else if(result.equalsIgnoreCase("Invalid IMEI No"))
                    {
                        ShowDialog();
                    }else {
                        Constants.showToast(getActivity(), getString(R.string.wrong_mpin));
                    }
                } else {
                    Constants.showToast(getActivity(), getString(R.string.network_error));
                }
            } catch (JSONException e) {
            }

        }
    }


    private class ForgotMPIN extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[6])));

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
                if (data != null) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object0 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object0.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {

                        JSONObject object1 = transaction.getJSONObject(1);

                        JSONArray forgotpin = object1.getJSONArray("forgotpin");
                        JSONObject object = forgotpin.getJSONObject(0);

                        String authToken = object.optString("authToken");
                        Constants.AuthToken = encryptDecryptRegister.decrypt(authToken);

                        startActivity(new Intent(getActivity(), Activity_SetOTP.class));
                    } else {
                        Constants.showToast(getActivity(), getString(R.string.request_cannot_process));
                    }
                }
                progressDialog.dismiss();
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }


    private void ShowDialog()
    {
        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_for_sign_in);
        dialog.setCancelable(false);

        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);

        // if button is clicked, close the custom dialog
        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                changeToSignUp();
            }
        });

        dialog.show();
    }


    private void changeToSignUp()
    {
        (getActivity().findViewById(R.id.layoutSignIn)).setVisibility(View.GONE);
        (getActivity().findViewById(R.id.layoutSignUp)).setVisibility(View.VISIBLE);
        (getActivity().findViewById(R.id.viewSignUp)).setBackgroundColor(Color.WHITE);
        (getActivity().findViewById(R.id.viewSignIn)).setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        SignUpFragment signUpFragment = new SignUpFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, signUpFragment).commit();
    }

}
