package com.axismerchant.fragments.reports;


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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.mis_reports.Activity_FilterMIS;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.CustomListAdapterForMPR;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.classes.MIS_MPR;
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
public class Fragment_for_MPR extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    public static final String ARG_OBJECT = "object";
    public static int flag = 0;
    public static View viewDetailsLayout;
    public ParallaxListView listData;
    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    BarChart layoutChart;
    String MOBILE, MID;
    MIS_MPR mis_mpr;
    ArrayList<MIS_MPR> mprDataSet;
    CustomListAdapterForMPR adapter;
    ImageView imgFilter;
    TextView txtMessage, txtDateDuration, txtGraphType;
    int type;
    double screenInches;
    private String date, mGraphType = "Transactions", currentDateAndTime, mDuration = "Daily";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.parallax_scroll_for_report,container,false);

        getInitialize(view);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));

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

            SharedPreferences pref = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
            boolean mCoach = pref.getBoolean("MPRCoach", true);
            if(mCoach)
            {
                int[] coachMarks = {R.drawable.reports_01, R.drawable.reports_02};
                Constants.onCoachMark(getActivity(), coachMarks);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("MPRCoach",false);
                editor.apply();
            }

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

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH:mm");
        String currentDateandTime = sdf.format(new Date());
        Log.e("current Date and time", currentDateandTime + "hrs");
        txtDate.setText(currentDateandTime + "hrs");

        txtMID.setText(Constants.MERCHANT_ID);

        listData = (ParallaxListView) view.findViewById(R.id.list_view);
        viewDetailsLayout = view.findViewById(R.id.lyDetailsLayout);
        imgFilter = (ImageView) view.findViewById(R.id.imgFilter);

        txtMessage = (TextView) view.findViewById(R.id.txtMessage);

        imgFilter.setOnClickListener(this);
        txtMessage.setVisibility(View.GONE);

        encryptDecryptRegister =  new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();
        mprDataSet = new ArrayList<>();
    }

    private void getChartData(String type) {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetTransactions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getTransactionBetDates", MID, MOBILE,type, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetTransactions().execute(Constants.DEMO_SERVICE+"getTransactionBetDates", MID, MOBILE,type, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }

    }


    private void getFilteredData(String reportType, String duration, String criteria) {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetFilteredData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"filterMPRTransactions", MID, MOBILE,duration,criteria,reportType, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetFilteredData().execute(Constants.DEMO_SERVICE+"filterMPRTransactions", MID, MOBILE,duration,criteria,reportType, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (type == 0) {
                Fragment_MPRDetails mprDetails = new Fragment_MPRDetails();
                Bundle bundle = new Bundle();
                bundle.putString("Date", mprDataSet.get(position - 1).gettDate());
                mprDetails.setArguments(bundle);
                getFragmentManager().beginTransaction().add(R.id.layout_container, mprDetails).addToBackStack("mprDetails").commit();
            }
        }catch (Exception e)
        {}
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
                String reportType = "unsettled";
                mDuration = data.getStringExtra("Duration");
                String reportCriteria = data.getStringExtra("Criteria");

                getFilteredData(reportType, mDuration, reportCriteria);
            }
        }
    }

    public void showBarChart() {
        if (layoutChart != null)
            layoutChart.removeAllViews();

        ArrayList<MIS_MPR> mprArrayList = new ArrayList<>();
        for (int i = mprDataSet.size() - 1; i >= 0; i--) {
            mprArrayList.add(mprDataSet.get(i));
        }

        date = "";
        date = date + mprArrayList.get(0).getTransDate() + " To " + mprArrayList.get(mprArrayList.size() - 1).getTransDate();
        txtDateDuration.setText(date);

        ArrayList<BarEntry> entries = new ArrayList<>();

        if (mGraphType.equalsIgnoreCase("Transactions")) {
            for (int i = 0; i < mprArrayList.size(); i++) {
                if (mprArrayList.get(i).getTransactions().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Integer.parseInt(mprArrayList.get(i).getTransactions()), i));
                }
            }
            txtGraphType.setText(getString(R.string.transaction));
        } else if (mGraphType.equalsIgnoreCase("Transaction Volume")) {
            for (int i = 0; i < mprArrayList.size(); i++) {
                if (mprArrayList.get(i).getTxnVolume().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Float.parseFloat(mprArrayList.get(i).getTxnVolume()), i));
                }
            }
            txtGraphType.setText(getString(R.string.xn_volume));
        } else {
            for (int i = 0; i < mprArrayList.size(); i++) {
                if (mprArrayList.get(i).getAvgTicketSize().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Float.parseFloat(mprArrayList.get(i).getAvgTicketSize()), i));
                }
            }
            txtGraphType.setText(getString(R.string.avg_ticket_size));
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

        adapter = new CustomListAdapterForMPR(getActivity(), mprDataSet, screenInches);
        adapter.notifyDataSetChanged();
        listData.setAdapter(adapter);

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

                            if(transDate.contains("-"))
                                transDate.replace("-","/");

                            if(mDuration.equalsIgnoreCase("Daily"))
                                transDate = transDate.split("\\s+")[0];

                            mis_mpr = new MIS_MPR(Transactions, AvgTicketSize, TxnVolume, transDate, tDate);
                            mprDataSet.add(mis_mpr);
                        }

                        showBarChart();
                        progressDialog.dismiss();
                        adapter = new CustomListAdapterForMPR(getActivity(), mprDataSet,screenInches);
                        listData.setAdapter(adapter);

                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(getActivity(), getString(R.string.session_expired));
                        logout();
                    } else {
                        txtMessage.setVisibility(View.VISIBLE);
                    }
                }
                progressDialog.dismiss();
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }

    public class MyValueFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Math.round(value)+"";
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.duration_analytics), duration));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.rptCriteria), criteria));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.trans_type), trans_type));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[7])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[8])));

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

                            if (transDate.contains("-"))
                                transDate.replace("-", "/");

                            if (mDuration.equalsIgnoreCase("Daily"))
                                transDate = transDate.split("\\s+")[0];

                            mis_mpr = new MIS_MPR(Transactions, AvgTicketSize, TxnVolume, transDate, tDate);
                            mprDataSet.add(mis_mpr);
                        }

                        txtDateDuration.setText(date);
                        showBarChart();
                        progressDialog.dismiss();
                        adapter = new CustomListAdapterForMPR(getActivity(), mprDataSet, screenInches);
                        listData.setAdapter(adapter);

                    } else if (result.equalsIgnoreCase("SessionFailure")) {
                        Constants.showToast(getActivity(), getString(R.string.session_expired));
                        logout();
                    } else {
                        progressDialog.dismiss();
                        txtMessage.setVisibility(View.VISIBLE);

                    }
                }
                progressDialog.dismiss();
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }

}
