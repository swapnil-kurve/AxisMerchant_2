package com.axismerchant.fragments.service_support;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.custom.ProgressDialogue;

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
public class Fragment_StatusDetails extends Fragment implements View.OnClickListener {

    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    String MID,MOBILE;
    TextView txtMIDNo, txtTIDNo, txtDocketID, txtProblemCode, txtDate, txtClosingRemark, txtOk, txtResponseCode, txtCurrentStatus, txtTIDTitle, textDocketIDTitle;
    ProgressDialogue progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status_details, container, false);

        progressDialog = new ProgressDialogue();
        txtMIDNo = (TextView) view.findViewById(R.id.txtMMIDNumber);
        txtTIDNo = (TextView) view.findViewById(R.id.txtTIDNumber);
        txtDocketID = (TextView) view.findViewById(R.id.txtDocketID);
        txtProblemCode = (TextView) view.findViewById(R.id.txtProblemCode);
        txtDate = (TextView) view.findViewById(R.id.txtDate);
        txtClosingRemark = (TextView) view.findViewById(R.id.txtClosingRemarks);
        txtOk = (TextView) view.findViewById(R.id.txtOK);
        txtCurrentStatus = (TextView) view.findViewById(R.id.txtCurrentStatus);
        txtResponseCode = (TextView) view.findViewById(R.id.txtResponseCode);
        txtTIDTitle = (TextView) view.findViewById(R.id.textTIDNumber);
        textDocketIDTitle = (TextView) view.findViewById(R.id.textDocketID);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID", "0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum", "0"));
        Constants.retrieveMPINFromDatabase(getActivity());
        Constants.getIMEI(getActivity());

        txtOk.setOnClickListener(this);

        Bundle bundle = getArguments();
        String mSRID = "";
        if(bundle != null)
        {
            mSRID = bundle.getString("SRID");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetSRStatusDetails().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getServiceByID", MID, MOBILE, mSRID, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetSRStatusDetails().execute(Constants.DEMO_SERVICE + "getServiceByID", MID, MOBILE, mSRID, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }

        }



        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtOK:
                getActivity().onBackPressed();
                break;
        }
    }

    private void logout() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("KeepLoggedIn", "false");
        editor.apply();
        Intent intent = new Intent(getActivity(), Activity_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private class GetSRStatusDetails extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.onCreateDialog(getActivity());
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
                String mID = encryptDecryptRegister.encrypt(arg0[1]);
                String mobile = encryptDecryptRegister.encrypt(arg0[2]);
                String srno = encryptDecrypt.encrypt(arg0[3]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.serviceRequestNumber),srno));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[6])));

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

            try{
                if(data != null) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates;

                            transactionBetDates = object.getJSONArray("getServiceByID");

                            JSONObject object2 = transactionBetDates.getJSONObject(0);
                            String merchantId = object2.optString("merchantId");
                            String serviceID = object2.optString("serviceID");
                            String merMobileNo = object2.optString("merMobileNo");
                            String tid = object2.optString("tid");
                            String serviceType = object2.optString("serviceType");
                            String probDetails = object2.optString("probDetails");
                            String offDays = object2.optString("offDays");
                            String visitTiming = object2.optString("visitTiming");
                            String contactNo = object2.optString("contactNo");
                            String rollsRequired = object2.optString("rollsRequired");
                            String serviceRequestNumber = object2.optString("serviceRequestNumber");
                            String serviceStatus = object2.optString("serviceStatus");
                            String requestDate = object2.optString("requestDate");
                            String problemSubCode = object2.optString("problemSubCode");
                            String responseCode = object2.optString("responseCode");
                            String currentStatus = object2.optString("currentStatus");
                            String docketId = object2.optString("docketId");

                            merchantId = encryptDecrypt.decrypt(merchantId);
                            serviceID = encryptDecrypt.decrypt(serviceID);
                            merMobileNo = encryptDecrypt.decrypt(merMobileNo);
                            tid = encryptDecrypt.decrypt(tid);
                            serviceType = encryptDecrypt.decrypt(serviceType);
                            probDetails = encryptDecrypt.decrypt(probDetails);
                            offDays = encryptDecrypt.decrypt(offDays);
                            visitTiming = encryptDecrypt.decrypt(visitTiming);
                            contactNo = encryptDecrypt.decrypt(contactNo);
                            rollsRequired = encryptDecrypt.decrypt(rollsRequired);
                            serviceRequestNumber = encryptDecrypt.decrypt(serviceRequestNumber);
                            serviceStatus = encryptDecrypt.decrypt(serviceStatus);
                            requestDate = encryptDecrypt.decrypt(requestDate);
                            problemSubCode = encryptDecrypt.decrypt(problemSubCode);
                            responseCode = encryptDecrypt.decrypt(responseCode);
                            currentStatus = encryptDecrypt.decrypt(currentStatus);
                            docketId = encryptDecrypt.decrypt(docketId);


                        if (tid.equalsIgnoreCase("") || tid.equalsIgnoreCase("Null")) {
                            txtTIDNo.setVisibility(View.GONE);
                            txtTIDTitle.setVisibility(View.GONE);
                        }
                        txtMIDNo.setText(merchantId);
                        txtTIDNo.setText(tid);
//                        txtDocketID.setText(docketId);
                        txtProblemCode.setText(problemSubCode);
                        txtDate.setText(requestDate);
                        txtClosingRemark.setText(serviceStatus);
                        txtResponseCode.setText(responseCode);
                        txtCurrentStatus.setText(currentStatus);

                        if (!docketId.equalsIgnoreCase("")) {
                            textDocketIDTitle.setText("Docket ID");
                            txtDocketID.setText(docketId);
                        } else {
                            textDocketIDTitle.setText("Reference ID");
                            txtDocketID.setText(serviceRequestNumber);
                        }


                        progressDialog.dismiss();

                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(getActivity(), getString(R.string.session_expired));
                        logout();
                    } else {
                        progressDialog.dismiss();
                        Constants.showToast(getActivity(), getString(R.string.no_details));

                    }
                }else {
                    Constants.showToast(getActivity(),getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                Constants.showToast(getActivity(),getString(R.string.network_error));
            }

        }
    }

}
