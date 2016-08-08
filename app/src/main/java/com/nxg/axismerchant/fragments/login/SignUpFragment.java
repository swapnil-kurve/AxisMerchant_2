package com.nxg.axismerchant.fragments.login;


import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.start.Activity_SetOTP;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {

    EditText edtMerchantID, edtMobileNumber;
    EncryptDecryptRegister encryptDecryptRegister;
    SharedPreferences preferences;
    ImageView imgSwitch, imgErrorMID, imgErrorMobile;
    int flag = 1 ;
    private String mGCMRedID;
    double screenInches;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        getInitialize(view);
        setSize(view);

        edtMerchantID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                imgErrorMID.setVisibility(View.GONE);
            }
        });

        edtMobileNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                imgErrorMobile.setVisibility(View.GONE);
            }
        });


        return view;
    }

    private void getInitialize(View view) {
        edtMerchantID = (EditText) view.findViewById(R.id.edtMerchantID);
        edtMobileNumber = (EditText) view.findViewById(R.id.edtMobileNumber);
        imgErrorMID = (ImageView) view.findViewById(R.id.imgErrorMID);
        imgErrorMobile = (ImageView) view.findViewById(R.id.imgErrorMobile);

        TextView txtSubmit = (TextView) view.findViewById(R.id.txtSubmit);
        imgSwitch = (ImageView) view.findViewById(R.id.imgTick);

        txtSubmit.setOnClickListener(this);
        imgSwitch.setOnClickListener(this);
        imgErrorMID.setOnClickListener(this);
        imgErrorMobile.setOnClickListener(this);

        encryptDecryptRegister = new EncryptDecryptRegister();

        /*SharedPreferences prefs = getActivity().getSharedPreferences(Constants.UserDetails,
                Context.MODE_PRIVATE);
        mGCMRedID = prefs.getString("REG_ID","");

        if(mGCMRedID.equalsIgnoreCase(""))
            txtSubmit.setEnabled(false);*/
    }


    private void setSize(View view) {
        screenInches = Constants.getRes(getActivity());

        if(screenInches<= 6 && screenInches>= 5)
        {
            Constants.showToast(getActivity(), "1");
            setSize(20,18,view);
        }
        else if(screenInches<= 5 && screenInches>= 4)
        {
            Constants.showToast(getActivity(), "2");
            setSize(18,16,view);
        }
        else if(screenInches<= 4 && screenInches>= 3)
        {
            Constants.showToast(getActivity(), "3");
            setSize(16,14,view);
        }
    }

    private void setSize(int i, int i1, View view) {
        edtMerchantID.setTextSize(i1);
        edtMobileNumber.setTextSize(i1);

        ((TextView)view.findViewById(R.id.txtKeepLogin)).setTextSize(i);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtSubmit:
                    getUserData();
                break;


            case R.id.imgTick:
                if(flag == 0)
                {
                    imgSwitch.setImageResource(R.mipmap.login_tick);
                    flag = 1;
                }else
                {
                    imgSwitch.setImageResource(0);
                    flag = 0;
                }
                break;

            case R.id.imgErrorMID:
                ShowDialogHelpMID();
                break;

            case R.id.imgErrorMobile:
                ShowDialogHelpMobile();
                break;
        }
    }


    private void ShowDialogHelpMobile()
    {
        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_for_sign_up_help_mobile);

        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);
        // if button is clicked, close the custom dialog
        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        dialog.show();
    }


    private void ShowDialogHelpMID()
    {
        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_for_sign_up_help_mid);

        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);
        // if button is clicked, close the custom dialog
        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        dialog.show();
    }


    private void getUserData() {
        Constants.MERCHANT_ID = edtMerchantID.getText().toString();
        Constants.MOBILE_NUM = edtMobileNumber.getText().toString();

        if(Constants.MERCHANT_ID.length() < 15 && Constants.MOBILE_NUM.length() < 10)
        {
            Constants.showToast(getActivity(), getString(R.string.enter_valid_details));
            imgErrorMID.setVisibility(View.VISIBLE);
            imgErrorMobile.setVisibility(View.VISIBLE);

        }else if(Constants.MERCHANT_ID.equals("") || Constants.MERCHANT_ID.length() < 15)
            {
                Constants.showToast(getActivity(), getString(R.string.enter_valid_mid));
                imgErrorMID.setVisibility(View.VISIBLE);
            }else if(Constants.MOBILE_NUM.equals("") || Constants.MOBILE_NUM.length() < 10)
            {
                Constants.showToast(getActivity(), getString(R.string.enter_valid_mobile));
                imgErrorMobile.setVisibility(View.VISIBLE);
            }
            else {
            Constants.getIMEI(getActivity());
            preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("MerchantID", Constants.MERCHANT_ID);
            editor.putString("MobileNum", Constants.MOBILE_NUM);
            editor.putString("IMEI", Constants.IMEI);
            if(flag == 1)
                editor.putString("KeepFlag",String.valueOf(flag));
                editor.apply();

                if (Constants.isNetworkConnectionAvailable(getActivity())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new LoginProcess().executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "registerAndSendVeriCode", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.IMEI, "android");
                    } else {
                        new LoginProcess().execute(Constants.DEMO_SERVICE + "registerAndSendVeriCode", Constants.MERCHANT_ID, Constants.MOBILE_NUM, Constants.IMEI, "android");

                    }
                } else {
                    Constants.showToast(getActivity(), getString(R.string.no_internet));
                }
            }


    }


    private class LoginProcess extends AsyncTask<String, Void, String> {

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
                if(data != null) {
                    JSONObject object = new JSONObject(data);
                    JSONArray verifyOTP = object.getJSONArray("registerAndSendVeriCode");
                    JSONObject object1 = verifyOTP.getJSONObject(0);
                    String result = object1.optString("result");
                    String email = object1.optString("email");
                    String username = object1.optString("username");
                    String isAdmin = object1.optString("isAdmin");

                    result = encryptDecryptRegister.decrypt(result);
                    progressDialog.dismiss();

                    if (result.equals("Success") || result.equals("AlreadyRegistered")) {
                        email = encryptDecryptRegister.decrypt(email);
                        username = encryptDecryptRegister.decrypt(username);
                        isAdmin = encryptDecryptRegister.decrypt(isAdmin);

                        preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("MerchantEmail", email);
                        editor.putString("Username", username);
                        editor.putString("isAdmin", isAdmin);
                        editor.apply();

                        startActivity(new Intent(getActivity(), Activity_SetOTP.class));
                    }else {
                        Constants.showToast(getActivity(), getString(R.string.invalid_details));
                    }
                }else {
                    Constants.showToast(getActivity(), getString(R.string.network_error));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
