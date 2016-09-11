package com.nxg.axismerchant.activity.qr_pay;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_Home;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;
import com.nxg.axismerchant.fragments.qr.PageFragmentForQR_SignUpFeature;
import com.nxg.axismerchant.fragments.qr.PageFragmentForQR_SignUpFees;
import com.nxg.axismerchant.fragments.sms.PageFragmentForSMS_SignUpFees;

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

public class Activity_QRSignUp extends AppCompatActivity implements View.OnClickListener {

    String MID,MOBILE;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    int flag = 0, eventClicked = 0;
    ImageView imgAccept;
    TextView txtDone,txtProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_sign_up);

        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        View layoutFeatures = findViewById(R.id.layoutFeatures);
        View layoutFees = findViewById(R.id.layoutFees);
        txtProceed = (TextView) findViewById(R.id.txtProceed);

        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        txtProceed.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
        layoutFeatures.setOnClickListener(this);
        layoutFees.setOnClickListener(this);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        SharedPreferences pref = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = pref.getString("MerchantID","0");
        MOBILE = pref.getString("MobileNum","0");

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);

        changeToFeatures();

        SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
        String res = preferences.getString("QRRequestValidated","No");
        if(res.equalsIgnoreCase("pending"))
        {
            ShowDialog2("pending");
        }

    }


    private void changeToFeatures()
    {
        txtProceed.setText(getString(R.string.proceed));
        (findViewById(R.id.viewFeatures)).setBackgroundColor(Color.WHITE);
        (findViewById(R.id.viewFees)).setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        PageFragmentForQR_SignUpFeature qr_signUpFeature = new PageFragmentForQR_SignUpFeature();
        Bundle bundle = new Bundle();
        bundle.putInt(PageFragmentForQR_SignUpFeature.ARG_OBJECT, 0);
        qr_signUpFeature.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.container,qr_signUpFeature).commit();
    }

    private void changeToFees()
    {
        txtProceed.setText(getString(R.string.proceedTerms));
        (findViewById(R.id.viewFees)).setBackgroundColor(Color.WHITE);
        (findViewById(R.id.viewFeatures)).setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        PageFragmentForQR_SignUpFees qr_signUpFees = new PageFragmentForQR_SignUpFees();
        Bundle bundle = new Bundle();
        bundle.putInt(PageFragmentForSMS_SignUpFees.ARG_OBJECT, 1);
        qr_signUpFees.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.container, qr_signUpFees).commit();
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
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));
                break;

            case R.id.txtProceed:
                if(eventClicked == 0) {
                    eventClicked = 1;
                    changeToFees();

                }else if(eventClicked == 1) {
                    ShowTermsAndConditions();
                }
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;

            case R.id.imgAccept:
                if(flag == 0)
                {
                    imgAccept.setImageResource(R.mipmap.login_tick);
                    txtDone.setTextColor(getResources().getColor(R.color.colorPrimary));
                    txtDone.setEnabled(true);
                    flag = 1;
                }else
                {
                    imgAccept.setImageResource(0);
                    txtDone.setTextColor(getResources().getColor(R.color.dark_gray));
                    txtDone.setEnabled(false);
                    flag = 0;
                }
                break;

            case R.id.txtDone:
                if(flag==1)
                    sendRequest();
                break;

            case R.id.layoutFeatures:
                changeToFeatures();
                break;

            case R.id.layoutFees:
                changeToFees();
                break;
        }
    }


    private void ShowDialog2(String status)
    {
     if(!status.equalsIgnoreCase("Not Requested")) {
         // custom dialog
         final Dialog dialog = new Dialog(this);
         dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         dialog.setContentView(R.layout.dialog_layout_message_for_sms);
         dialog.setCancelable(false);

         TextView txtMID = (TextView) dialog.findViewById(R.id.txtMID);
         TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);
         ImageView imgResponse = (ImageView) dialog.findViewById(R.id.imgResponse);
         TextView txtTitle = (TextView) dialog.findViewById(R.id.txtTitle);
         TextView txtMsg1 = (TextView) dialog.findViewById(R.id.txtMessage);
         TextView txtMsg2 = (TextView) dialog.findViewById(R.id.msg1);

         if (status.equalsIgnoreCase("Pending")) {
             txtTitle.setVisibility(View.GONE);
             txtMsg1.setText("Your Request is pending.");
             txtMsg2.setText("Our Relationship Manager will contact you soon.");
             txtMsg2.setMaxLines(3);
             txtConfirm.setText("Ok");
             SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
             SharedPreferences.Editor editor = preferences.edit();
             editor.putString("Validated", "pending");
             editor.apply();
         } else if (status.equalsIgnoreCase("Block")) {
             txtTitle.setVisibility(View.GONE);
             txtMsg1.setText("Your are not authorised for this service.");
             txtMsg2.setText("Contact your branch manager to avail this service.");
             txtMsg2.setMaxLines(3);
             imgResponse.setImageResource(R.mipmap.fail);
             txtConfirm.setText("Ok");
             SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
             SharedPreferences.Editor editor = preferences.edit();
             editor.putString("Validated", "pending");
             editor.apply();
         }
         // if button is clicked, close the custom dialog
         txtConfirm.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 dialog.dismiss();
                 onBackPressed();
             }
         });

         dialog.show();
     }
    }


    private void ShowTermsAndConditions()
    {
        // custom dialog
        final Dialog dialog = new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_qr_terms);

        imgAccept = (ImageView) dialog.findViewById(R.id.imgAccept);
        txtDone = (TextView) dialog.findViewById(R.id.txtDone);

        TextView txtTerms = (TextView) dialog.findViewById(R.id.txtTerms);

        txtTerms.setText(Html.fromHtml(getString(R.string.termsDataQR)));
        txtTerms.setMovementMethod(LinkMovementMethod.getInstance());

        imgAccept.setOnClickListener(this);
        txtDone.setOnClickListener(this);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }


    private void sendRequest() {
        if (Constants.isNetworkConnectionAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new SendRequest().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, "", "MVISA ONBOARDING REQUEST", "", "", "", "", "", Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new SendRequest().execute(Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, "", "MVISA ONBOARDING REQUEST", "", "", "", "", "", Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }


    public class SendRequest extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Activity_QRSignUp.this);
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mer_mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.tid), encryptDecrypt.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.service_type), encryptDecrypt.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.prob_details), encryptDecrypt.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.off_days), encryptDecrypt.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.visit_timing), encryptDecrypt.encrypt(arg0[7])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.contact_no), encryptDecrypt.encrypt(arg0[8])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.rolls_required), encryptDecrypt.encrypt(arg0[9])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[10])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[11])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[12])));

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
            try {
                if(!data.equals("")) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);

                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray addServiceRequest = object.getJSONArray("addServiceRequest");

                        for (int i = 0; i < addServiceRequest.length(); i++) {
                            JSONObject object2 = addServiceRequest.getJSONObject(i);
                            String Request_Number = object2.optString("Request_Number");
                            String Call_Status = object2.optString("Call_Status");

                            Request_Number = encryptDecrypt.decrypt(Request_Number);
                            Call_Status = encryptDecrypt.decrypt(Call_Status);

//                            Constants.showToast(Activity_QRTerms.this, "Your request number is "+Request_Number);
//                            onBackPressed();
                            progressDialog.dismiss();
                            ShowDialog2("Success",Request_Number,Call_Status);
                        }
                    }else
                    {
//                        Constants.showToast(Activity_QRTerms.this, "Network error, please try again later");
                        progressDialog.dismiss();
                        ShowDialog2(result, "Fail", "null");
                    }
                }else
                {
                    Constants.showToast(Activity_QRSignUp.this, getString(R.string.network_error));
                }


            } catch (JSONException e) {
                progressDialog.dismiss();
                Constants.showToast(Activity_QRSignUp.this, getString(R.string.network_error));
            }
            progressDialog.dismiss();
        }
    }


    private void ShowDialog2(String result, String status, String reqID)
    {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_message_for_sms);
        dialog.setCancelable(false);

        TextView txtMID = (TextView) dialog.findViewById(R.id.txtMID);
        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);

        TextView txtTitle = (TextView) dialog.findViewById(R.id.txtTitle);
        ImageView imgResponse = (ImageView) dialog.findViewById(R.id.imgResponse);
        TextView txtMsg1 = (TextView) dialog.findViewById(R.id.txtMessage);
        TextView txtMsg2 = (TextView) dialog.findViewById(R.id.msg1);

        if(!result.equals("Success"))
        {
            txtMsg1.setText(getString(R.string.sms_on_boarding_pop_up_submit_fail));
            imgResponse.setImageResource(R.mipmap.fail);
            txtConfirm.setText("Ok");
            txtMID.setVisibility(View.GONE);

            // if button is clicked, close the custom dialog
            txtConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Intent intent = new Intent(Activity_QRSignUp.this, Activity_Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }else
        {
            txtMsg2.setVisibility(View.GONE);
            txtMsg1.setText(getString(R.string.qr_on_boarding_pop_up));
            txtConfirm.setText("Ok");

            SharedPreferences preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString("QRRequestValidated","pending");
            editor.putString("Status",status);
            editor.putString("Request_ID", reqID);
            editor.apply();

            // if button is clicked, close the custom dialog
            txtConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    Intent intent = new Intent(Activity_QRSignUp.this, Activity_Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }


        dialog.show();
    }


}
