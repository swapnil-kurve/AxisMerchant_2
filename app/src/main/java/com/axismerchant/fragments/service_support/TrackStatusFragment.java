package com.axismerchant.fragments.service_support;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.classes.SRStatus;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by vismita.jain on 7/1/16.
 */
public class TrackStatusFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    SRStatus srStatus;
    ArrayList<SRStatus> srStatuses;
    ListView listSRStatus;
    SRStatusAdapter adapter;
    String MID,MOBILE;
    View vSearchLayout;
    EditText edtSrNo, edtTidNo;
    TextView txtSearch,txtSearch1;
    String mSRNO = "", mTIDNo = "", currentDateAndTime;
    TextView txtFromDate, txtToDate;
    int DateFlag = 0;
    Calendar myCalendar = Calendar.getInstance();
    View lyTID, lyDate;
    TextView txtSearchByDate, txtSearchBySR;
    DatePickerDialog.OnDateSetListener selectedDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };
    ProgressDialogue progressDialog;
    private int flag = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trackstatus, container, false);

        progressDialog = new ProgressDialogue();
        listSRStatus = (ListView) view.findViewById(R.id.listSRStatus);
        txtSearch = (TextView) view.findViewById(R.id.txtSearch);
        txtSearch1 = (TextView) view.findViewById(R.id.txtSearch1);
        lyDate = view.findViewById(R.id.lyDate);
        lyTID = view.findViewById(R.id.lyTid);
        vSearchLayout = view.findViewById(R.id.searchLayout);

        edtSrNo = (EditText) view.findViewById(R.id.edtSRNo);
        edtTidNo = (EditText) view.findViewById(R.id.edtTidNo);

        txtSearchByDate = (TextView) view.findViewById(R.id.txtSearchByDate);
        txtSearchBySR = (TextView) view.findViewById(R.id.txtSearchBySR);

        txtFromDate = (TextView) view.findViewById(R.id.txtFromDate);
        txtToDate = (TextView) view.findViewById(R.id.txtToDate);

        listSRStatus.setOnItemClickListener(this);
        txtSearch.setOnClickListener(this);
        txtSearch1.setOnClickListener(this);
        txtFromDate.setOnClickListener(this);
        txtToDate.setOnClickListener(this);
        txtSearchBySR.setOnClickListener(this);
        txtSearchByDate.setOnClickListener(this);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();
        srStatuses = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        currentDateAndTime = sdf.format(new Date());

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));
        Constants.retrieveMPINFromDatabase(getActivity());
        Constants.getIMEI(getActivity());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetSRStatusList().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getLatestServiceRequest", MID, MOBILE, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetSRStatusList().execute(Constants.DEMO_SERVICE + "getLatestServiceRequest", MID, MOBILE, Constants.SecretKey, Constants.AuthToken,Constants.IMEI);

            }


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtSearch:
                listSRStatus.setVisibility(View.GONE);
                vSearchLayout.setVisibility(View.VISIBLE);
                txtSearch.setVisibility(View.GONE);
                break;

            case R.id.txtSearch1:
                searchStatus();
                break;


            case R.id.txtFromDate:
                DateFlag = 0;
                new DatePickerDialog(getActivity(), selectedDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;

            case R.id.txtToDate:
                DateFlag = 1;
                new DatePickerDialog(getActivity(), selectedDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;

            case R.id.txtSearchByDate:
                lyDate.setVisibility(View.VISIBLE);
                lyTID.setVisibility(View.GONE);
                break;

            case R.id.txtSearchBySR:
                lyDate.setVisibility(View.GONE);
                lyTID.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void searchStatus() {

        if(lyTID.getVisibility() == View.VISIBLE)
        {
            if(edtSrNo.getText().toString().trim().equals("") && edtTidNo.getText().toString().trim().equals(""))
            {
                Constants.showToast(getActivity(), getString(R.string.enter_one_of_the_obove));
            }else if(!edtSrNo.getText().toString().trim().equals("") && edtTidNo.getText().toString().trim().equals(""))
            {
                mSRNO = edtSrNo.getText().toString().trim();
                SearchSR();
            }else if(edtSrNo.getText().toString().trim().equals("") && !edtTidNo.getText().toString().trim().equals(""))
            {
                mTIDNo = edtTidNo.getText().toString().trim();
                SearchSR();
            }else
            {
                mSRNO = edtSrNo.getText().toString().trim();
                mTIDNo = edtTidNo.getText().toString().trim();
                SearchSR();
            }
        }else
        {
            if(txtFromDate.getText().toString().equals(""))
            {
                Constants.showToast(getActivity(),getString(R.string.select_from_date));
            }else if(txtToDate.getText().toString().equals(""))
            {
                Constants.showToast(getActivity(),getString(R.string.select_to_date));
            }else if(!txtFromDate.getText().toString().equals("") && !txtToDate.getText().toString().equals("")) {

                flag = 1;
                String fromDate = txtFromDate.getText().toString().trim();
                String toDate = txtToDate.getText().toString().trim();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new GetSRStatusList().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getrequestsBetweenDates", MID, MOBILE, fromDate, toDate, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
                } else {
                    new GetSRStatusList().execute(Constants.DEMO_SERVICE + "getrequestsBetweenDates", MID, MOBILE, fromDate, toDate, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
                }

            }
        }


    }

    private void SearchSR()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new GetStatusListBySR().executeOnExecutor(AsyncTask
                    .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "searchSRService", MID, MOBILE, mSRNO, mTIDNo, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
        } else {
            new GetStatusListBySR().execute(Constants.DEMO_SERVICE + "searchSRService", MID, MOBILE, mSRNO, mTIDNo, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.listSRStatus)
        {
            flag = 0;
            Bundle bundle = new Bundle();
            bundle.putString("SRID",srStatuses.get(position).getServiceID());
            bundle.putString("Call_Type","Details");
            Fragment_StatusDetails statusDetails = new Fragment_StatusDetails();
            statusDetails.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.xnContainer, statusDetails).addToBackStack("statusDetails").commit();
        }
    }

    private void updateLabel() {

        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String calDate = sdf.format(myCalendar.getTime());
        try {
            if (DateFlag == 0) {
                if (!sdf.parse(currentDateAndTime).before(sdf.parse(calDate)) && !sdf.parse(currentDateAndTime).equals(sdf.parse(calDate))) {
                    txtFromDate.setText(sdf.format(myCalendar.getTime()));

                } else {
                    Constants.showToast(getActivity(), getString(R.string.from_date_should_not_less));
                }
            } else {
                if (txtFromDate.getText().toString().equals("")) {
                    Constants.showToast(getActivity(), getString(R.string.select_from_date));
                } else {
                    if (sdf.parse(calDate).after(sdf.parse(txtFromDate.getText().toString())) && !sdf.parse(calDate).after(sdf.parse(currentDateAndTime))) {
                        txtToDate.setText(sdf.format(myCalendar.getTime()));
                    } else {
                        Constants.showToast(getActivity(), getString(R.string.invalid_date));
                    }
                }
            }

        } catch (Exception e) {

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

    private class GetSRStatusList extends AsyncTask<String, Void, String>
    {
        private int len;

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
                len = arg0.length;
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                String mID = encryptDecryptRegister.encrypt(arg0[1]);
                String mobile = encryptDecryptRegister.encrypt(arg0[2]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));

                if(flag != 0)
                {
                    String mFromDate = encryptDecrypt.encrypt(arg0[3]);
                    String mToDate = encryptDecrypt.encrypt(arg0[4]);

                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.fromDate), mFromDate));
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.toDate), mToDate));
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[5])));
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[6])));
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[7])));
                }else
                {
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[3])));
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[4])));
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[5])));
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
                        srStatuses.clear();
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates;
                        if(len > 6)
                            transactionBetDates= object.getJSONArray("getrequestsBetweenDates");
                        else
                            transactionBetDates= object.getJSONArray("getLatestServiceRequest");
                        for (int i = 0; i < transactionBetDates.length(); i++) {

                            JSONObject object2 = transactionBetDates.getJSONObject(i);
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


                            srStatus = new SRStatus(merchantId,serviceID,merMobileNo,tid,serviceType,probDetails,offDays,visitTiming,contactNo,rollsRequired,
                                    serviceRequestNumber,serviceStatus,requestDate,problemSubCode,responseCode,docketId,currentStatus);
                            srStatuses.add(srStatus);
                        }

                        listSRStatus.setVisibility(View.VISIBLE);
                        vSearchLayout.setVisibility(View.GONE);
                        txtSearch.setVisibility(View.VISIBLE);
                        adapter = new SRStatusAdapter(getActivity(), srStatuses);
                        listSRStatus.setAdapter(adapter);
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

    private class SRStatusAdapter extends BaseAdapter
    {
        Context context;
        ArrayList<SRStatus> srStatuses;
        public SRStatusAdapter(Activity activity, ArrayList<SRStatus> srStatuses) {
            context = activity;
            this.srStatuses = srStatuses;
        }

        @Override
        public int getCount() {
            return srStatuses.size();
        }

        @Override
        public Object getItem(int position) {
            return srStatuses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.custom_row_for_sr_status, null);

            TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            TextView txtDocketIDTitle = (TextView) convertView.findViewById(R.id.txtDocketIDTitle);
            TextView txtDocketID = (TextView) convertView.findViewById(R.id.txtDocketID);
            TextView txtProblemCode = (TextView) convertView.findViewById(R.id.txtProblemCode);

            txtDate.setText("Requested on "+srStatuses.get(position).getRequestDate().split("\\s+")[0]);
            txtProblemCode.setText(srStatuses.get(position).getProblemSubCode());

            if (!srStatuses.get(position).getDocketId().equalsIgnoreCase(""))
            {
                txtDocketIDTitle.setText("Docket ID");
                txtDocketID.setText(srStatuses.get(position).getDocketId());
            }else
            {
                txtDocketIDTitle.setText("Reference ID");
                txtDocketID.setText(srStatuses.get(position).getServiceRequestNumber());
            }

            return convertView;
        }
    }

    private class GetStatusListBySR extends AsyncTask<String, Void, String>
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[7])));

                String tid = encryptDecrypt.encrypt(arg0[4]);
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.serviceRequestNumber),srno));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.tid),tid));


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

                        srStatuses.clear();
                        transactionBetDates = object.getJSONArray("searchSRService");

                        for (int i = 0; i < transactionBetDates.length(); i++) {

                            JSONObject object2 = transactionBetDates.getJSONObject(i);
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



                            srStatus = new SRStatus(merchantId,serviceID,merMobileNo,tid,serviceType,probDetails,offDays,visitTiming,contactNo,rollsRequired,
                                    serviceRequestNumber,serviceStatus,requestDate,problemSubCode,responseCode,docketId,currentStatus);
                            srStatuses.add(srStatus);

                        }

                        listSRStatus.setVisibility(View.VISIBLE);
                        vSearchLayout.setVisibility(View.GONE);
                        txtSearch.setVisibility(View.VISIBLE);
                        adapter = new SRStatusAdapter(getActivity(), srStatuses);
                        listSRStatus.setAdapter(adapter);
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
