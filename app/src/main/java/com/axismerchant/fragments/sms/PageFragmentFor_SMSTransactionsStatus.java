package com.axismerchant.fragments.sms;


import android.app.ProgressDialog;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.sms.Activity_TransactionStatusDetails;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.classes.SMSPayStatus;

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
public class PageFragmentFor_SMSTransactionsStatus extends Fragment implements AdapterView.OnItemClickListener {

    public static final String ARG_OBJECT = "object";
    ListView listTransactions;
    TextView txtMessage;
    ArrayList<SMSPayStatus> statusArrayList;
    SMSPayStatus smsPayStatus;
    DataAdapter dataAdapter;
    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    int pageNO;
    boolean loadingMore = false;
    private String[] arrTitle = {"All", "Pending", "Success", "Failed"};
    private String pageTitle, MID, MOBILE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.page_fragment_for_transactions, container, false);
        Bundle args = getArguments();

        getInitialize(view);

        pageNO = args.getInt(ARG_OBJECT, 0);


        pageTitle = arrTitle[pageNO];


        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());

        grabURL("0");

        listTransactions.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                int lastInScreen = firstVisibleItem + visibleItemCount;
                if (statusArrayList.size() > 0) {
                    if ((lastInScreen == totalItemCount) && !(loadingMore)) {
                        grabURL(statusArrayList.get(statusArrayList.size() - 1).getInvoiceNum());
                    }
                }
            }
        });

        return view;

    }

    private void grabURL(String transactionId) {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetEPayData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getLatestMerchantUserTransWithId", MID, MOBILE, pageTitle, Constants.SecretKey, Constants.AuthToken, Constants.IMEI, transactionId);//arrTitle[pageNO]);
            } else {
                new GetEPayData().execute(Constants.DEMO_SERVICE + "getLatestMerchantUserTransWithId", MID, MOBILE, pageTitle, Constants.SecretKey, Constants.AuthToken, Constants.IMEI, transactionId);//arrTitle[pageNO]);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void getInitialize(View view) {
        listTransactions = (ListView) view.findViewById(R.id.listTransactions);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        statusArrayList = new ArrayList<>();

        listTransactions.setOnItemClickListener(this);

        txtMessage = (TextView) view.findViewById(R.id.txtMessage);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.listTransactions) {
            Intent intent = new Intent(getActivity(), Activity_TransactionStatusDetails.class);
            intent.putExtra("XnID", statusArrayList.get(position).getInvoiceNum());
            startActivity(intent);
        }
    }


