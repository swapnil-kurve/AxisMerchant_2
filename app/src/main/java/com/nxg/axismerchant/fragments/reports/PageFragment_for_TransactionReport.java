package com.nxg.axismerchant.fragments.reports;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.SMSPayStatus;
import com.nxg.axismerchant.classes.TransactionReport;
import com.nxg.axismerchant.database.DBHelper;

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
public class PageFragment_for_TransactionReport extends Fragment {

    public static final String ARG_OBJECT = "object";
    ParallaxListView listData;
    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    BarChart layoutChart;
    TextView txtDateDuration;
    String MOBILE, MID;
    private String date;
    int pageNO;
    DBHelper dbHelper;
    TransactionReport report;
    private String pageTitle;
    ArrayList<TransactionReport> transactionReports;
    TransactionReportAdapter transactionReportAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.parallax_scroll_for_report, container, false);
        getInitialize(view);

        encryptDecryptRegister =  new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();

        Bundle bundle = getArguments();
        pageNO = bundle.getInt(ARG_OBJECT);
        TextView txtLabel = (TextView) view.findViewById(R.id.txtLabel);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        View v = inflater.inflate(R.layout.fragment_for_transaction,null);

        layoutChart = (BarChart) v.findViewById(R.id.chartVolume);
        txtDateDuration = (TextView) v.findViewById(R.id.txtDateDuration);

        listData.addParallaxedHeaderView(v);



        if(pageNO == 0)
        {
            txtLabel.setVisibility(View.VISIBLE);

            dbHelper = new DBHelper(getActivity());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("delete from "+ DBHelper.TABLE_NAME_MIS_XN_REPORT);

            getChartData();
        }else{
            txtLabel.setVisibility(View.GONE);
            if(pageNO == 1)
                pageTitle = "SMS";
            else if(pageNO == 2)
                pageTitle = "QR";

            retrieveFromDatabase();
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
        listData = (ParallaxListView) view.findViewById(R.id.list_view);

        transactionReports = new ArrayList<>();
    }


    private void getChartData() {
        String mVisaId = "";
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.ProfileInfo,Context.MODE_PRIVATE);
        if(preferences.contains("mvisaId")) {
            mVisaId = preferences.getString("mvisaId", "");
        }

        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetTransactions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getTransDetailsforAll", MID, MOBILE, mVisaId, "All");
            } else {
                new GetTransactions().execute(Constants.DEMO_SERVICE+"getTransDetailsforAll", MID, MOBILE, mVisaId, "All");

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
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
            String str = null;
            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                String mID = encryptDecryptRegister.encrypt(arg0[1]);
                String mobile = encryptDecryptRegister.encrypt(arg0[2]);
                String mVisaId = encryptDecrypt.encrypt(arg0[3]);
                String mTType = encryptDecrypt.encrypt(arg0[4]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mvisaId), mVisaId));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.Ttype), mTType));

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
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates = object.getJSONArray("getTransDetailsforAll");
                        for (int i = 0; i < transactionBetDates.length(); i++) {

                            JSONObject object2 = transactionBetDates.getJSONObject(i);
                            String Totaltransaction = object2.optString("Totaltransaction");
                            String avgTicketSize = object2.optString("avgTicketSize");
                            String TxnVolume = object2.optString("TxnVolume");
                            String transDate = object2.optString("transDate");
                            String tDate = object2.optString("tDate");
                            String tType = object2.optString("tType");

                            Totaltransaction = encryptDecrypt.decrypt(Totaltransaction);
                            avgTicketSize = encryptDecrypt.decrypt(avgTicketSize);
                            TxnVolume = encryptDecrypt.decrypt(TxnVolume);
                            transDate = encryptDecrypt.decrypt(transDate);
                            tDate = encryptDecrypt.decrypt(tDate);
                            tType = encryptDecrypt.decrypt(tType);

                            if (transDate.contains("-"))
                                transDate.replace("-", "/");

                            transDate = transDate.split("\\s+")[0];
//                        report = new TransactionReport(Totaltransaction,transDate,TxnVolume,avgTicketSize,tDate,tType);
//                        transactionReports.add(report);
                            InsertIntoDatabase(Totaltransaction, avgTicketSize, TxnVolume, transDate, tDate, tType);
                        }
                        txtDateDuration.setText(date);
                        retrieveFromDatabase();

                        transactionReportAdapter = new TransactionReportAdapter(getActivity(), transactionReports);
                        listData.setAdapter(transactionReportAdapter);
                        progressDialog.dismiss();

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
        layoutChart.clear();

        if(transactionReports.size() > 0) {
            ArrayList<TransactionReport> xnArrayList = new ArrayList<>();
            for (int i = transactionReports.size() - 1; i >= 0; i--) {
                xnArrayList.add(transactionReports.get(i));
            }

            date = "";
            date = date + xnArrayList.get(0).getTransDate() + " To " + xnArrayList.get(xnArrayList.size() - 1).getTransDate();
            txtDateDuration.setText(date);

            ArrayList<BarEntry> entries = new ArrayList<>();

            for (int i = 0; i < xnArrayList.size(); i++) {
                if (xnArrayList.get(i).getTotaltransaction().equals("")) {
                    entries.add(new BarEntry(0, i));
                } else {
                    entries.add(new BarEntry(Integer.parseInt(xnArrayList.get(i).getTotaltransaction()), i));
                }
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            dataSet.setColor(getResources().getColor(R.color.colorPrimary));
            dataSet.setBarSpacePercent(25f);

            ArrayList<String> labels = new ArrayList<>();

            for (int i = 0; i < xnArrayList.size(); i++) {
                labels.add(xnArrayList.get(i).gettDate());
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

            chart.getAxisRight().setEnabled(false);
            chart.setDescription("");
            chart.setData(data);
            chart.setVisibleXRangeMaximum(7);
            chart.moveViewToX(xnArrayList.size());
            chart.setDoubleTapToZoomEnabled(false);
            chart.setPinchZoom(false);
            chart.setScaleEnabled(false);

            layoutChart.addView(chart);

            transactionReportAdapter = new TransactionReportAdapter(getActivity(), transactionReports);
            transactionReportAdapter.notifyDataSetChanged();
            listData.setAdapter(transactionReportAdapter);
        }
    }


    public class MyValueFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Math.round(value)+"";
        }
    }


    private class TransactionReportAdapter extends BaseAdapter
    {
        private LayoutInflater inflater;
        private Context context;
        private ArrayList<TransactionReport> transactionReports;

        public TransactionReportAdapter(FragmentActivity activity, ArrayList<TransactionReport> transactionReports) {
            this.context = activity;
            this.transactionReports = transactionReports;
        }

        @Override
        public int getCount() {
            return transactionReports.size();
        }

        @Override
        public Object getItem(int position) {
            return transactionReports.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_row_for_sms_report, null);

            TextView txtTransactions = (TextView) convertView.findViewById(R.id.txtTransactions);
            TextView txtVolume = (TextView) convertView.findViewById(R.id.txtVolume);
            TextView txtAvgTicketSize = (TextView) convertView.findViewById(R.id.txtTicketSize);
            TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);

            txtTransactions.setText(transactionReports.get(position).getTotaltransaction());
            txtVolume.setText(transactionReports.get(position).getTxnVolume());
            txtAvgTicketSize.setText(transactionReports.get(position).getAvgTicketSize());
            txtDate.setText(transactionReports.get(position).getTransDate());

            return convertView;
        }
    }


    private void InsertIntoDatabase(String totaltransaction, String avgTicketSize, String txnVolume, String transDate, String tDate, String tType) {
        dbHelper = new DBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.MIS_TR_TOTAL_Xn, totaltransaction);
        values.put(DBHelper.MIS_TR_AVG_TCKT_SIZE, avgTicketSize);
        values.put(DBHelper.MIS_TR_XN_VOLUME, txnVolume);
        values.put(DBHelper.MIS_TR_XN_DATE, transDate);
        values.put(DBHelper.MIS_TR_TDATE, tDate);
        values.put(DBHelper.MIS_TR_TTYPE, tType);

        long id = db.insert(DBHelper.TABLE_NAME_MIS_XN_REPORT, null, values);
        Log.v("id", String.valueOf(id));
    }


    private void retrieveFromDatabase() {
        dbHelper = new DBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor crs;
        if(pageNO == 0) {
            crs = db.rawQuery("select DISTINCT " + DBHelper.UID + "," + DBHelper.MIS_TR_TOTAL_Xn + ","
                    + DBHelper.MIS_TR_AVG_TCKT_SIZE + "," + DBHelper.MIS_TR_XN_VOLUME + ","
                    + DBHelper.MIS_TR_XN_DATE + "," + DBHelper.MIS_TR_TDATE + "," + DBHelper.MIS_TR_TTYPE+ " from " + DBHelper.TABLE_NAME_MIS_XN_REPORT , null);
        }else {
            crs = db.rawQuery("select DISTINCT " + DBHelper.UID + "," + DBHelper.MIS_TR_TOTAL_Xn + ","
                    + DBHelper.MIS_TR_AVG_TCKT_SIZE + "," + DBHelper.MIS_TR_XN_VOLUME + ","
                    + DBHelper.MIS_TR_XN_DATE + "," + DBHelper.MIS_TR_TDATE + "," + DBHelper.MIS_TR_TTYPE+ " from " + DBHelper.TABLE_NAME_MIS_XN_REPORT+
                    " where " + DBHelper.MIS_TR_TTYPE+" = ?", new String[] {pageTitle});
        }

        transactionReports.clear();
        while (crs.moveToNext()) {
            String mUID = crs.getString(crs.getColumnIndex(DBHelper.UID));
            String Totaltransaction = crs.getString(crs.getColumnIndex(DBHelper.MIS_TR_TOTAL_Xn));
            String avgTicketSize = crs.getString(crs.getColumnIndex(DBHelper.MIS_TR_AVG_TCKT_SIZE));
            String TxnVolume = crs.getString(crs.getColumnIndex(DBHelper.MIS_TR_XN_VOLUME));
            String transDate = crs.getString(crs.getColumnIndex(DBHelper.MIS_TR_XN_DATE));
            String tDate = crs.getString(crs.getColumnIndex(DBHelper.MIS_TR_TDATE));
            String tType = crs.getString(crs.getColumnIndex(DBHelper.MIS_TR_TTYPE));

            report = new TransactionReport(Totaltransaction,transDate,TxnVolume,avgTicketSize,tDate,tType);
            transactionReports.add(report);
        }

        transactionReportAdapter = new TransactionReportAdapter(getActivity(),transactionReports);
        listData.setAdapter(transactionReportAdapter);

        transactionReportAdapter.notifyDataSetChanged();

        showBarChart();
    }


}
