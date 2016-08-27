package com.nxg.axismerchant.fragments.reports;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.TransactionReport;

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
public class Fragment_for_TransactionReport extends Fragment {
    public static final String ARG_OBJECT = "object";
    ViewPager viewPager;
    private String[] tabs ;
    String MOBILE, MID;
    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    TransactionReport report;
    ArrayList<TransactionReport> transactionReports;
    Typeface typeFace;
    PagerSlidingTabStrip tabsStrip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions_status, container, false);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        encryptDecryptRegister =  new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();

        transactionReports = new ArrayList<>();

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        getChartData();



        return view; //fragment_page_for_transaction_report
    }


    public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

        public SampleFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public Fragment getItem(int position) {
            PageFragment_for_TransactionReport fragmentForTransaction = new PageFragment_for_TransactionReport();
            Bundle bundle = new Bundle();
            bundle.putInt(PageFragment_for_TransactionReport.ARG_OBJECT, position);
            bundle.putSerializable("TransactionArrayList",transactionReports);
            fragmentForTransaction.setArguments(bundle);
            return fragmentForTransaction;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabs[position];
        }
    }


    private void getChartData() {
        String mVisaId = "";
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.ProfileInfo,Context.MODE_PRIVATE);
        if(preferences.contains("mvisaId")) {
            mVisaId = preferences.getString("mvisaId", "");
        }

        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetTransactions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getTransDetailsforAll", MID, MOBILE, mVisaId, "All");
            } else {
                new GetTransactions().execute(Constants.DEMO_SERVICE+"getTransDetailsforAll", MID, MOBILE, mVisaId, "All");

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
            String str = null;
            try {
                HTTPUtils utils = new HTTPUtils();
                HttpClient httpclient = utils.getNewHttpClient(arg0[0].startsWith("https"));
                URI newURI = URI.create(arg0[0]);
                HttpPost httppost = new HttpPost(newURI);

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                String mID = encryptDecryptRegister.encrypt(arg0[1]);
                String mobile = encryptDecryptRegister.encrypt(arg0[2]);
                String mVisaId = encryptDecrypt.encrypt(arg0[3]);
                String mTType = encryptDecrypt.encrypt(arg0[4]);

                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), mID));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), mobile));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mvisaId), mVisaId));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.Ttype), mTType));

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
                if(data != null) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates = object.getJSONArray("getTransDetailsforAll");
                        for (int i = 0; i < transactionBetDates.length(); i++) {

                            JSONObject object2 = transactionBetDates.getJSONObject(i);
                            String Totaltransaction = object2.optString("Totaltransaction");
                            String avgTicketSize = object2.optString("avgTicketSize");
                            String TxnVolume = object2.optString("TxnVolume");
                            String transDate = object2.optString("transDate");
                            String tDate = object2.optString("tDate");
                            String tType = object2.optString("tType");

                            Totaltransaction = encryptDecrypt.decrypt(Totaltransaction);
                            avgTicketSize = encryptDecrypt.decrypt(avgTicketSize);
                            TxnVolume = encryptDecrypt.decrypt(TxnVolume);
                            transDate = encryptDecrypt.decrypt(transDate);
                            tDate = encryptDecrypt.decrypt(tDate);
                            tType = encryptDecrypt.decrypt(tType);

                            if (transDate.contains("-"))
                                transDate.replace("-", "/");

                            transDate = transDate.split("\\s+")[0];
                            report = new TransactionReport(Totaltransaction,transDate,TxnVolume,avgTicketSize,tDate,tType);
                            transactionReports.add(report);
                        }

                        tabs = getResources().getStringArray(R.array.all_xn_report_mis);
                        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Futura_LightBold.ttf");

                        viewPager.setOffscreenPageLimit(1);
                        viewPager.setAdapter(new SampleFragmentPagerAdapter(getChildFragmentManager()));

                        tabsStrip.setTypeface(typeFace,0);
                        // Attach the view pager to the tab strip
                        tabsStrip.setViewPager(viewPager);

                        progressDialog.dismiss();

                    } else {
                        progressDialog.dismiss();
                        Constants.showToast(getActivity(), getString(R.string.no_details));

                    }
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }

        }
    }

}
