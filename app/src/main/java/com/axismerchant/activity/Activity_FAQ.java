package com.axismerchant.activity;


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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.activity.start.Activity_UserProfile;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.classes.Notification;
import com.axismerchant.custom.ProgressDialogue;
import com.axismerchant.database.DBHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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


public class Activity_FAQ extends AppCompatActivity implements View.OnClickListener {
    ListView listFAQ;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    FAQ faq;
    ArrayList<FAQ> faqArrayList;
    FAQAdapter adapter;
    String MID,MOBILE;
    ProgressDialogue progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        progressDialog = new ProgressDialogue();
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        listFAQ = (ListView) findViewById(R.id.listFAQ);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister =  new EncryptDecryptRegister();
        faqArrayList = new ArrayList<>();

        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgNotification.setOnClickListener(this);

        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        String user = encryptDecryptRegister.decrypt(preferences.getString("Username", ""));
        String LastLogin = encryptDecryptRegister.decrypt(preferences.getString("LastLogin", ""));
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));

        if (Constants.isNetworkConnectionAvailable(Activity_FAQ.this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetFaq().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getFAQs", MID, MOBILE, Constants.SecretKey, Constants.AuthToken,Constants.IMEI);
            } else {
                new GetFaq().execute(Constants.DEMO_SERVICE+"getFAQs", MID, MOBILE, Constants.SecretKey, Constants.AuthToken,Constants.IMEI);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.imgBack :
                onBackPressed();
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;
        }
    }

    @Override
    protected void onResume() {
        TextView txtNotification = (TextView) findViewById(R.id.txtNotificationCount);
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notificationArrayList = Constants.retrieveFromDatabase(this, dbHelper);

        if (notificationArrayList.size() > 0) {
            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setText(String.valueOf(notificationArrayList.size()));
        } else {
            txtNotification.setVisibility(View.GONE);
        }
        super.onResume();
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("KeepLoggedIn", "false");
        editor.apply();
        Intent intent = new Intent(this, Activity_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private class GetFaq extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.onCreateDialog(Activity_FAQ.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[5])));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                int stats = response.getStatusLine().getStatusCode();

                if (stats == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    str = data;
                }
            } catch (org.apache.http.ParseException e1) {
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
                if(!data.equals("")){
                JSONArray transaction = new JSONArray(data);
                JSONObject object1 = transaction.getJSONObject(0);

                JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                JSONObject obj = rowResponse.getJSONObject(0);
                String result = obj.optString("result");

                result = encryptDecryptRegister.decrypt(result);
                if(result.equals("Success"))
                {
                    JSONObject object = transaction.getJSONObject(1);
                    JSONArray transactionBetDates = object.getJSONArray("getFAQs");
                    for (int i = 0; i < transactionBetDates.length(); i++) {

                        JSONObject object2 = transactionBetDates.getJSONObject(i);
                        String que = object2.optString("faqQue");
                        String ans = object2.optString("faqAns");

                        que = encryptDecryptRegister.decrypt(que);
                        ans = encryptDecryptRegister.decrypt(ans);

                        faq = new FAQ(que,ans);
                        faqArrayList.add(faq);
                    }
                    progressDialog.dismiss();
                    adapter = new FAQAdapter(Activity_FAQ.this,faqArrayList);
                    listFAQ.setAdapter(adapter);

                }else if(result.equalsIgnoreCase("SessionFailure")){
                    Constants.showToast(Activity_FAQ.this, getString(R.string.session_expired));
                    logout();
                }
                else {
                    progressDialog.dismiss();
//                    Constants.showToast(Activity_FAQ.this, getString(R.string.no_internet));
                }
                }else {
                    Constants.showToast(Activity_FAQ.this, getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }

    private class FAQ {
        String que ,ans;

        public FAQ(String que, String ans) {
            this.que = que;
            this.ans = ans;
        }

        public String getQue() {
            return que;
        }

        public String getAns() {
            return ans;
        }
    }

    private class FAQAdapter extends BaseAdapter
    {
        private Context context;
        private ArrayList<FAQ> faqArrayList;
        public FAQAdapter(Activity_FAQ activity_faq, ArrayList<FAQ> faqArrayList) {
            this.context =  activity_faq;
            this.faqArrayList = faqArrayList;
        }

        @Override
        public int getCount() {
            return faqArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return faqArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater  inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_layout_for_faq, null);

            TextView txtQueNo = (TextView) view.findViewById(R.id.txtQueNo);
            TextView txtQue = (TextView) view.findViewById(R.id.txtQue);
            TextView txtAns = (TextView) view.findViewById(R.id.txtAns);

            txtQueNo.setText("Question "+(i+1));
            txtQue.setText(faqArrayList.get(i).getQue());
            txtAns.setText(faqArrayList.get(i).getAns());

            return view;
        }
    }


}
