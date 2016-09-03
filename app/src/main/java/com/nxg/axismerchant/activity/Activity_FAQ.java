package com.nxg.axismerchant.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;


public class Activity_FAQ extends AppCompatActivity implements View.OnClickListener {
    ListView listFAQ;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    FAQ faq;
    ArrayList<FAQ> faqArrayList;
    FAQAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

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

        if (Constants.isNetworkConnectionAvailable(Activity_FAQ.this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetFaq().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getFAQs");
            } else {
                new GetFaq().execute(Constants.DEMO_SERVICE+"getFAQs");

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


    private class GetFaq extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_FAQ.this);
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
                HttpGet httpGet = new HttpGet(newURI);

                HttpResponse response = httpclient.execute(httpGet);
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
                e.printStackTrace();
            }

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
