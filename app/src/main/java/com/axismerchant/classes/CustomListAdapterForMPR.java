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

public class CustomListAdapterForMPR extends BaseAdapter {

    private LayoutInflater inflater;
    Context context;
    ArrayList<MIS_MPR> mprDataSet;
    double screenInches;

    public CustomListAdapterForMPR(FragmentActivity activity, ArrayList<MIS_MPR> mprDataSet, double screenInches) {
        this.context = activity;
        this.mprDataSet = mprDataSet;
        this.screenInches = screenInches;
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

        viewHolder viewHolder = new viewHolder();
        viewHolder.txtTransactions = (TextView) convertView.findViewById(R.id.txtTransactions);
        viewHolder.txtVolume = (TextView) convertView.findViewById(R.id.txtVolume);
        viewHolder.txtAvgTicketSize = (TextView) convertView.findViewById(R.id.txtTicketSize);
        viewHolder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);

        viewHolder.txtTransactions.setText(mprDataSet.get(position).getTransactions());
        viewHolder.txtVolume.setText(mprDataSet.get(position).getTxnVolume());
        viewHolder.txtAvgTicketSize.setText(mprDataSet.get(position).getAvgTicketSize());
        if(mprDataSet.get(position).gettDate().length() == 2) {
            viewHolder.txtDate.setText("Week "+mprDataSet.get(position).gettDate());
        }else
        {
            viewHolder.txtDate.setText(mprDataSet.get(position).getTransDate());
        }

        if(screenInches<= 6 && screenInches>= 5)
        {
            setSize(16,18,viewHolder);
        }
        else if(screenInches<= 5 && screenInches>= 4)
        {
            setSize(14,16, viewHolder);
        }
        else if(screenInches<= 4 && screenInches>= 3)
        {
            setSize(12,12, viewHolder);
        }

        return convertView;
    }


    private void setSize(int i, int i1, viewHolder viewHolder) {

        viewHolder.txtDate.setTextSize(i);
        viewHolder.txtTransactions.setTextSize(i);
        viewHolder.txtVolume.setTextSize(i);
        viewHolder.txtAvgTicketSize.setTextSize(i);
    }

    class viewHolder
    {
        TextView txtTransactions, txtVolume, txtAvgTicketSize, txtDate;
    }
}
