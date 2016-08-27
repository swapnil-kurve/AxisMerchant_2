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
public class Fragment_RefundTransactions extends Fragment {

    public static final String ARG_OBJECT = "object";
    ViewPager viewPager;
    private String[] tabs ;
    String MID, MOBILE;
    ArrayList<SMSPayStatus> statusArrayList;
    SMSPayStatus smsPayStatus;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    PagerSlidingTabStrip tabsStrip;
    Typeface typeFace;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refund_transactions, container, false);

        statusArrayList = new ArrayList<>();

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);

        getTransactionData();

        
        return view;
    }


   /* @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());
    }*/

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
            PageFragment_for_refundXn refundXn = new PageFragment_for_refundXn();
            Bundle bundle = new Bundle();
            bundle.putInt(PageFragment_for_refundXn.ARG_OBJECT, position);
            bundle.putSerializable("StatusArrayList",statusArrayList);
            refundXn.setArguments(bundle);
            return refundXn;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabs[position];
        }
    }


    private void getTransactionData()
    {
        String mVisaId = "";
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.ProfileInfo,Context.MODE_PRIVATE);
        if(preferences.contains("mvisaId")) {
            mVisaId = preferences.getString("mvisaId", "");
        }
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetTransactions().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getRefundTransactions", MID, MOBILE, mVisaId);
            } else {
                new GetTransactions().execute(Constants.DEMO_SERVICE + "getRefundTransactions", MID, MOBILE, mVisaId);

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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mvisaID), encryptDecryptRegister.encrypt(arg0[3])));
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
                        JSONArray getLatestMerchantUserTrans = object2.getJSONArray("getRefundTransactions");

                        for (int i = 0; i < getLatestMerchantUserTrans.length(); i++) {
                            JSONObject object1 = getLatestMerchantUserTrans.getJSONObject(i);
                            String transactionId = object1.optString("transactionId");
                            String custMobile = object1.optString("custMobile");
                            String transAmt = object1.optString("transAmt");
                            String remark = object1.optString("remark");
                            String transStatus = object1.optString("transStatus");
                            String Ttype = object1.optString("Ttype");
                            String transDate = object1.optString("transDate");

                            transactionId = encryptDecrypt.decrypt(transactionId);
                            custMobile = encryptDecrypt.decrypt(custMobile);
                            transAmt = encryptDecrypt.decrypt(transAmt);
                            remark = encryptDecrypt.decrypt(remark);
                            transStatus = encryptDecrypt.decrypt(transStatus);
                            Ttype = encryptDecrypt.decrypt(Ttype);
                            transDate = encryptDecrypt.decrypt(transDate);

                            if(transDate.contains("-"))
                                transDate.replace("-","/");

                            transDate = transDate.split("\\s+")[0];
                            smsPayStatus = new SMSPayStatus("",custMobile, transAmt, transactionId, transStatus, remark, Ttype,transDate);
                            statusArrayList.add(smsPayStatus);
                        }

//                        dataAdapter = new DataAdapter(getActivity(), statusArrayList);
//                        listData.setAdapter(dataAdapter);

                        tabs = getResources().getStringArray(R.array.all_xn_report_mis);
                        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Futura_LightBold.ttf");

                        viewPager.setOffscreenPageLimit(1);
                        viewPager.setAdapter(new SampleFragmentPagerAdapter(getChildFragmentManager()));

                        tabsStrip.setTypeface(typeFace,0);

                        // Attach the view pager to the tab strip
                        tabsStrip.setViewPager(viewPager);
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


}
