package com.nxg.axismerchant.fragments.service_support;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
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
public class Fragment_StatusDetails extends Fragment implements View.OnClickListener {

    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    String MID,MOBILE, callType;
    TextView txtMIDNo, txtTIDNo,txtDocketID, txtProblemCode, txtDate, txtClosingRemark, txtOk;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status_details, container, false);

        txtMIDNo = (TextView) view.findViewById(R.id.txtMMIDNumber);
        txtTIDNo = (TextView) view.findViewById(R.id.txtTIDNumber);
        txtDocketID = (TextView) view.findViewById(R.id.txtDocketID);
        txtProblemCode = (TextView) view.findViewById(R.id.txtProblemCode);
        txtDate = (TextView) view.findViewById(R.id.txtDate);
        txtClosingRemark = (TextView) view.findViewById(R.id.txtClosingRemarks);
        txtOk = (TextView) view.findViewById(R.id.txtOK);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID", "0");
        MOBILE = preferences.getString("MobileNum", "0");
        Constants.retrieveMPINFromDatabase(getActivity());
        Constants.getIMEI(getActivity());

        txtOk.setOnClickListener(this);

        Bundle bundle = getArguments();
        String mSRNo = "", mTIDNo = "";
        if(bundle != null)
        {
            callType = bundle.getString("Call_Type");
            if(callType.equals("Details")) {
                mSRNo = bundle.getString("SRNo");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new GetSRStatusDetails().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getServiceByID", MID, MOBILE, mSRNo);
                } else {
                    new GetSRStatusDetails().execute(Constants.DEMO_SERVICE + "getServiceByID", MID, MOBILE, mSRNo);

                }
            }
            else
            {
                mTIDNo = bundle.getString("TIDNo");
                mSRNo = bundle.getString("SRNo");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new GetSRStatusDetails().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "searchSRService", MID, MOBILE, mSRNo, mTIDNo);
                } else {
                    new GetSRStatusDetails().execute(Constants.DEMO_SERVICE + "searchSRService", MID, MOBILE, mSRNo, mTIDNo);

                }
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

    private class GetSRStatusDetails extends AsyncTask<String, Void, String>
    {
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
                String mID = encryptDecryptRegister.encrypt(arg0[1]);
                String mobile = encryptDecryptRegister.encrypt(arg0[2]);
                String srno = encryptDecrypt.encrypt(arg0[3]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));
                if(callType.equals("Details"))
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.serviceRequestNumber),srno));
                else{
                    String tid = encryptDecrypt.encrypt(arg0[4]);
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.serviceRequestNumber),srno));
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.tid),tid));
                }

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
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates;

                        if(callType.equals("Details"))
                            transactionBetDates = object.getJSONArray("getServiceByID");
                        else
                            transactionBetDates = object.getJSONArray("searchSRService");

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


                        txtMIDNo.setText(merchantId);
                        txtTIDNo.setText(tid);
                        txtDocketID.setText("");
                        txtProblemCode.setText(problemSubCode);
                        txtDate.setText(requestDate);
                        txtClosingRemark.setText("");

                        progressDialog.dismiss();

                    } else {
                        progressDialog.dismiss();
                        Constants.showToast(getActivity(), getString(R.string.no_details));

                    }
                }else {
                    Constants.showToast(getActivity(),getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
                Constants.showToast(getActivity(),getString(R.string.network_error));
            }

        }
    }


}
