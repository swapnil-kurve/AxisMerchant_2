package com.axismerchant.classes;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.axismerchant.R;

import java.util.ArrayList;

public class CustomListAdapterForMerchantLikeMe extends BaseAdapter {

    private LayoutInflater inflater;
    Context context;
    ArrayList<MerchantLikeMe> likeMeArrayList;

    public CustomListAdapterForMerchantLikeMe(FragmentActivity activity, ArrayList<MerchantLikeMe> likeMeArrayList) {
        this.context = activity;
        this.likeMeArrayList = likeMeArrayList;
    }

    @Override
    public int getCount() {
        return likeMeArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return likeMeArrayList.get(position);
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

        txtTransactions.setText(likeMeArrayList.get(position).getNoOfTxn());
        txtVolume.setText(likeMeArrayList.get(position).getTxnVol());
        txtAvgTicketSize.setText(likeMeArrayList.get(position).getAvgTicketSize());
        txtDate.setText(likeMeArrayList.get(position).getmHead());

        return convertView;
    }
}