/*
    private void retrieveFromDatabase() {
        dbHelper = new DBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor crs;
        if(pageNO == 0) {
            crs = db.rawQuery("select DISTINCT " + DBHelper.UID + "," + DBHelper.CUST_MOBILE + ","
                    + DBHelper.AMOUNT + "," + DBHelper.REMARK + ","
                    + DBHelper.INVOICE_NO + "," + DBHelper.STATUS + "," + DBHelper.TRANS_DATE+ "," + DBHelper.IS_REFUND+ " from " + DBHelper.TABLE_NAME_E_PAYMENT +" order by CAST("+DBHelper.INVOICE_NO+" AS Integer) desc", null);
        }else {
            crs = db.rawQuery("select DISTINCT " + DBHelper.UID + "," + DBHelper.CUST_MOBILE + ","
                    + DBHelper.AMOUNT + "," + DBHelper.REMARK + ","
                    + DBHelper.INVOICE_NO + "," + DBHelper.STATUS + "," + DBHelper.TRANS_DATE+"," + DBHelper.IS_REFUND+ " from " + DBHelper.TABLE_NAME_E_PAYMENT+
                    " where " + DBHelper.STATUS+" = ?"+" order by CAST("+DBHelper.INVOICE_NO+" AS Integer) desc", new String[] {pageTitle});
        }

        statusArrayList.clear();
        while (crs.moveToNext()) {
            String mUID = crs.getString(crs.getColumnIndex(DBHelper.UID));
            String mCustMobile = crs.getString(crs.getColumnIndex(DBHelper.CUST_MOBILE));
            String mAmount = crs.getString(crs.getColumnIndex(DBHelper.AMOUNT));
            String mRemark = crs.getString(crs.getColumnIndex(DBHelper.REMARK));
            String mInvoiceNumber = crs.getString(crs.getColumnIndex(DBHelper.INVOICE_NO));
            String mStatus = crs.getString(crs.getColumnIndex(DBHelper.STATUS));
            String mTransDate = crs.getString(crs.getColumnIndex(DBHelper.TRANS_DATE));
            String mIsRefund = crs.getString(crs.getColumnIndex(DBHelper.IS_REFUND));

            smsPayStatus = new SMSPayStatus(mUID,mCustMobile, mAmount, mInvoiceNumber, mStatus, mRemark,mIsRefund,mTransDate);
            statusArrayList.add(smsPayStatus);
        }

        dataAdapter = new DataAdapter(getActivity(), statusArrayList);
        listTransactions.setAdapter(dataAdapter);

        dataAdapter.notifyDataSetChanged();
    }
*/

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
            ImageView imgStatusSMS = (ImageView) view.findViewById(R.id.imgStatusSMS);
            View lyRefundLayout = view.findViewById(R.id.refundLayout);

            try {
                if(statusArrayList.get(position).getCustMobile().length()>10){
                    String mob = statusArrayList.get(position).getCustMobile();
                    txtMobile.setText(mob.substring(mob.length()-10,mob.length()));
                }else {
                    txtMobile.setText(statusArrayList.get(position).getCustMobile());
                }
                txtAmount.setText(getResources().getString(R.string.Rs) + statusArrayList.get(position).getAmount());
                txtStatus.setText(statusArrayList.get(position).getStatus());
                txtDate.setText(statusArrayList.get(position).getTransDate().split("\\s+")[0]);
//                txtDate.setText(statusArrayList.get(position).getIsRefund());
                txtXnID.setText(statusArrayList.get(position).getInvoiceNum());

                if (statusArrayList.get(position).getStatus().equals("Pending")) {
                    imgStatusSMS.setImageResource(R.mipmap.pending);
                    txtStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                } else if (statusArrayList.get(position).getStatus().equals("Success")) {
                    imgStatusSMS.setImageResource(R.mipmap.successfull);
                    txtStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                    if(statusArrayList.get(position).getIsRefund().equalsIgnoreCase("1"))
                        lyRefundLayout.setVisibility(View.GONE);
                    else
                        lyRefundLayout.setVisibility(View.VISIBLE);

                } else {
                    imgStatusSMS.setImageResource(R.mipmap.fail);
                    txtStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }

            }catch (Exception e)
            {

            }
            return view;
        }
    }

    private class GetEPayData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingMore = true;
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.transactionId), encryptDecrypt.encrypt(arg0[7])));

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
                if(!data.equals("")) {
                    JSONArray jsonArray = new JSONArray(data);
                    JSONObject object = jsonArray.getJSONObject(0);
                    JSONArray rowsResponse = object.getJSONArray("rowsResponse");
                    JSONObject obj = rowsResponse.getJSONObject(0);
                    String result = obj.optString("result");
                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object2 = jsonArray.getJSONObject(1);
                        JSONArray getLatestMerchantUserTrans = object2.getJSONArray("getLatestMerchantUserTransWithId");

                        for (int i = 0; i < getLatestMerchantUserTrans.length(); i++) {
                            JSONObject object1 = getLatestMerchantUserTrans.getJSONObject(i);
                            String transactionId = object1.optString("transactionId");
                            String custMobile = object1.optString("custMobile");
                            String transAmt = object1.optString("transAmt");
                            String remark = object1.optString("remark");
                            String transStatus = object1.optString("transStatus");
                            String transDate = object1.optString("transDate");
                            String isRefund = object1.optString("isRefund");

                            transactionId = encryptDecrypt.decrypt(transactionId);
                            custMobile = encryptDecrypt.decrypt(custMobile);
                            transAmt = encryptDecrypt.decrypt(transAmt);
                            remark = encryptDecrypt.decrypt(remark);
                            transStatus = encryptDecrypt.decrypt(transStatus);
                            transDate = encryptDecrypt.decrypt(transDate);
                            isRefund = encryptDecrypt.decrypt(isRefund);

                            if(transDate.contains("-"))
                                transDate.replace("-","/");
                            transDate = transDate.split("\\s+")[0];

                            /*transDate = Constants.splitDate(transDate.split("\\s+")[0]);*/

                            smsPayStatus = new SMSPayStatus(custMobile, transAmt, transactionId, transStatus, remark,isRefund,transDate);
                            statusArrayList.add(smsPayStatus);
                        }

                        dataAdapter = new DataAdapter(getActivity(), statusArrayList);
                        listTransactions.setAdapter(dataAdapter);

                        dataAdapter.notifyDataSetChanged();
                        loadingMore = false;

                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(getActivity(), getString(R.string.session_expired));
                        logout();
                    } else {
//                        Constants.showToast(Activity_SMSPayHome.this, "No details found.");

                    }
                    progressDialog.dismiss();
                }else {
                    progressDialog.dismiss();
                    Constants.showToast(getActivity(), getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                Constants.showToast(getActivity(),getString(R.string.network_error));
            }

        }
    }
}
