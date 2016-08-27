package com.nxg.axismerchant.fragments.reports;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.SMSPayStatus;

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
public class PageFragment_for_refundXn extends Fragment {

    public static final String ARG_OBJECT = "object";
    String MID, MOBILE;
    ListView listData;

    ArrayList<SMSPayStatus> statusArrayList;
    SMSPayStatus smsPayStatus;
    DataAdapter dataAdapter;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    private String pageTitle;
    int pageNO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_for_refund_xn, container, false);
        getInitialize(view);

        Bundle bundle = getArguments();

        if(bundle != null && bundle.containsKey("StatusArrayList")){
            statusArrayList = (ArrayList<SMSPayStatus>) bundle.getSerializable("StatusArrayList");
        }

        pageNO = bundle.getInt(ARG_OBJECT);
        TextView txtLabel = (TextView) view.findViewById(R.id.txtLabel);
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
        ArrayList<SMSPayStatus> payStatuses = new ArrayList<>();
        if(pageNO == 0)
        {
            dataAdapter = new DataAdapter(getActivity(),statusArrayList);
            listData.setAdapter(dataAdapter);
            dataAdapter.notifyDataSetChanged();
        }else if(pageNO == 1) {
            pageTitle = "SMS";
            for (int i = 0; i < statusArrayList.size(); i++) {
                if(statusArrayList.get(i).getIsRefund().equalsIgnoreCase("SMS")){
                    payStatuses.add(statusArrayList.get(i));
                }
            }
            dataAdapter = new DataAdapter(getActivity(),payStatuses);
            listData.setAdapter(dataAdapter);
            dataAdapter.notifyDataSetChanged();
        }
        else if(pageNO == 2) {
            pageTitle = "QR";
            for (int i = 0; i < statusArrayList.size(); i++) {
                if(statusArrayList.get(i).getIsRefund().equalsIgnoreCase("QR")){
                    payStatuses.add(statusArrayList.get(i));
                }
            }
            dataAdapter = new DataAdapter(getActivity(),payStatuses);
            listData.setAdapter(dataAdapter);
            dataAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    private void getInitialize(View view){
        listData = (ListView) view.findViewById(R.id.listTransactions);

        statusArrayList = new ArrayList<>();

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();
    }


    private class DataAdapter extends BaseAdapter {
        ArrayList<SMSPayStatus> statusArrayList;
        Context context;

        public DataAdapter(Context context, ArrayList<SMSPayStatus> statusArrayList) {
            this.context = context;
            this.statusArrayList = statusArrayList;
        }

        @Override
        public int getCount() {
            return statusArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return statusArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_row_for_sms_pay_status, null);

            TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
            TextView txtXnID = (TextView) view.findViewById(R.id.txtTransactionID);
            TextView txtStatus = (TextView) view.findViewById(R.id.txtStatus);
            TextView txtAmount = (TextView) view.findViewById(R.id.txtAmount);
            TextView txtMobile = (TextView) view.findViewById(R.id.txtMobileNo);
            TextView txtRemark = (TextView) view.findViewById(R.id.txtRemark);
            ImageView imgStatusSMS = (ImageView) view.findViewById(R.id.imgStatusSMS);
            View lyRefundLayout = view.findViewById(R.id.refundLayout);

            txtMobile.setText(statusArrayList.get(position).getCustMobile());
            txtAmount.setText(getResources().getString(R.string.Rs)+statusArrayList.get(position).getAmount());
            txtStatus.setText(statusArrayList.get(position).getStatus());
//            txtDate.setText(Constants.splitDate(statusArrayList.get(position).getTransDate()));
            txtDate.setText(statusArrayList.get(position).getTransDate());
            txtXnID.setText(statusArrayList.get(position).getInvoiceNum());
            txtRemark.setText(statusArrayList.get(position).getRemark());
            txtRemark.setVisibility(View.VISIBLE);

            if(statusArrayList.get(position).getStatus().equals("Pending")) {
                imgStatusSMS.setImageResource(R.mipmap.pending);
                txtStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            }
            else if(statusArrayList.get(position).getStatus().equals("Success")) {
                imgStatusSMS.setImageResource(R.mipmap.successfull);
                txtStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                lyRefundLayout.setVisibility(View.GONE);
            }
            else{
                imgStatusSMS.setImageResource(R.mipmap.fail);
                txtStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            return view;
        }
    }


}
