package com.nxg.axismerchant.classes;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nxg.axismerchant.R;

import java.util.ArrayList;

public class CustomListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    Context context;
    ArrayList<SMSXnSummary> smsXnSummaries;

    public CustomListAdapter(FragmentActivity activity, ArrayList<SMSXnSummary> smsXnSummaries) {
        this.context = activity;
        this.smsXnSummaries = smsXnSummaries;
    }

    @Override
    public int getCount() {
        return smsXnSummaries.size();
    }

    @Override
    public Object getItem(int position) {
        return smsXnSummaries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        LinearLayout textView = (LinearLayout) convertView;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.custom_row_for_sms_report, null);

        TextView txtTransactions = (TextView) convertView.findViewById(R.id.txtTransactions);
        TextView txtVolume = (TextView) convertView.findViewById(R.id.txtVolume);
        TextView txtAvgTicketSize = (TextView) convertView.findViewById(R.id.txtTicketSize);
        TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);

        txtTransactions.setText(smsXnSummaries.get(position).getNoOfTrans());
        txtVolume.setText(smsXnSummaries.get(position).getVolume());
        txtAvgTicketSize.setText(smsXnSummaries.get(position).getTicketSize());
        txtDate.setText(smsXnSummaries.get(position).getTransdate());

        return convertView;
    }
}
