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

public class CustomListAdapterForMPR extends BaseAdapter {

    private LayoutInflater inflater;
    Context context;
    ArrayList<MIS_MPR> mprDataSet;

    public CustomListAdapterForMPR(FragmentActivity activity, ArrayList<MIS_MPR> mprDataSet) {
        this.context = activity;
        this.mprDataSet = mprDataSet;
    }

    @Override
    public int getCount() {
        return mprDataSet.size();
    }

    @Override
    public Object getItem(int position) {
        return mprDataSet.get(position);
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

        txtTransactions.setText(mprDataSet.get(position).getTransactions());
        txtVolume.setText(mprDataSet.get(position).getTxnVolume());
        txtAvgTicketSize.setText(mprDataSet.get(position).getAvgTicketSize());
        if(mprDataSet.get(position).gettDate().length() == 2) {
            txtDate.setText("Week "+mprDataSet.get(position).gettDate());
        }else
        {
            txtDate.setText(mprDataSet.get(position).getTransDate());
        }
        return convertView;
    }
}
