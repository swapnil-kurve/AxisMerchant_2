package com.axismerchant.fragments;


import android.content.Context;
import android.content.Intent;
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

import com.axismerchant.R;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.custom.ProgressDialogue;

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
    int pos;
    TextView txtText;
    View viewButtonLayout;
    ProgressDialogue progressDialog;
    private String mPromotionId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_for_offer_features, container, false);

        progressDialog = new ProgressDialogue();
        viewButtonLayout = getActivity().findViewById(R.id.buttonLayout);
        txtText = (TextView) view.findViewById(R.id.txtText);

        encryptDecryptRegister = new EncryptDecryptRegister();
        encryptDecrypt = new EncryptDecrypt();

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));
        Constants.retrieveMPINFromDatabase(getActivity());
        Constants.getIMEI(getActivity());

        Bundle bundle = getArguments();
        if(bundle != null)
        {
            mPromotionId = bundle.getString("PromotionId");
            pos = Integer.parseInt(bundle.getString("Position"));
        }


        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetPromotionsByID().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getPromotionById", MID, MOBILE,mPromotionId, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);//arrTitle[pageNO]);
            } else {
                new GetPromotionsByID().execute(Constants.DEMO_SERVICE + "getPromotionById", MID, MOBILE,mPromotionId, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);//arrTitle[pageNO]);

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

    /**
     * To Get Promotion details on the basis of Promotion ID
     */
    private class GetPromotionsByID extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.onCreateDialog(getActivity());
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[6])));

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

                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(getActivity(), getString(R.string.session_expired));
                        logout();
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
