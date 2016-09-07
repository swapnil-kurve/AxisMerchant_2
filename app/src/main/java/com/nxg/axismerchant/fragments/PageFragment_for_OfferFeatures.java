package com.nxg.axismerchant.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;

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
public class PageFragment_for_OfferFeatures extends Fragment{

    public static final String ARG_OBJECT = "object";
    String MID,MOBILE;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    String pImgpath,promotionText,withOptions,terms,pHead,promoCode,offerValidity,promotype;

    private String mPromotionId;
    int pos;
    TextView txtText;
    View viewButtonLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_for_offer_features, container, false);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        Constants.retrieveMPINFromDatabase(getActivity());
        Constants.getIMEI(getActivity());

        Bundle bundle = getArguments();
        if(bundle != null)
        {
            mPromotionId = bundle.getString("PromotionId");
            pos = Integer.parseInt(bundle.getString("Position"));
        }
        
        viewButtonLayout = getActivity().findViewById(R.id.buttonLayout);
        txtText = (TextView) view.findViewById(R.id.txtText);

        encryptDecryptRegister = new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();


        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetPromotionsByID().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getPromotionById", MID, MOBILE,mPromotionId);//arrTitle[pageNO]);
            } else {
                new GetPromotionsByID().execute(Constants.DEMO_SERVICE + "getPromotionById", MID, MOBILE,mPromotionId);//arrTitle[pageNO]);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }





        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());
    }


    /**
     * To Get Promotion details on the basis of Promotion ID
     */
    private class GetPromotionsByID extends AsyncTask<String, Void, String> {

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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.promotionId), encryptDecrypt.encrypt(arg0[3])));

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
                        JSONArray getLatestMerchantUserTrans = object2.getJSONArray("getPromotionById");


                        for (int i = 0; i < getLatestMerchantUserTrans.length(); i++) {
                            JSONObject object1 = getLatestMerchantUserTrans.getJSONObject(i);
                            pImgpath = object1.optString("pImgpath");
                            promotionText = object1.optString("promotionText");
                            withOptions = object1.optString("withOptions");
                            terms = object1.optString("terms");
                            pHead = object1.optString("pHead");
                            promoCode = object1.optString("promoCode");
                            offerValidity = object1.optString("offerValidity");
                            promotype = object1.optString("promotype");


                            pImgpath = encryptDecrypt.decrypt(pImgpath);
                            promotionText = encryptDecrypt.decrypt(promotionText);
                            withOptions = encryptDecrypt.decrypt(withOptions);
                            terms = encryptDecrypt.decrypt(terms);
                            pHead = encryptDecrypt.decrypt(pHead);
                            promoCode = encryptDecrypt.decrypt(promoCode);
                            offerValidity = encryptDecrypt.decrypt(offerValidity);
                            promotype = encryptDecrypt.decrypt(promotype);

                        }

                        if(pos == 1)
                        {
                            txtText.setText(Html.fromHtml(terms));
                        }
                        if(pos == 0)
                        {
//                            txtText.setText(promotionText);
                            txtText.setText(Html.fromHtml(promotionText));
                        }

                        if(withOptions.equalsIgnoreCase("Yes"))
                        {
                            viewButtonLayout.setVisibility(View.VISIBLE);
                        }else
                        {
                            viewButtonLayout.setVisibility(View.GONE);
                        }

                    } else {
                        Constants.showToast(getActivity(), getString(R.string.no_details));

                    }
                    progressDialog.dismiss();
                }
                progressDialog.dismiss();
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }


}
