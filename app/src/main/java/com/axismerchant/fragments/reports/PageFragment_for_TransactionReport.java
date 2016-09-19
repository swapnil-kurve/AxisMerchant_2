package com.axismerchant.fragments.reports;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.axismerchant.R;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.TransactionReport;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageFragment_for_TransactionReport extends Fragment {

    public static final String ARG_OBJECT = "object";
    ParallaxListView listData;

    BarChart layoutChart;
    TextView txtDateDuration;
    private String date;
    int pageNO;
    ArrayList<TransactionReport> transactionReports;
    TransactionReportAdapter transactionReportAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.parallax_scroll_for_report, container, false);
        getInitialize(view);

        TextView txtLabel = (TextView) view.findViewById(R.id.txtLabel);

        View v = inflater.inflate(R.layout.fragment_for_transaction,null);

        layoutChart = (BarChart) v.findViewById(R.id.chartVolume);
        txtDateDuration = (TextView) v.findViewById(R.id.txtDateDuration);

        listData.addParallaxedHeaderView(v);

        txtDateDuration.setText(date);

        Bundle bundle = getArguments();

        if(bundle != null && bundle.containsKey("TransactionArrayList"))
        {
            transactionReports = (ArrayList<TransactionReport>) bundle.getSerializable("TransactionArrayList");
        }
        pageNO = bundle.getInt(ARG_OBJECT);
        if(pageNO == 0)
        {
            txtLabel.setVisibility(View.VISIBLE);
        }else{
            txtLabel.setVisibility(View.GONE);
        }


        return view;
    }

    @Override
    public void onResume() {
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());

        ArrayList<TransactionReport> reportArrayList = new ArrayList<>();
        if(pageNO == 0)
        {
            transactionReportAdapter = new TransactionReportAdapter(getActivity(),transactionReports);
            listData.setAdapter(transactionReportAdapter);
            transactionReportAdapter.notifyDataSetChanged();

            showBarChart(transactionReports);
        }else if(pageNO == 1) {
            for (int i = 0; i < transactionReports.size(); i++) {
                if(transactionReports.get(i).gettType().equalsIgnoreCase("SMS")){
                    reportArrayList.add(transactionReports.get(i));
                }
            }
            transactionReportAdapter = new TransactionReportAdapter(getActivity(),reportArrayList);
            listData.setAdapter(transactionReportAdapter);
            transactionReportAdapter.notifyDataSetChanged();

            showBarChart(reportArrayList);
        }
        else if(pageNO == 2) {
            for (int i = 0; i < transactionReports.size(); i++) {
                if(transactionReports.get(i).gettType().equalsIgnoreCase("QR")){
                    reportArrayList.add(transactionReports.get(i));
                }
            }
            transactionReportAdapter = new TransactionReportAdapter(getActivity(),reportArrayList);
            listData.setAdapter(transactionReportAdapter);
            transactionReportAdapter.notifyDataSetChanged();

            showBarChart(reportArrayList);
        }
        super.onResume();
    }

    private void getInitialize(View view) {
        listData = (ParallaxListView) view.findViewById(R.id.list_view);

        transactionReports = new ArrayList<>();
    }





    public void showBarChart(ArrayList<TransactionReport> reportArrayList)
    {
        layoutChart.clear();

        if(reportArrayList.size() > 0) {
            ArrayList<TransactionReport> xnArrayList = new ArrayList<>();
            for (int i = reportArrayList.size() - 1; i >= 0; i--) {
                xnArrayList.add(reportArrayList.get(i));
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

            transactionReportAdapter = new TransactionReportAdapter(getActivity(), reportArrayList);
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


}
