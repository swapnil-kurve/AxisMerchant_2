package com.nxg.axismerchant.fragments.reports;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.nirhart.parallaxscroll.views.ParallaxListView;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.mis_reports.Activity_FilterMIS;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.CustomListAdapterForMPR;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.MIS_MPR;

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
public class Fragment_for_MPR extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    BarChart layoutChart;
    String MOBILE, MID;
    MIS_MPR mis_mpr;
    ArrayList<MIS_MPR> mprDataSet;
    private String date, mGraphType = "Transactions",currentDateAndTime,mDuration = "Daily";

    CustomListAdapterForMPR adapter;
    public ParallaxListView listData;
    public static final String ARG_OBJECT = "object";
    public static int flag = 0;
    public static View viewDetailsLayout;
    private View lyEmail, lyDetailsLayout, lyShowEmailButton;
    ImageView imgFilter;
    TextView txtFromDate, txtToDate;
    int DateFlag = 0,type;
    double screenInches;
    Calendar myCalendar = Calendar.getInstance();
    TextView txtGrossAmount,txtMDR,txtServiceTax,txtHoldAmount,txtAdjustments,txtCashPos, txtPaymentDate,txtNoOfTxn,txtTotalValue, txtNetAmount,txtDateDuration, txtGraphType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.parallax_scroll_for_report,container,false);

        getInitialize(view);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        encryptDecryptRegister =  new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();
        mprDataSet = new ArrayList<>();

        View v = inflater.inflate(R.layout.fragment_transaction_report,null);
        layoutChart = (BarChart) v.findViewById(R.id.chartTransaction);
        txtDateDuration = (TextView) v.findViewById(R.id.txtDateDuration);
        txtGraphType = (TextView) v.findViewById(R.id.txtLeftText);
        TextView txtLabel = (TextView) view.findViewById(R.id.txtLabel);

        listData.addParallaxedHeaderView(v);
        listData.setOnItemClickListener(this);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        currentDateAndTime = sdf.format(new Date());

        type  = 0;
        Bundle bundle = getArguments();
        if(bundle != null && bundle.containsKey(ARG_OBJECT))
        {
            type = bundle.getInt(ARG_OBJECT,0);
        }
        if(type == 0) {
            getChartData("settled");
            flag = 0;
            txtLabel.setVisibility(View.GONE);
        }
        else {

            getChartData("unsettled");
            imgFilter.setVisibility(View.VISIBLE);
            flag = 1;
            txtLabel.setVisibility(View.VISIBLE);
        }
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());
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

        listData = (ParallaxListView) view.findViewById(R.id.list_view);
        viewDetailsLayout = view.findViewById(R.id.lyDetailsLayout);
        imgFilter = (ImageView) view.findViewById(R.id.imgFilter);

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

        imgFilter.setOnClickListener(this);
        txtFromDate.setOnClickListener(this);
        txtToDate.setOnClickListener(this);
