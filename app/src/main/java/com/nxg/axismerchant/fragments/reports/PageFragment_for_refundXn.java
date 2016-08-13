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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_for_refund_xn, container, false);
        getInitialize(view);

        Bundle bundle = getArguments();
        int pos = bundle.getInt(ARG_OBJECT);
        TextView txtLabel = (TextView) view.findViewById(R.id.txtLabel);
        if(pos == 0)
        {
            txtLabel.setVisibility(View.VISIBLE);
        }else{
            txtLabel.setVisibility(View.GONE);
        }

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());

        getTransactionData();

        return view;
    }

    private void getInitialize(View view){
        listData = (ListView) view.findViewById(R.id.listTransactions);

        statusArrayList = new ArrayList<>();

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();
    }


    private void getTransactionData()
    {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetTransactions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getLatestMerchantUserTrans", MID, MOBILE, "Refund");//arrTitle[pageNO]);
            } else {
                new GetTransactions().execute(Constants.DEMO_SERVICE + "getLatestMerchantUserTrans", MID, MOBILE, "Refund");//arrTitle[pageNO]);

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
            String str = "";
            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.transStatus), encryptDecryptRegister.encrypt(arg0[3])));
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
                if(!data.equals("")) {
                    JSONArray jsonArray = new JSONArray(data);
                    JSONObject object = jsonArray.getJSONObject(0);
                    JSONArray rowsResponse = object.getJSONArray("rowsResponse");
                    JSONObject obj = rowsResponse.getJSONObject(0);
                    String result = obj.optString("result");
                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object2 = jsonArray.getJSONObject(1);
                        JSONArray getLatestMerchantUserTrans = object2.getJSONArray("getLatestMerchantUserTrans");

                        for (int i = 0; i < getLatestMerchantUserTrans.length(); i++) {
                            JSONObject object1 = getLatestMerchantUserTrans.getJSONObject(i);
                            String transactionId = object1.optString("transactionId");
                            String custMobile = object1.optString("custMobile");
                            String transAmt = object1.optString("transAmt");
                            String remark = object1.optString("remark");
                            String transStatus = object1.optString("transStatus");
                            String isRefund = object1.optString("isRefund");
                            String transDate = object1.optString("transDate");

                            transactionId = encryptDecrypt.decrypt(transactionId);
                            custMobile = encryptDecrypt.decrypt(custMobile);
                            transAmt = encryptDecrypt.decrypt(transAmt);
                            remark = encryptDecrypt.decrypt(remark);
                            transStatus = encryptDecrypt.decrypt(transStatus);
                            isRefund = encryptDecrypt.decrypt(isRefund);
                            transDate = encryptDecrypt.decrypt(transDate);

                           if(transDate.contains("-"))
                                transDate.replace("-","/");

                            transDate = transDate.split("\\s+")[0];
                            smsPayStatus = new SMSPayStatus("",custMobile, transAmt, transactionId, transStatus, remark, isRefund,transDate);
                            statusArrayList.add(smsPayStatus);
                        }

                        dataAdapter = new DataAdapter(getActivity(), statusArrayList);
                        listData.setAdapter(dataAdapter);
                    } else {
                        Constants.showToast(getActivity(), getString(R.string.no_details));

                    }
                    progressDialog.dismiss();
                }else {
                    progressDialog.dismiss();
                    Constants.showToast(getActivity(), getString(R.string.network_error));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Constants.showToast(getActivity(),getString(R.string.network_error));
            }

        }
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
