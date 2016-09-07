package com.nxg.axismerchant.activity.qr_pay;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.classes.QRTransactions;
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

public class Activity_QRAllTransaction extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    EncryptDecrypt  encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    String MID, MOBILE, mVisa;
    ArrayList<QRTransactions> qrTransactionsList;
    QRTransactions qrTransactions;
    ListView listQRTransactions;
    SetQRAdapter qrAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_all_transaction);

        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        listQRTransactions = (ListView) findViewById(R.id.listQRTransactions);

        imgBack.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
        imgProfile.setOnClickListener(this);

        listQRTransactions.setOnItemClickListener(this);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        qrTransactionsList = new ArrayList<>();

        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        SharedPreferences pref = getSharedPreferences(Constants.ProfileInfo,MODE_PRIVATE);
        mVisa = pref.getString("mvisaId","");

        if (Constants.isNetworkConnectionAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetQRPayData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getlatestMvisaTransactions", MID, MOBILE, mVisa);
            } else {
                new GetQRPayData().execute(Constants.DEMO_SERVICE + "getlatestMvisaTransactions", MID, MOBILE, mVisa);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;
        }
    }

    @Override
    protected void onResume() {
        TextView txtNotification = (TextView) findViewById(R.id.txtNotificationCount);

        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notificationArrayList = Constants.retrieveFromDatabase(this, dbHelper);
        if(notificationArrayList.size() > 0)
        {
            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setText(String.valueOf(notificationArrayList.size()));
        }else
        {
            txtNotification.setVisibility(View.GONE);
        }
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Intent intent = new Intent(this, Activity_QRTransactionDetails.class);
        intent.putExtra("XnId",qrTransactionsList.get(i).getId());
        startActivity(intent);
    }


    private class GetQRPayData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_QRAllTransaction.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mvisa_merchant_id), encryptDecrypt.encrypt(arg0[3])));
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
                        JSONArray getLatestMerchantUserTrans = object2.getJSONArray("getlatestMvisaTransactions");

                        for (int i = 0; i < getLatestMerchantUserTrans.length(); i++) {
                            JSONObject object1 = getLatestMerchantUserTrans.getJSONObject(i);
                            String mvisa_merchant_id = object1.optString("mvisa_merchant_id");
                            String onDate = object1.optString("onDate");
                            String txn_amount = object1.optString("txn_amount");
                            String ref_no = object1.optString("ref_no");
                            String id = object1.optString("id");

                            mvisa_merchant_id = encryptDecrypt.decrypt(mvisa_merchant_id);
                            onDate = encryptDecrypt.decrypt(onDate);
                            txn_amount = encryptDecrypt.decrypt(txn_amount);
                            ref_no = encryptDecrypt.decrypt(ref_no);
                            id = encryptDecrypt.decrypt(id);

                            if(onDate.contains("-"))
                                onDate.replace("-","/");
                            onDate = onDate.split("\\s+")[0];
                            qrTransactions = new QRTransactions(id,onDate,ref_no,mvisa_merchant_id,txn_amount);
                            qrTransactionsList.add(qrTransactions);
                        }

                        qrAdapter = new SetQRAdapter(Activity_QRAllTransaction.this, qrTransactionsList);
                        listQRTransactions.setAdapter(qrAdapter);

                    } else {
                        Constants.showToast(Activity_QRAllTransaction.this, getString(R.string.no_details));

                    }
                    progressDialog.dismiss();
                }else {
                    progressDialog.dismiss();
                    Constants.showToast(Activity_QRAllTransaction.this, getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                Constants.showToast(Activity_QRAllTransaction.this,getString(R.string.network_error));
            }

        }
    }


    private class SetQRAdapter extends BaseAdapter
    {

        Context context;
        ArrayList<QRTransactions> qrTransactionsList;
        public SetQRAdapter(Activity_QRAllTransaction activity_qrAllTransaction, ArrayList<QRTransactions> qrTransactionsList) {
            this.context = activity_qrAllTransaction;
            this.qrTransactionsList = qrTransactionsList;
        }

        @Override
        public int getCount() {
            return qrTransactionsList.size();
        }

        @Override
        public Object getItem(int i) {
            return qrTransactionsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_row_for_qr_pay_status,null);

            TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
            TextView txtmVisaID = (TextView) view.findViewById(R.id.txtmVisaID);
            TextView txtRRnNo = (TextView) view.findViewById(R.id.txtRRnNo);
            TextView txtAmount = (TextView) view.findViewById(R.id.txtAmount);

            txtDate.setText(qrTransactionsList.get(i).getOnDate());
            txtmVisaID.setText(qrTransactionsList.get(i).getMvisa_merchant_id());
            txtRRnNo.setText(qrTransactionsList.get(i).getRef_no());
            txtAmount.setText(qrTransactionsList.get(i).getTxn_amount());


            return view;
        }
    }

}
