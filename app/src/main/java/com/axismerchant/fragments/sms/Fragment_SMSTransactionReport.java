package com.axismerchant.fragments.sms;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.CustomListAdapter;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.classes.SMSXnSummary;
import com.axismerchant.custom.ProgressDialogue;
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
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_SMSTransactionReport extends Fragment{

    public static String ARG_OBJECT = "object";
    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    BarChart layoutChartXn, layoutChartVolume;
    SMSXnSummary smsXnSummary;
    ArrayList<SMSXnSummary> smsXnSummaries;
    TextView txtGraphType, txtDateDuration;
    CustomListAdapter adapter;
    ParallaxListView listData;
    String date;
    ProgressDialogue progressDialog;
    private int type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.parallax_scroll_for_report, container, false);
        Bundle args = getArguments();

        progressDialog = new ProgressDialogue();
        listData = (ParallaxListView) view.findViewById(R.id.list_view);

        encryptDecryptRegister =  new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();
        smsXnSummaries = new ArrayList<>();

        View v = inflater.inflate(R.layout.fragment_transaction_report,null);
        layoutChartXn = (BarChart) v.findViewById(R.id.chartTransaction);
        layoutChartVolume = (BarChart) v.findViewById(R.id.chartVolume);
        txtGraphType = (TextView) v.findViewById(R.id.txtLeftText);
        txtDateDuration = (TextView) v.findViewById(R.id.txtDateDuration);

        listData.addParallaxedHeaderView(v);

        type = args.getInt(ARG_OBJECT, 0);

        getChartData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());
    }

    private void getChartData() {
        String MOBILE, MID;
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));

        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetTransactions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getlatestSMSTransSummary", MID, MOBILE, "Success", Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetTransactions().execute(Constants.DEMO_SERVICE+"getlatestSMSTransSummary", MID, MOBILE, "Success", Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }

    }

   public void showBarChartXn()
    {
        layoutChartXn.clear();
        layoutChartVolume.setVisibility(View.GONE);
        layoutChartXn.setVisibility(View.VISIBLE);
        txtGraphType.setText(getString(R.string.transaction));
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        ArrayList<SMSXnSummary> xnSummaries = new ArrayList<>();
        for (int i = smsXnSummaries.size()-1; i>=0 ; i--) {
            xnSummaries.add(smsXnSummaries.get(i));
        }

        date = "";
        date = date + xnSummaries.get(0).getTransdate()+" To "+xnSummaries.get(xnSummaries.size()-1).getTransdate();
        txtDateDuration.setText(date);

        for (int i = 0; i < xnSummaries.size(); i++) {
            if (xnSummaries.get(i).getNoOfTrans().equals("")) {
                entries.add(new BarEntry(0, i));
            } else {
                entries.add(new BarEntry(Integer.parseInt(xnSummaries.get(i).getNoOfTrans()), i));
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setBarSpacePercent(25f);

        for (int i = 0; i < xnSummaries.size(); i++) {
            labels.add(xnSummaries.get(i).getFd()+xnSummaries.get(i).getFmonth());
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
        chart.moveViewToX(xnSummaries.size());
        chart.getAxisRight().setDrawLabels(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);

        layoutChartXn.addView(chart);

        adapter = new CustomListAdapter(getActivity(),smsXnSummaries);
        adapter.notifyDataSetChanged();
        listData.setAdapter(adapter);

    }

    public void showBarChartVolume()
    {
        layoutChartVolume.clear();
        layoutChartVolume.setVisibility(View.VISIBLE);
        layoutChartXn.setVisibility(View.GONE);
        txtGraphType.setText(getString(R.string.amount));
        ArrayList<BarEntry> entries = new ArrayList<>();

        ArrayList<SMSXnSummary> xnSummaries = new ArrayList<>();
        for (int i = smsXnSummaries.size()-1; i>=0 ; i--) {
            xnSummaries.add(smsXnSummaries.get(i));
        }

        date = "";
        date = date + xnSummaries.get(0).getTransdate()+" To "+xnSummaries.get(xnSummaries.size()-1).getTransdate();
        txtDateDuration.setText(date);

        for (int i = 0; i < xnSummaries.size(); i++) {
            if (xnSummaries.get(i).getVolume().equals("")) {
                entries.add(new BarEntry(0, i));
            } else {
                entries.add(new BarEntry(Float.parseFloat(xnSummaries.get(i).getVolume()), i));
            }
        }


        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setBarSpacePercent(25f);

        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < xnSummaries.size(); i++) {
            labels.add(xnSummaries.get(i).getFd()+xnSummaries.get(i).getFmonth());
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
        chart.moveViewToX(xnSummaries.size());
        chart.getAxisRight().setDrawLabels(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);

        layoutChartVolume.addView(chart);

        adapter = new CustomListAdapter(getActivity(),smsXnSummaries);
        adapter.notifyDataSetChanged();
        listData.setAdapter(adapter);

    }

    private void logout()
    {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("KeepLoggedIn", "false");
        editor.apply();
        Intent intent = new Intent(getActivity(), Activity_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private class GetTransactions extends AsyncTask<String, Void, String> {

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
                String fDate = encryptDecryptRegister.encrypt(arg0[3]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.transStatus), fDate));
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

            try {
                if (data != null) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates = object.getJSONArray("getlatestSMSTransSummary");
                        for (int i = 0; i < transactionBetDates.length(); i++) {

                            JSONObject object2 = transactionBetDates.getJSONObject(i);
                            String volume = object2.optString("volume");
                            String ticketSize = object2.optString("ticketSize");
                            String noOfTrans = object2.optString("noOfTrans");
                            String transdate = object2.optString("transdate");
                            String fd = object2.optString("fd");
                            String fday = object2.optString("fday");
                            String fmonth = object2.optString("fmonth");
                            String fyear = object2.optString("fyear");
                            String fulldate = object2.optString("fulldate");
                            String transstatus = object2.optString("transstatus");

                            volume = encryptDecrypt.decrypt(volume);
                            ticketSize = encryptDecrypt.decrypt(ticketSize);
                            noOfTrans = encryptDecrypt.decrypt(noOfTrans);
                            transdate = encryptDecrypt.decrypt(transdate);
                            fd = encryptDecrypt.decrypt(fd);
                            fday = encryptDecrypt.decrypt(fday);
                            fmonth = encryptDecrypt.decrypt(fmonth);
                            fyear = encryptDecrypt.decrypt(fyear);
                            fulldate = encryptDecrypt.decrypt(fulldate);
                            transstatus = encryptDecrypt.decrypt(transstatus);

                            if (transdate.contains("-"))
                                transdate.replace("-", "/");

                            transdate = transdate.split("\\s+")[0];

                            /*transdate = Constants.splitDate(transdate.split("\\s+")[0]);*/

                            smsXnSummary = new SMSXnSummary(volume, ticketSize, noOfTrans, transdate, fd, fday, fmonth, fyear, fulldate, transstatus);
                            smsXnSummaries.add(smsXnSummary);
                        }

                        if (type == 1) {
                            showBarChartXn();
                        } else {
                            showBarChartVolume();
                        }

                        adapter = new CustomListAdapter(getActivity(), smsXnSummaries);
                        listData.setAdapter(adapter);
                        progressDialog.dismiss();

                    } else if (result.equalsIgnoreCase("SessionFailure")) {
                        Constants.showToast(getActivity(), getString(R.string.session_expired));
                        logout();
                    } else {
                        progressDialog.dismiss();
                        Constants.showToast(getActivity(), getString(R.string.no_details));

                    }
                } else {
                    Constants.showToast(getActivity(), getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                Constants.showToast(getActivity(), getString(R.string.network_error));
            }

        }
    }

    public class MyValueFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Math.round(value) + "";
        }
    }

}
