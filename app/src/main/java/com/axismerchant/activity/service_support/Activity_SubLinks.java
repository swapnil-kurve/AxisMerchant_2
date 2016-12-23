package com.axismerchant.activity.service_support;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.Activity_Notification;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vismita.jain on 7/1/16.
 */
public class Activity_SubLinks extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    ImageView imgBack, imgNotification, imgProfile;
    TextView txtHeading;
    TextView txtSubCode,txtCurrentDate, txtMID;
    String MID,MOBILE, mServiceType = "";
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    EditText edtTID, edtProblemDetails, edtContactNumber;//, edtVisitTime;
    Spinner spinNoOfRolls, spinnerVisitingTime;
    Spinner spinWeeklyOff;
    String mTotalRollsRequired, weeklyOff, visitingTime;
    ProgressDialogue progressDialog;
    private String blockCharacterSet = "~#^|$%&*!()-+?,.<>@:;";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sublinks);

        progressDialog = new ProgressDialogue();
        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        TextView txtSubmitRequest = (TextView) findViewById(R.id.txtSubmitRequest);
        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgNotification = (ImageView) findViewById(R.id.imgNotification);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        txtMID = (TextView) findViewById(R.id.txtMID);

        txtHeading = (TextView) findViewById(R.id.txtHeading);
        txtCurrentDate = (TextView) findViewById(R.id.txtCurrentDate);

        edtTID = (EditText) findViewById(R.id.edtTID);
        txtSubCode = (TextView) findViewById(R.id.txtSubcode);
        edtProblemDetails = (EditText) findViewById(R.id.edtProblemDescription);
        edtContactNumber = (EditText) findViewById(R.id.edtCustMobile);

        spinNoOfRolls = (Spinner) findViewById(R.id.spinnerNoOfRoll);
        spinWeeklyOff = (Spinner) findViewById(R.id.spinnerWeeklyOff);
        spinnerVisitingTime = (Spinner) findViewById(R.id.spinnerVisitingTime);


        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH:mm");
        String currentDateandTime = sdf.format(new Date());
        Log.e("current Date and time", currentDateandTime + "hrs");
        txtCurrentDate.setText(currentDateandTime + "hrs");

        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID","0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum","0"));

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);

        settotalRollsRequired();
        setMerchantWeekOff();
        setVisitingTime();

        spinNoOfRolls.setOnItemSelectedListener(this);
        spinWeeklyOff.setOnItemSelectedListener(this);
        spinnerVisitingTime.setOnItemSelectedListener(this);

        edtProblemDetails.setMaxLines(3);

        InputFilter[] filter = new InputFilter[2];
        filter[0] = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };
        filter[1] = new InputFilter.LengthFilter(10);

        edtContactNumber.setFilters(filter);

        InputFilter[] filterTID = new InputFilter[2];
        filterTID[0] = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };
        filterTID[1] = new InputFilter.LengthFilter(8);

        edtTID.setFilters(filterTID);

        txtMID.setText(MID);

        Bundle bundle = getIntent().getExtras();
        if(bundle!= null)
        {
            String heading = bundle.getString("Heading");
            if(heading.equalsIgnoreCase("RollsRequired")) {
                txtSubCode.setText(getString(R.string.roll_required));
                txtHeading.setText(getString(R.string.quick) + " " + getString(R.string.links));
                findViewById(R.id.lyNoOfRolls).setVisibility(View.VISIBLE);
                mServiceType = "ROLLS REQUIRED";
            }else
            if(heading.equalsIgnoreCase("TrainingRequired")) {
                txtSubCode.setText(getString(R.string.training_required));
                txtHeading.setText(getString(R.string.quick) + " " + getString(R.string.links));
                mServiceType = "TRAINING REQUIRED";
            }else
            if(heading.equalsIgnoreCase("AdaptorProblem")) {
                txtSubCode.setText(getString(R.string.adapter_problem));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.hw_issue));
                mServiceType = "ADAPTOR PROBLEM";
            }else
            if(heading.equalsIgnoreCase("BaseProblem")){
                txtSubCode.setText(getString(R.string.base_problem));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.hw_issue));
                mServiceType = "BASE PROBLEM";
            }else
            if(heading.equalsIgnoreCase("BatteryProblem")){
                txtSubCode.setText(getString(R.string.battery_problem));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.hw_issue));
                mServiceType = "BATTERY PROBLEM";
            }else
            if(heading.equalsIgnoreCase("CardReaderProblem")){
                txtSubCode.setText(getString(R.string.card_reader_problem));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.hw_issue));
                mServiceType = "CARD READER PROBLEM";
            }else
            if(heading.equalsIgnoreCase("DisplayProblem")){
                txtSubCode.setText(getString(R.string.display_problm));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.hw_issue));
                mServiceType = "DISPLAY PROBLEM";
            }else
            if(heading.equalsIgnoreCase("KeysNotWorking")){
                txtSubCode.setText(getString(R.string.key_problem));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.hw_issue));
                mServiceType = "KEYS NOT WORKING";
            }else
            if(heading.equalsIgnoreCase("PowerCardProblem")){
                txtSubCode.setText(getString(R.string.power_card_problem));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.hw_issue));
                mServiceType = "POWER CORD PROBLEM";
            }else
            if(heading.equalsIgnoreCase("PrinterProblem")){
                txtSubCode.setText(getString(R.string.printer_problem));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.hw_issue));
                mServiceType = "PRINTER PROBLEM";
            }else
            if(heading.equalsIgnoreCase("SettlementProblem")){
                txtSubCode.setText(getString(R.string.settlement_Problem));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.app_issue));
                mServiceType = "SETTLEMENT PROBLEM";
            }else
            if(heading.equalsIgnoreCase("TerminalSoftwareCorrupted")){
                txtSubCode.setText(getString(R.string.terminal_sw_currpted));
                txtHeading.setText(getString(R.string.terminal) + " " + getString(R.string.app_issue));
                mServiceType = "TERMINAL SOFTWARE CORRUPTED";
            }


            if(heading.equalsIgnoreCase("CallIssues")){
                txtSubCode.setText(getString(R.string.call_issues));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "CALL ISSUER";
            }else
            if(heading.equalsIgnoreCase("PaymentInquiry")){
                txtSubCode.setText(getString(R.string.payement_inquiry));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "PAYMENT INQUIRY";
            }else
            if(heading.equalsIgnoreCase("PickUpCard")){
                txtSubCode.setText(getString(R.string.pick_up_card));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "PICK UP CARD";
            }else
            if(heading.equalsIgnoreCase("DeclineCard")){
                txtSubCode.setText(getString(R.string.decline_card));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "DECLINE CARD";
            }else
            if(heading.equalsIgnoreCase("CardSwipeError")){
                txtSubCode.setText(getString(R.string.card_swipe_error));
                txtHeading.setText(getString(R.string.others));
                mServiceType = "CARD SWIPE ERROR";
            }

        }

        imgBack.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        txtSubmitRequest.setOnClickListener(this);

    }

    private void settotalRollsRequired() {

        // Spinner Drop down elements
        ArrayList<String> totalRollsRequired = new ArrayList<String>();
        totalRollsRequired.add("No. Of Rolls");
        totalRollsRequired.add("1");
        totalRollsRequired.add("2");
        totalRollsRequired.add("3");
        totalRollsRequired.add("4");
        totalRollsRequired.add("5");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = adapterForSpinner(totalRollsRequired);

        // attaching data adapter to spinner
        spinNoOfRolls.setAdapter(dataAdapter);

    }

    private void setMerchantWeekOff()
    {

        ArrayList<String> merchantWeekOff = new ArrayList<String>();
        merchantWeekOff.add("Merchant Week-Off");
        merchantWeekOff.add("No Week-Off");
        merchantWeekOff.add("Monday");
        merchantWeekOff.add("Tuesday");
        merchantWeekOff.add("Wednesday");
        merchantWeekOff.add("Thursday");
        merchantWeekOff.add("Friday");
        merchantWeekOff.add("Saturday");
        merchantWeekOff.add("Sunday");

        ArrayAdapter<String> dataAdapter = adapterForSpinner(merchantWeekOff);

        // attaching data adapter to spinner
        spinWeeklyOff.setAdapter(dataAdapter);
    }

    private void setVisitingTime() {

        // Spinner Drop down elements
        ArrayList<String> visitingTime = new ArrayList<String>();
        visitingTime.add("Visiting Time");
        visitingTime.add("9AM-12NOON");
        visitingTime.add("12NOON-4PM");
        visitingTime.add("4PM-7PM");


        ArrayAdapter<String> dataAdapter = adapterForSpinner(visitingTime);

        // attaching data adapter to spinner
        spinnerVisitingTime.setAdapter(dataAdapter);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtSubmitRequest:
                sendRequest();
                break;

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

    private void sendRequest() {

        /*if(!txtHeading.getText().toString().trim().equalsIgnoreCase("Account Management") && !txtHeading.getText().toString().trim().equalsIgnoreCase("Other Issues"))
        {*/
            /*if (edtTID.getText().toString().trim().length() == 0 || edtProblemDetails.getText().toString().trim().length() == 0
                    || edtContactNumber.getText().toString().trim().length() == 0)
            {
                Constants.showToast(this, "Please fill required details");
            } else*/
            if(edtTID.getText().toString().trim().length() == 0)
            {
                Constants.showToast(this, getString(R.string.invalid_id));
                edtTID.setError("");
            } else if(edtProblemDetails.getText().toString().trim().length() == 0)
            {
                Constants.showToast(this, getString(R.string.invalid_problem_desc));
                edtProblemDetails.setError("");
            } else if (edtContactNumber.getText().toString().trim().length() < 10)
            {
                Constants.showToast(this, getString(R.string.invalid_mobile_number));
                edtContactNumber.setError("");
            } else if (visitingTime.equals("") || visitingTime.equalsIgnoreCase("Visiting Time"))
            {
                Constants.showToast(this, getString(R.string.null_visit_time));
            } else if (weeklyOff.equals("") || weeklyOff.equalsIgnoreCase("Merchant Week-Off"))
            {
                Constants.showToast(this, getString(R.string.null_week_off));
            } else if (txtSubCode.getText().toString().equalsIgnoreCase("Roll Required"))
            {
                if (mTotalRollsRequired.equals("") || mTotalRollsRequired.equalsIgnoreCase("No. Of Rolls"))
                {
                    Constants.showToast(this, getString(R.string.no_of_rolls));
                } else
                {
                    callService();
                }
            } else {
                callService();
            }
       /* }else{
            if(edtProblemDetails.getText().toString().trim().length() == 0)
            {
                Constants.showToast(this, getString(R.string.invalid_problem_desc));
                edtProblemDetails.setError("");
            }else {
                callService();
            }
        }*/
    }

    private void callService() {
        String tid = edtTID.getText().toString().trim();
        String ProblemDetails = edtProblemDetails.getText().toString().trim();
        String contactNumber = edtContactNumber.getText().toString().trim();
        String VisitingTime = visitingTime;//edtVisitTime.getText().toString().trim();
        String RollRequired ;
        if(txtSubCode.getText().toString().equalsIgnoreCase("Roll Required"))
            RollRequired = mTotalRollsRequired;
        else
            RollRequired = "0";
        String WeeklyOff = weeklyOff.substring(0,3);
        String serviceType = mServiceType;

        if (Constants.isNetworkConnectionAvailable(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, tid, serviceType, ProblemDetails, WeeklyOff, VisitingTime, contactNumber, RollRequired, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);
            } else {
                new GetData().execute(Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, tid, serviceType, ProblemDetails, WeeklyOff, VisitingTime, contactNumber, RollRequired, Constants.SecretKey, Constants.AuthToken, Constants.IMEI);

            }
        } else {
            Constants.showToast(this, getString(R.string.no_internet));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId())
        {
            case R.id.spinnerNoOfRoll:
                mTotalRollsRequired = spinNoOfRolls.getSelectedItem().toString().trim();
                Log.e("mtotalroll required", mTotalRollsRequired);
                break;

            case R.id.spinnerWeeklyOff:
                weeklyOff = spinWeeklyOff.getSelectedItem().toString().trim();
                Log.e("weeklyOff", ""+weeklyOff);
                break;

            case R.id.spinnerVisitingTime:
                visitingTime = spinnerVisitingTime.getSelectedItem().toString().trim();
                Log.e("visitingTime", visitingTime);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private ArrayAdapter<String> adapterForSpinner(ArrayList<String> list) {
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        return dataAdapter;
    }

    private void ShowDialogReponse(String response, String request_Number, String docket_id, String response_code) {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_for_sr_request);
        dialog.setCancelable(true);

        TextView txtResponseStatus = (TextView) dialog.findViewById(R.id.txtResponseStatus);
        TextView txtRequestNumber = (TextView) dialog.findViewById(R.id.txtRequestNumber);
        ImageView imgResponse = (ImageView) dialog.findViewById(R.id.imgResponse);
        TextView txtConfirm = (TextView) dialog.findViewById(R.id.txtDone);

        if (response.equalsIgnoreCase("Success")) {
            txtResponseStatus.setText("Success");
            txtResponseStatus.setTextColor(Color.GREEN);
            if (docket_id.equals("")) {
                txtRequestNumber.setText(getString(R.string.request_raised) + "\n" + getString(R.string.request_number) + " \n" + request_Number);
            } else {
                txtRequestNumber.setText(getString(R.string.request_raised) + "\n" + getString(R.string.request_number) + " \n" + request_Number + ",\n Docket Id " + docket_id);
            }
            imgResponse.setImageResource(R.drawable.happiness);
        } else {
            txtResponseStatus.setText("Fail");
            txtResponseStatus.setTextColor(Color.RED);
            txtRequestNumber.setText(response_code + "\n" + getString(R.string.try_later));
            imgResponse.setImageResource(R.mipmap.fail);
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

    public class GetData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            progressDialog.onCreateDialog(Activity_SubLinks.this);
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
                            String docket_id = object2.optString("docket_id");
                            String responseCode = object2.optString("responseCode");

                            Request_Number = encryptDecrypt.decrypt(Request_Number);
                            Call_Status = encryptDecrypt.decrypt(Call_Status);
                            docket_id = encryptDecrypt.decrypt(docket_id);
                            responseCode = encryptDecrypt.decrypt(responseCode);

                            if(Request_Number.equalsIgnoreCase(""))
                                ShowDialogReponse("Fail", "", "", "");
                            else
                                ShowDialogReponse("Success", Request_Number, docket_id, responseCode);
                        }
                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(Activity_SubLinks.this, getString(R.string.session_expired));
                        logout();
                    }else
                    {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray addServiceRequest = object.getJSONArray("addServiceRequest");
                        String responseCode = "";
                        for (int i = 0; i < addServiceRequest.length(); i++) {
                            JSONObject object2 = addServiceRequest.getJSONObject(i);
                            responseCode = object2.optString("result");
                            responseCode = encryptDecrypt.decrypt(responseCode);
                        }
                        String res;
                        if (responseCode.contains("AlreadyAdded")) {
                            res = responseCode.split(",")[0];
                            ShowDialogReponse("Fail", "", "", res);
                        } else
                            ShowDialogReponse("Fail", "", "", responseCode);
                    }
                }else
                {
                    Constants.showToast(Activity_SubLinks.this, getString(R.string.network_error));
                }


            } catch (JSONException e) {
                progressDialog.dismiss();
                Constants.showToast(Activity_SubLinks.this, getString(R.string.network_error));
            }
            progressDialog.dismiss();
        }
    }

}