//        txtConfirm.setOnClickListener(this);
        lyShowEmailButton.setOnClickListener(this);
    }

    private void getChartData(String type) {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetTransactions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getTransactionBetDates", MID, MOBILE,type);
            } else {
                new GetTransactions().execute(Constants.DEMO_SERVICE+"getTransactionBetDates", MID, MOBILE,type);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }

    }


    private void getFilteredData(String reportType, String duration, String criteria) {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetFilteredData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"filterMPRTransactions", MID, MOBILE,duration,criteria,reportType);
            } else {
                new GetFilteredData().execute(Constants.DEMO_SERVICE+"filterMPRTransactions", MID, MOBILE,duration,criteria,reportType);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(type == 0) {
            Fragment_MPRDetails mprDetails = new Fragment_MPRDetails();
            Bundle bundle = new Bundle();
            bundle.putString("Date",mprDataSet.get(position-1).gettDate());
            mprDetails.setArguments(bundle);
            getFragmentManager().beginTransaction().add(R.id.layout_container,mprDetails).addToBackStack("mprDetails").commit();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.imgFilter:
                Intent intent=new Intent(getActivity(),Activity_FilterMIS.class);
                startActivityForResult(intent, 2);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {
            if(data != null) {
                mGraphType = data.getStringExtra("ReportType");
                String reportType = "unsettled";
                mDuration = data.getStringExtra("Duration");
                String reportCriteria = data.getStringExtra("Criteria");

                getFilteredData(reportType, mDuration, reportCriteria);
            }
        }
    }

    private class GetTransactions extends AsyncTask<String, Void, String>
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
            String str = "";
            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                String mID = encryptDecryptRegister.encrypt(arg0[1]);
                String mobile = encryptDecryptRegister.encrypt(arg0[2]);
                String trans_type = encryptDecrypt.encrypt(arg0[3]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));
                nameValuePairs.add(new BasicNameValuePair("TRANS_TYPE", trans_type));

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
                if(!data.equalsIgnoreCase("")) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates = object.getJSONArray("getTransactionBetDates");
                        for (int i = 0; i < transactionBetDates.length(); i++) {

                            JSONObject object2 = transactionBetDates.getJSONObject(i);
                            String Transactions = object2.optString("Transactions");
                            String AvgTicketSize = object2.optString("AvgTicketSize");
                            String TxnVolume = object2.optString("TxnVolume");
                            String transDate = object2.optString("transDate");
                            String tDate = object2.optString("tDate");

                            Transactions = encryptDecrypt.decrypt(Transactions);
                            AvgTicketSize = encryptDecrypt.decrypt(AvgTicketSize);
                            TxnVolume = encryptDecrypt.decrypt(TxnVolume);
                            transDate = encryptDecrypt.decrypt(transDate);
                            tDate = encryptDecrypt.decrypt(tDate);

                           /* if(transDate.contains("-"))
                                transDate.replace("-","/");

                            if(mDuration.equalsIgnoreCase("Daily"))
                                transDate = Constants.splitDate(transDate.split("\\s+")[0]);*/

                            mis_mpr = new MIS_MPR(Transactions, AvgTicketSize, TxnVolume, transDate, tDate);
                            mprDataSet.add(mis_mpr);
                        }

                        showBarChart();
                        progressDialog.dismiss();
                        adapter = new CustomListAdapterForMPR(getActivity(), mprDataSet,screenInches);
                        listData.setAdapter(adapter);

                    } else {
                        progressDialog.dismiss();
                        Constants.showToast(getActivity(), getString(R.string.no_details));

                    }
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }

        }
    }


    public void showBarChart()
    {
        if(layoutChart != null)
            layoutChart.removeAllViews();

        ArrayList<MIS_MPR> mprArrayList = new ArrayList<>();
        for (int i = mprDataSet.size()-1; i>=0 ; i--) {
            mprArrayList.add(mprDataSet.get(i));
        }

        date = "";
        date = date + mprArrayList.get(0).getTransDate()+" To "+mprArrayList.get(mprArrayList.size()-1).getTransDate();
        txtDateDuration.setText(date);

        ArrayList<BarEntry> entries = new ArrayList<>();

        if(mGraphType.equalsIgnoreCase("Transactions")) {
            for (int i = 0; i < mprArrayList.size(); i++) {
                if (mprArrayList.get(i).getTransactions().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Integer.parseInt(mprArrayList.get(i).getTransactions()), i));
                }
            }
            txtGraphType.setText("Transactions");
        }else if(mGraphType.equalsIgnoreCase("Transaction Volume"))
        {
            for (int i = 0; i < mprArrayList.size(); i++) {
                if (mprArrayList.get(i).getTxnVolume().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Float.parseFloat(mprArrayList.get(i).getTxnVolume()), i));
                }
            }
            txtGraphType.setText("Transaction Volume");
        }else
        {
            for (int i = 0; i < mprArrayList.size(); i++) {
                if (mprArrayList.get(i).getAvgTicketSize().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Float.parseFloat(mprArrayList.get(i).getAvgTicketSize()), i));
                }
            }
            txtGraphType.setText("Average Ticket Size");
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setBarSpacePercent(25f);

        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < mprArrayList.size(); i++) {
            labels.add(mprArrayList.get(i).gettDate());
        }

        BarChart chart = new BarChart(getActivity());
        BarData data = new BarData(labels, dataSet);
        data.setValueFormatter(new MyValueFormatter());

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(0);

        Legend l = chart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setForm(Legend.LegendForm.LINE);
        l.setFormSize(0f);
        l.setTextSize(0f);
        l.setXEntrySpace(0f);
        l.setExtra(ColorTemplate.COLORFUL_COLORS, new String[]{});

