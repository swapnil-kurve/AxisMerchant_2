package com.nxg.axismerchant.fragments.analytics;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.nirhart.parallaxscroll.views.ParallaxListView;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.mis_reports.Activity_FilterMIS;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.CustomListAdapterForMPR;
import com.nxg.axismerchant.classes.CustomListAdapterForMerchantLikeMe;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.MIS_MPR;
import com.nxg.axismerchant.classes.MerchantLikeMe;

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
public class PageFragment_TransactionAnalytics extends Fragment implements View.OnClickListener {

    public static final String ARG_OBJECT = "object";
    String MOBILE, MID, mGraphType = "Transactions";
    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    public ParallaxListView listData;
    MerchantLikeMe merchantLikeMe;
    ArrayList<MerchantLikeMe> likeMeArrayList;
    MIS_MPR mis_mpr;
    ArrayList<MIS_MPR> analyticsArrayList;
    String date;
    CustomListAdapterForMerchantLikeMe adapter;
    CustomListAdapterForMPR adapterAnalytics;
    BarChart layoutChart;
    TextView txtDateDuration,txtGraphType, txtXn, txtVol, txtTicket;
    View lyTop, lyInfo, lyTopMessages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_transaction_analytics, container, false);

        ImageView imgFilter = (ImageView) view.findViewById(R.id.imgFilter);
        listData = (ParallaxListView) view.findViewById(R.id.list_view);
        lyTop = view.findViewById(R.id.lyTop);
        lyInfo = view.findViewById(R.id.lyInfo);

        View v = inflater.inflate(R.layout.fragment_transaction_report,null);
        layoutChart = (BarChart) v.findViewById(R.id.chartTransaction);
        txtDateDuration = (TextView) v.findViewById(R.id.txtDateDuration);
        txtGraphType = (TextView) v.findViewById(R.id.txtLeftText);
        lyTopMessages = v.findViewById(R.id.lyTopMessages);

        txtXn = (TextView) v.findViewById(R.id.txtXn);
        txtVol = (TextView) v.findViewById(R.id.txtVol);
        txtTicket = (TextView) v.findViewById(R.id.txtTicket);

        listData.addParallaxedHeaderView(v);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        encryptDecryptRegister =  new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();
        likeMeArrayList = new ArrayList<>();
        analyticsArrayList = new ArrayList<>();

        imgFilter.setOnClickListener(this);

        int position  = 0;
        Bundle bundle = getArguments();
        if(bundle != null && bundle.containsKey(ARG_OBJECT))
        {
            position = bundle.getInt(ARG_OBJECT,0);
        }

        if(position == 0) {
            getAnalyticsData();
            txtGraphType.setVisibility(View.VISIBLE);
            lyTopMessages.setVisibility(View.VISIBLE);

            txtXn.setVisibility(View.GONE);
            txtVol.setVisibility(View.GONE);
            txtTicket.setVisibility(View.GONE);
        } else {
            getMerchantData();
            imgFilter.setVisibility(View.GONE);
            txtGraphType.setVisibility(View.GONE);

            txtXn.setVisibility(View.VISIBLE);
            txtVol.setVisibility(View.VISIBLE);
            txtTicket.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Constants.retrieveMPINFromDatabase(getActivity());
        Constants.getIMEI(getActivity());

    }

    private void getMerchantData() {

        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetMerchantData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getMerchantLikeMe", MID, MOBILE);
            } else {
                new GetMerchantData().execute(Constants.DEMO_SERVICE+"getMerchantLikeMe", MID, MOBILE);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }

    }

    private void getGraphData() {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetMerchantGraphData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getMerchantLikeMedataForGraph", MID, MOBILE);
            } else {
                new GetMerchantGraphData().execute(Constants.DEMO_SERVICE+"getMerchantLikeMedataForGraph", MID, MOBILE);

            }
        } else {
            Constants.showToast(getActivity(),getString(R.string.no_internet));
        }

    }

    private void getAnalyticsData() {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetAnalyticsData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getDataForAnalytics", MID, MOBILE);
            } else {
                new GetAnalyticsData().execute(Constants.DEMO_SERVICE+"getDataForAnalytics", MID, MOBILE);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }
    }

    private void getAnalyticsDataForFilter(String duration, String reportCriteria) {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetAnalyticsDataForFilter().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"filterDataForAnalytics", MID, MOBILE, duration, reportCriteria);
            } else {
                new GetAnalyticsDataForFilter().execute(Constants.DEMO_SERVICE+"filterDataForAnalytics", MID, MOBILE, duration, reportCriteria);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
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
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {
            if(data != null) {
                mGraphType = data.getStringExtra("ReportType");
                String duration = data.getStringExtra("Duration");
                String reportCriteria = data.getStringExtra("Criteria");

                getAnalyticsDataForFilter(duration, reportCriteria);
            }
        }
    }


    private class GetMerchantData extends AsyncTask<String, Void, String>
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

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));

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
                    JSONArray transactionBetDates = object.getJSONArray("getMerchantLikeMe");
                    for (int i = 0; i < transactionBetDates.length(); i++) {

                        JSONObject object2 = transactionBetDates.getJSONObject(i);
                        String noOfTxn = object2.optString("noOfTxn");
                        String avgTicketSize = object2.optString("avgTicketSize");
                        String txnVol = object2.optString("txnVol");
                        String mer_id = object2.optString("mer_id");
                        String mHead = object2.optString("mHead");

                        noOfTxn = encryptDecrypt.decrypt(noOfTxn);
                        avgTicketSize = encryptDecrypt.decrypt(avgTicketSize);
                        txnVol = encryptDecrypt.decrypt(txnVol);
                        mer_id = encryptDecrypt.decrypt(mer_id);
                        mHead = encryptDecrypt.decrypt(mHead);

                        merchantLikeMe = new MerchantLikeMe(noOfTxn,txnVol,avgTicketSize,mer_id,mHead);
                        likeMeArrayList.add(merchantLikeMe);
                    }


                    getGraphData();
                    progressDialog.dismiss();
                    adapter = new CustomListAdapterForMerchantLikeMe(getActivity(),likeMeArrayList);
                    listData.setAdapter(adapter);

                }
                else {
                    lyTop.setVisibility(View.GONE);
                    lyInfo.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();

                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }

        }
    }



    private class GetMerchantGraphData extends AsyncTask<String, Void, String>
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

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));

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
                    JSONArray transactionBetDates = object.getJSONArray("getMerchantLikeMedataForGraph");

                    JSONObject object2 = transactionBetDates.getJSONObject(0);
                    String noOfTxnofAllMerchant = object2.optString("noOfTxnofAllMerchant");
                    String txnVolofAllMerchant = object2.optString("txnVolofAllMerchant");
                    String avgTicketSizeofAllMerchant = object2.optString("avgTicketSizeofAllMerchant");
                    String noOfTxnofMerchant = object2.optString("noOfTxnofMerchant");
                    String txnVolofMerchant = object2.optString("txnVolofMerchant");
                    String avgTicketSizeofMerchant = object2.optString("avgTicketSizeofMerchant");

                    noOfTxnofAllMerchant = encryptDecrypt.decrypt(noOfTxnofAllMerchant);
                    txnVolofAllMerchant = encryptDecrypt.decrypt(txnVolofAllMerchant);
                    avgTicketSizeofAllMerchant = encryptDecrypt.decrypt(avgTicketSizeofAllMerchant);
                    noOfTxnofMerchant = encryptDecrypt.decrypt(noOfTxnofMerchant);
                    txnVolofMerchant = encryptDecrypt.decrypt(txnVolofMerchant);
                    avgTicketSizeofMerchant = encryptDecrypt.decrypt(avgTicketSizeofMerchant);

                    showBarChart(noOfTxnofAllMerchant,txnVolofAllMerchant,avgTicketSizeofAllMerchant,noOfTxnofMerchant,txnVolofMerchant,avgTicketSizeofMerchant);
                    progressDialog.dismiss();
                    adapter = new CustomListAdapterForMerchantLikeMe(getActivity(),likeMeArrayList);
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



    public void showBarChart(String noOfTxnofAllMerchant, String txnVolofAllMerchant, String avgTicketSizeofAllMerchant, String noOfTxnofMerchant, String txnVolofMerchant, String avgTicketSizeofMerchant)
    {
        layoutChart.clear();
        HorizontalBarChart chart = new HorizontalBarChart(getActivity());

        ArrayList<BarEntry> yValues1 = new ArrayList<>();
        BarEntry v1e11 = new BarEntry(Float.parseFloat(noOfTxnofMerchant), 2); // Jan
        yValues1.add(v1e11);
        BarEntry v1e12 = new BarEntry(Float.parseFloat(txnVolofMerchant), 1); // Feb
        yValues1.add(v1e12);
        BarEntry v1e13 = new BarEntry(Float.parseFloat(avgTicketSizeofMerchant), 0); // Mar
        yValues1.add(v1e13);

        ArrayList<BarEntry> yValues2 = new ArrayList<>();
        BarEntry v1e21 = new BarEntry(Float.parseFloat(noOfTxnofAllMerchant), 2); // Jan
        yValues2.add(v1e21);
        BarEntry v1e22 = new BarEntry(Float.parseFloat(txnVolofAllMerchant), 1); // Feb
        yValues2.add(v1e22);
        BarEntry v1e23 = new BarEntry(Float.parseFloat(avgTicketSizeofAllMerchant), 0); // Mar
        yValues2.add(v1e23);

        BarDataSet barDataSet1 = new BarDataSet(yValues1, "Avg. of Merchant");
        barDataSet1.setColor(getResources().getColor(R.color.green_float));
        BarDataSet barDataSet2 = new BarDataSet(yValues2, "You");
        barDataSet2.setColor(getResources().getColor(R.color.colorPrimary));

        barDataSet1.setBarSpacePercent(0f);
        barDataSet2.setBarSpacePercent(0f);


        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(barDataSet1);
        dataSets.add(barDataSet2);


        ArrayList<String> xValues = new ArrayList<>();
        xValues.add("");
        xValues.add("");
        xValues.add("");

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);

        BarData data = new BarData(xValues, dataSets);
        data.setValueTextSize(6);
        data.setValueTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/futura_std_medium.otf"));
        data.setValueFormatter(new MyValueFormatter());
        data.setGroupSpace(500f);

        data.setGroupSpace(500f);
        data.setGroupSpace(500f);

        chart.setData(data);
        chart.getXAxis().setTextSize(6);// hides horizontal grid lines inside chart
        YAxis leftAxis = chart.getAxisLeft();
        chart.getAxisRight().setEnabled(false); // hides horizontal grid lines with below line
        leftAxis.setEnabled(false); // hides vertical grid lines  inside chart
        chart.invalidate();
        chart.setClickable(false);
        chart.setDescription("");    // Hide the description
        chart.getLegend().setEnabled(true);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setPinchZoom(false);
        chart.notifyDataSetChanged();
        chart.setScaleEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);

        leftAxis.setDrawLabels(true);
        layoutChart.addView(chart);

        adapter = new CustomListAdapterForMerchantLikeMe(getActivity(),likeMeArrayList);
        adapter.notifyDataSetChanged();
        listData.setAdapter(adapter);

    }




    private class GetAnalyticsData extends AsyncTask<String, Void, String>
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

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));

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
            String date = "";
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
                    JSONArray transactionBetDates = object.getJSONArray("getDataForAnalytics");
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

                        if(transDate.contains("/"))
                        transDate = Constants.splitDate(transDate.split("\\s+")[0]);

                        mis_mpr = new MIS_MPR(Transactions,AvgTicketSize,TxnVolume,transDate,tDate);
                        analyticsArrayList.add(mis_mpr);
                    }


                    txtDateDuration.setText(date);
                    showBarChart();
                    progressDialog.dismiss();
                    adapterAnalytics = new CustomListAdapterForMPR(getActivity(),analyticsArrayList);
                    listData.setAdapter(adapterAnalytics);

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

    /**
     * Modify Here Analytics Graph
     */
    int[] clor = new int[1];
    public void showBarChart()
    {
        if(layoutChart != null)
        layoutChart.removeAllViews();

        ArrayList<Entry> entries = new ArrayList<>();
        LineChart chart = new LineChart(getActivity());

        ArrayList<MIS_MPR> arrayAnalytics = new ArrayList<>();
        for (int i = analyticsArrayList.size()-1; i>=0 ; i--) {
            arrayAnalytics.add(analyticsArrayList.get(i));
        }

        date = "";
        date = date + arrayAnalytics.get(0).getTransDate()+" To "+arrayAnalytics.get(arrayAnalytics.size()-1).getTransDate();
        txtDateDuration.setText(date);

        if(mGraphType.equalsIgnoreCase("Transactions")) {
            for (int i = 0; i < arrayAnalytics.size(); i++) {
                if (arrayAnalytics.get(i).getTransactions().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Integer.parseInt(arrayAnalytics.get(i).getTransactions()), i));
                }
            }
            txtGraphType.setText("Transactions");
        }else if(mGraphType.equalsIgnoreCase("Transaction Volume"))
        {
            for (int i = 0; i < arrayAnalytics.size(); i++) {
                if (arrayAnalytics.get(i).getTxnVolume().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Float.parseFloat(arrayAnalytics.get(i).getTxnVolume()), i));
                }
            }
            txtGraphType.setText("Transaction Volume");
        }else
        {
            for (int i = 0; i < arrayAnalytics.size(); i++) {
                if (arrayAnalytics.get(i).getAvgTicketSize().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Float.parseFloat(arrayAnalytics.get(i).getAvgTicketSize()), i));
                }
            }
            txtGraphType.setText("Average Ticket Size");
        }
        LineDataSet dataSet = new LineDataSet(entries, "");
        clor[0] = getResources().getColor(R.color.colorPrimary);
        dataSet.setColors(clor);

        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < arrayAnalytics.size(); i++) {
            labels.add(arrayAnalytics.get(i).gettDate());
        }

        LineData data = new LineData(labels, dataSet);
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

        chart.getAxisRight().setEnabled(false);
        chart.setDescription("");
        chart.setData(data);
        chart.setVisibleXRangeMaximum(7);
        chart.moveViewToX(arrayAnalytics.size());
        chart.setDoubleTapToZoomEnabled(false);
        chart.setPinchZoom(false);
        chart.notifyDataSetChanged();
        chart.setScaleEnabled(false);

        layoutChart.addView(chart);

        adapterAnalytics = new CustomListAdapterForMPR(getActivity(),analyticsArrayList);
        adapterAnalytics.notifyDataSetChanged();
        listData.setAdapter(adapter);
    }


    public class MyValueFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Math.round(value)+"";
        }
    }


    private class GetAnalyticsDataForFilter extends AsyncTask<String, Void, String>
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.duration_analytics), duration));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.rptCriteria), criteria));

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
                    analyticsArrayList.clear();
                    txtDateDuration.setText("");
                    String date = "";
                    JSONObject object = transaction.getJSONObject(1);
                    JSONArray transactionBetDates = object.getJSONArray("filterDataForAnalytics");
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

                        mis_mpr = new MIS_MPR(Transactions,AvgTicketSize,TxnVolume,transDate,tDate);
                        analyticsArrayList.add(mis_mpr);
                    }

                    txtDateDuration.setText(date);
                    showBarChart();
                    progressDialog.dismiss();
                    adapterAnalytics = new CustomListAdapterForMPR(getActivity(),analyticsArrayList);
                    listData.setAdapter(adapterAnalytics);

                }
                else {
                    progressDialog.dismiss();
                    Constants.showToast(getActivity(), getString(R.string.no_internet));

                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }

        }
    }



}
