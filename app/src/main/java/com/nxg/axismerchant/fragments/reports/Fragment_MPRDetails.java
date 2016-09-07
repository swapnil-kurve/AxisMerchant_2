package com.nxg.axismerchant.fragments.reports;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_MPRDetails extends Fragment implements View.OnClickListener {

    TextView txtGrossAmount,txtMDR,txtServiceTax,txtHoldAmount,txtAdjustments,txtCashPos, txtPaymentDate,txtNoOfTxn,txtTotalValue, txtNetAmount,txtFromDate, txtToDate;
    String MOBILE, MID, currentDateAndTime;
    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    private View lyEmail,lyDetailsLayout, lyShowEmailButton;
    Calendar myCalendar = Calendar.getInstance();
    int DateFlag = 0;
    public static int flag = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_fragment_for_mpr, container, false);

        getInitialize(view);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        encryptDecryptRegister =  new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        currentDateAndTime = sdf.format(new Date());

        Bundle bundle = getArguments();
        String mDate = "";
        if(bundle != null && bundle.containsKey("Date"))
        {
            mDate = bundle.getString("Date");
        }

        flag = 1;
        getMPRDetails(mDate);
        return view;
    }


    private void getInitialize(View view) {
        TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
        TextView txtMID = (TextView) view.findViewById(R.id.txtMID);
        TextView txtConfirm = (TextView) view.findViewById(R.id.txtConfirm);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH:mm");
        String currentDateandTime = sdf.format(new Date());
        Log.e("current Date and time", currentDateandTime + "hrs");
        txtDate.setText(currentDateandTime + "hrs");

        txtMID.setText(Constants.MERCHANT_ID);

        lyEmail = view.findViewById(R.id.lyEmail);
        lyDetailsLayout = view.findViewById(R.id.lyDetails);
        lyShowEmailButton = view.findViewById(R.id.lyShowEmail);

        txtGrossAmount = (TextView) view.findViewById(R.id.txtGrossAmount);
        txtMDR = (TextView) view.findViewById(R.id.txtMdr);
        txtServiceTax = (TextView) view.findViewById(R.id.txtServiceTax);
        txtHoldAmount = (TextView) view.findViewById(R.id.txtHoldAmount);
        txtAdjustments = (TextView) view.findViewById(R.id.txtAdjustment);
        txtCashPos = (TextView) view.findViewById(R.id.txtCashPos);
        txtNoOfTxn = (TextView) view.findViewById(R.id.txtCountOfXn);
        txtPaymentDate = (TextView) view.findViewById(R.id.txtPaymentDate);
        txtTotalValue = (TextView) view.findViewById(R.id.txtTotalValue);
        txtNetAmount = (TextView) view.findViewById(R.id.txtNetAmount);

        txtFromDate = (TextView) view.findViewById(R.id.txtFromDate);
        txtToDate = (TextView) view.findViewById(R.id.txtToDate);

        txtFromDate.setOnClickListener(this);
        txtToDate.setOnClickListener(this);
        txtConfirm.setOnClickListener(this);
        lyShowEmailButton.setOnClickListener(this);
    }



    private void getMPRDetails(String transDate) {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetMPRDetails().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getMPRDetailsForDate", MID, MOBILE,transDate);
            } else {
                new GetMPRDetails().execute(Constants.DEMO_SERVICE+"getMPRDetailsForDate", MID, MOBILE,transDate);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {

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

            case R.id.txtConfirm:
                SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref,Context.MODE_PRIVATE);
                String email = preferences.getString("MerchantEmail","");
                if(email.equalsIgnoreCase(""))
                    ShowDialog("No");
                else
                    sendEmail();
                break;

            case R.id.lyShowEmail:
                lyDetailsLayout.setVisibility(View.GONE);
                lyEmail.setVisibility(View.VISIBLE);
                break;
        }

    }


    private void sendEmail() {
        if(txtFromDate.getText().toString().equals(""))
        {
            Constants.showToast(getActivity(),"Please provide from date");
        }else if(txtToDate.getText().toString().equals(""))
        {
            Constants.showToast(getActivity(),"Please provide to date");
        }else
        {
            String fromDate = txtFromDate.getText().toString().trim();
            String toDate = txtToDate.getText().toString().trim();

            if (Constants.isNetworkConnectionAvailable(getActivity())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new SendData().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"addEmailRequest", MID, MOBILE,fromDate,toDate);
                } else {
                    new SendData().execute(Constants.DEMO_SERVICE+"addEmailRequest", MID, MOBILE,fromDate,toDate);

                }
            } else {
                Constants.showToast(getActivity(), getString(R.string.no_internet));
            }
        }
    }



    private class SendData extends AsyncTask<String, Void, String>
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
                String duration = encryptDecrypt.encrypt(arg0[3]);
                String criteria = encryptDecrypt.encrypt(arg0[4]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.fromdate), duration));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.todate), criteria));

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
                if(data != null){
                JSONArray transaction = new JSONArray(data);
                JSONObject object1 = transaction.getJSONObject(0);

                JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                JSONObject obj = rowResponse.getJSONObject(0);
                String result = obj.optString("result");

                result = encryptDecryptRegister.decrypt(result);
                if(result.equals("Success"))
                {
                    progressDialog.dismiss();
                    ShowDialog("yes");
                }
                else {
                    progressDialog.dismiss();
                    ShowDialog("no");
                }
                }else {
                    Constants.showToast(getActivity(), getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }




    private class GetMPRDetails extends AsyncTask<String, Void, String>
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
                String trans_date = encryptDecrypt.encrypt(arg0[3]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));
                nameValuePairs.add(new BasicNameValuePair("forDate", trans_date));

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
                if(data != null){
                JSONArray transaction = new JSONArray(data);
                JSONObject object1 = transaction.getJSONObject(0);

                JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                JSONObject obj = rowResponse.getJSONObject(0);
                String result = obj.optString("result");

                result = encryptDecryptRegister.decrypt(result);
                if(result.equals("Success"))
                {
                    JSONObject object = transaction.getJSONObject(1);
                    JSONArray transactionBetDates = object.getJSONArray("getMPRDetailsForDate");
                    for (int i = 0; i < transactionBetDates.length(); i++) {

                        JSONObject object2 = transactionBetDates.getJSONObject(i);
                        String GrossAmt = object2.optString("GrossAmt");
                        String MDR = object2.optString("MDR");
                        String serviceTax = object2.optString("serviceTax");
                        String HOLD_VALUE = object2.optString("HOLD_VALUE");
                        String ADJUSTMENTS = object2.optString("ADJUSTMENTS");
                        String CASH_AT_POS = object2.optString("CASH_AT_POS");
                        String NETAMT = object2.optString("NETAMT");
                        String transDate = object2.optString("transDate");
                        String tDate = object2.optString("tDate");
                        String TOTALTXNS = object2.optString("TOTALTXNS");

                        transDate = encryptDecrypt.decrypt(transDate);
                        transDate = transDate.split("\\s+")[0];

                        txtGrossAmount.setText(encryptDecrypt.decrypt(GrossAmt));
                        txtMDR.setText(encryptDecrypt.decrypt(MDR));
                        txtServiceTax.setText(encryptDecrypt.decrypt(serviceTax));
                        txtHoldAmount.setText(encryptDecrypt.decrypt(HOLD_VALUE));
                        txtAdjustments.setText(encryptDecrypt.decrypt(ADJUSTMENTS));
                        txtCashPos.setText(encryptDecrypt.decrypt(CASH_AT_POS));
                        txtNoOfTxn.setText(encryptDecrypt.decrypt(TOTALTXNS));
                        txtPaymentDate.setText(transDate);
                        txtTotalValue.setText(encryptDecrypt.decrypt(NETAMT));
                        txtNetAmount.setText(encryptDecrypt.decrypt(NETAMT));

                    }
                    progressDialog.dismiss();

                }
                else {
                    progressDialog.dismiss();
                    Constants.showToast(getActivity(),getString(R.string.no_internet));
                }
                }else {
                    Constants.showToast(getActivity(), getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();

            }

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


        }
    }


    private void ShowDialog(String val)
    {
        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_for_email_success);
        dialog.setCancelable(false);

        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);
        TextView textMessage = (TextView) dialog.findViewById(R.id.text);
        TextView textEmailId = (TextView) dialog.findViewById(R.id.txtEmailID);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref,Context.MODE_PRIVATE);
        String email = preferences.getString("MerchantEmail","");
        textEmailId.setText(email);

        if(val.equalsIgnoreCase("no"))
        {
            textEmailId.setVisibility(View.GONE);
            textMessage.setText(getResources().getString(R.string.email_sent_failed));
            txtConfirm.setText("OK");
        }
        // if button is clicked, close the custom dialog
        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                getActivity().onBackPressed();
            }
        });

        dialog.show();
    }


}
