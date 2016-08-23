package com.nxg.axismerchant.fragments.service_support;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.SRStatus;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trackstatus, container, false);

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
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        Constants.retrieveMPINFromDatabase(getActivity());
        Constants.getIMEI(getActivity());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetSRStatusList().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getLatestServiceRequest", MID, MOBILE);
            } else {
                new GetSRStatusList().execute(Constants.DEMO_SERVICE + "getLatestServiceRequest", MID, MOBILE);

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
                Constants.showToast(getActivity(), "Please enter at least one of the above field");
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
                Constants.showToast(getActivity(),"Please provide from date");
            }else if(txtToDate.getText().toString().equals(""))
            {
                Constants.showToast(getActivity(),"Please provide to date");
            }else if(!txtFromDate.getText().toString().equals("") && !txtToDate.getText().toString().equals("")) {

                String fromDate = txtFromDate.getText().toString().trim();
                String toDate = txtToDate.getText().toString().trim();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new GetSRStatusList().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getrequestsBetweenDates", MID, MOBILE, fromDate, toDate);
                } else {
                    new GetSRStatusList().execute(Constants.DEMO_SERVICE + "getrequestsBetweenDates", MID, MOBILE, fromDate, toDate);
                }

            }
        }


    }

    private void SearchSR()
    {
        /*Bundle bundle = new Bundle();
        bundle.putString("SRNo",mSRNO);
        bundle.putString("TIDNo",mTIDNo);
        bundle.putString("Call_Type","Search");
        Fragment_StatusDetails statusDetails = new Fragment_StatusDetails();
        statusDetails.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.xnContainer, statusDetails).commit();*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new GetStatusListBySR().executeOnExecutor(AsyncTask
                    .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "searchSRService", MID, MOBILE, mSRNO, mTIDNo);
        } else {
            new GetStatusListBySR().execute(Constants.DEMO_SERVICE + "searchSRService", MID, MOBILE, mSRNO, mTIDNo);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.listSRStatus)
        {
            Bundle bundle = new Bundle();
            bundle.putString("SRNo",srStatuses.get(position).getServiceRequestNumber());
            bundle.putString("Call_Type","Details");
            Fragment_StatusDetails statusDetails = new Fragment_StatusDetails();
            statusDetails.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.xnContainer, statusDetails).commit();
        }
    }


    private class GetSRStatusList extends AsyncTask<String, Void, String>
    {
        private int len ;
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

                if(arg0.length > 3)
                {
                    String mFromDate = encryptDecrypt.encrypt(arg0[3]);
                    String mToDate = encryptDecrypt.encrypt(arg0[4]);

                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.fromDate), mFromDate));
                    nameValuePairs.add(new BasicNameValuePair(getString(R.string.toDate), mToDate));
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
                        srStatuses.clear();
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates;
                        if(len > 3)
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

                            srStatus = new SRStatus(merchantId,serviceID,merMobileNo,tid,serviceType,probDetails,offDays,visitTiming,contactNo,rollsRequired,
                                    serviceRequestNumber,serviceStatus,requestDate,problemSubCode);
                            srStatuses.add(srStatus);
                        }

                        listSRStatus.setVisibility(View.VISIBLE);
                        vSearchLayout.setVisibility(View.GONE);
                        txtSearch.setVisibility(View.VISIBLE);
                        adapter = new SRStatusAdapter(getActivity(), srStatuses);
                        listSRStatus.setAdapter(adapter);
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
            convertView  = inflater.inflate(R.layout.custom_row_for_notification, null);

            TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMsg);

            txtDate.setText(srStatuses.get(position).getRequestDate());
            txtMessage.setText("TID - "+srStatuses.get(position).getTid());

            return convertView;
        }
    }


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


    private void updateLabel() {

        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String calDate = sdf.format(myCalendar.getTime());
        try {
            if (DateFlag == 0) {
                if (!sdf.parse(currentDateAndTime).before(sdf.parse(calDate)) && !sdf.parse(currentDateAndTime).equals(sdf.parse(calDate))) {
                    txtFromDate.setText(sdf.format(myCalendar.getTime()));

                } else {
                    Constants.showToast(getActivity(), "From date should be less than today's date");
                }
            } else {
                if(txtFromDate.getText().toString().equals("")){
                    Constants.showToast(getActivity(), "Please enter from date");
                }else {
                    if (sdf.parse(calDate).after(sdf.parse(txtFromDate.getText().toString())) && !sdf.parse(calDate).after(sdf.parse(currentDateAndTime))) {
                        txtToDate.setText(sdf.format(myCalendar.getTime()));
                    } else {
                        Constants.showToast(getActivity(), "Enter valid date");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private class GetStatusListBySR extends AsyncTask<String, Void, String>
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



                            srStatus = new SRStatus(merchantId,serviceID,merMobileNo,tid,serviceType,probDetails,offDays,visitTiming,contactNo,rollsRequired,
                                    serviceRequestNumber,serviceStatus,requestDate,problemSubCode);
                            srStatuses.add(srStatus);

                        }

                        listSRStatus.setVisibility(View.VISIBLE);
                        vSearchLayout.setVisibility(View.GONE);
                        txtSearch.setVisibility(View.VISIBLE);
                        adapter = new SRStatusAdapter(getActivity(), srStatuses);
                        listSRStatus.setAdapter(adapter);
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