//        chart.animateY(700);
        chart.setDescription("");
        chart.setData(data);
        chart.setVisibleXRangeMaximum(7);
        chart.moveViewToX(mprArrayList.size());
        chart.getAxisRight().setDrawLabels(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);

        layoutChart.notifyDataSetChanged();
        layoutChart.addView(chart);

        adapter = new CustomListAdapterForMPR(getActivity(),mprDataSet,screenInches);
        adapter.notifyDataSetChanged();
        listData.setAdapter(adapter);

    }


    public class MyValueFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Math.round(value)+"";
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
                    adapter = new CustomListAdapterForMPR(getActivity(),mprDataSet, screenInches);
                    listData.setAdapter(adapter);

                }
                else {
                    progressDialog.dismiss();
                    Constants.showToast(getActivity(), getString(R.string.no_details));

                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }

        }
    }



    private class GetFilteredData extends AsyncTask<String, Void, String>
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
                String trans_type = encryptDecrypt.encrypt(arg0[5]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.duration), duration));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.rptCriteria), criteria));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.trans_type), trans_type));

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
                JSONArray transaction = new JSONArray(data);
                JSONObject object1 = transaction.getJSONObject(0);

                JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                JSONObject obj = rowResponse.getJSONObject(0);
                String result = obj.optString("result");

                result = encryptDecryptRegister.decrypt(result);
                if(result.equals("Success"))
                {
                    mprDataSet.clear();
                    JSONObject object = transaction.getJSONObject(1);
                    JSONArray transactionBetDates = object.getJSONArray("filterMPRTransactions");
                    for (int i = 0; i < transactionBetDates.length(); i++) {

                        JSONObject object2 = transactionBetDates.getJSONObject(i);
                        String Transactions = object2.optString("Transactions");
                        String AvgTicketSize = object2.optString("AvgTicketSize");
                        String TxnVolume = object2.optString("TxnVolume");
                        String transDate = object2.optString("transDate");
                        String tDate = object2.optString("tDate");

                        Transactions = encryptDecrypt.decrypt(Transactions);
                        AvgTicketSize = encryptDecrypt.decrypt(AvgTicketSize);
                        TxnVolume = encryptDecrypt.decrypt(TxnVolume);
                        transDate = encryptDecrypt.decrypt(transDate);
                        tDate = encryptDecrypt.decrypt(tDate);

                        /*if(transDate.contains("-"))
                            transDate.replace("-","/");

                        if(mDuration.equalsIgnoreCase("Daily"))
                            transDate = Constants.splitDate(transDate.split("\\s+")[0]);*/

                        mis_mpr = new MIS_MPR(Transactions,AvgTicketSize,TxnVolume,transDate,tDate);
                        mprDataSet.add(mis_mpr);
                    }

                    txtDateDuration.setText(date);
                    showBarChart();
                    progressDialog.dismiss();
                    adapter = new CustomListAdapterForMPR(getActivity(),mprDataSet, screenInches);
                    listData.setAdapter(adapter);

                }
                else {
                    progressDialog.dismiss();
                    Constants.showToast(getActivity(), getString(R.string.no_details));

                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
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
                    getActivity().onBackPressed();
                }
                else {
                    progressDialog.dismiss();
                    ShowDialog("no");
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
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
            e.printStackTrace();

        }
    }


    private void ShowDialog(String val)
    {
        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_for_email_success);

        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);
        TextView textMessage = (TextView) dialog.findViewById(R.id.text);
        TextView textEmailId = (TextView) dialog.findViewById(R.id.txtEmailID);
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

            }
        });

        dialog.show();
    }

}
