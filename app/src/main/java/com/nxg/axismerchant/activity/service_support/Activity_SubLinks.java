package com.nxg.axismerchant.activity.service_support;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.Notification;
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
    String MID,MOBILE;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    EditText edtTID, edtProblemDetails, edtContactNumber;//, edtVisitTime;
    Spinner spinNoOfRolls, spinnerVisitingTime;
    Spinner spinWeeklyOff;
    String mTotalRollsRequired, weeklyOff, visitingTime;
    private String blockCharacterSet = "~#^|$%&*!()-+?,.<>@:;";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sublinks);

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
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");

        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);

        settotalRollsRequired();
        setMerchantWeekOff();
        setVisitingTime();

        spinNoOfRolls.setOnItemSelectedListener(this);
        spinWeeklyOff.setOnItemSelectedListener(this);
        spinnerVisitingTime.setOnItemSelectedListener(this);

        edtProblemDetails.setMaxLines(3);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

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
                txtSubCode.setText("Roll Required");
                txtHeading.setText("Quick Links");
                ((RelativeLayout)findViewById(R.id.lyNoOfRolls)).setVisibility(View.VISIBLE);
            }

            if(heading.equalsIgnoreCase("TrainingRequired")) {
                txtSubCode.setText("Training Required");
                txtHeading.setText("Quick Links");
            }

            if(heading.equalsIgnoreCase("AdaptorProblem")) {
                txtSubCode.setText("Adaptor Problem");
                txtHeading.setText("Terminal Hardware Issues");
            }

            if(heading.equalsIgnoreCase("BaseProblem")){
                txtSubCode.setText("Base Problem");
                txtHeading.setText("Terminal Hardware Issues");
            }

            if(heading.equalsIgnoreCase("BatteryProblem")){
                txtSubCode.setText("Battery Problem");
                txtHeading.setText("Terminal Hardware Issues");
            }

            if(heading.equalsIgnoreCase("CardReaderProblem")){
                txtSubCode.setText("Card Reader Problem");
                txtHeading.setText("Terminal Hardware Issues");
            }

            if(heading.equalsIgnoreCase("DisplayProblem")){
                txtSubCode.setText("Display Problem");
                txtHeading.setText("Terminal Hardware Issues");
            }

            if(heading.equalsIgnoreCase("KeysNotWorking")){
                txtSubCode.setText("Keys Not Working");
                txtHeading.setText("Terminal Hardware Issues");
            }

            if(heading.equalsIgnoreCase("PowerCardProblem")){
                txtSubCode.setText("Power Card Problem");
                txtHeading.setText("Terminal Hardware Issues");
            }

            if(heading.equalsIgnoreCase("PrinterProblem")){
                txtSubCode.setText("Printer Problem");
                txtHeading.setText("Terminal Hardware Issues");
            }

            if(heading.equalsIgnoreCase("SettlementProblem")){
                txtSubCode.setText("Settlement Problem");
                txtHeading.setText("Terminal Application Issues");
            }

            if(heading.equalsIgnoreCase("TerminalSoftwareCorrupted")){
                txtSubCode.setText("Terminal Software Corrupted");
                txtHeading.setText("Terminal Application Issues");
            }

            if(heading.equalsIgnoreCase("CallIssues")){
                txtSubCode.setText("Call Issues");
                txtHeading.setText("Other Issues");
            }

            if(heading.equalsIgnoreCase("PaymentInquiry")){
                txtSubCode.setText("Payment Inquiry");
                txtHeading.setText("Other Issues");
            }

            if(heading.equalsIgnoreCase("PickUpCard")){
                txtHeading.setText("Pick Up Card");
                txtHeading.setText("Other Issues");
            }

            if(heading.equalsIgnoreCase("DeclineCard")){
                txtSubCode.setText("Decline Card");
                txtHeading.setText("Other Issues");
            }

            if(heading.equalsIgnoreCase("CardSwipeError")){
                txtSubCode.setText("Card Swipe Error");
                txtHeading.setText("Other Issues");
            }

            if(heading.equalsIgnoreCase("AxisAccNo")){
                txtSubCode.setText("Axis Account No.");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("NeftRtgs")){
                txtSubCode.setText("Neft Rtgs");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("DbaName")){
                txtSubCode.setText("Dba Name");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("LegalName")){
                txtSubCode.setText("Legal Name");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("AddressChange")){
                txtSubCode.setText("Address Change");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("PhoneNo")){
                txtSubCode.setText("Phone No");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("NewLocation")){
                txtSubCode.setText("New Location");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("AssetSwapping")){
                txtSubCode.setText("Asset Swapping");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("Dcc")){
                txtSubCode.setText("Dcc");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("AdditionalDcc")){
                txtSubCode.setText("Additional Dcc");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("CashPos")){
                txtSubCode.setText("Cash Pos");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("Apply_mVisa")){
                txtSubCode.setText("Apply mVisa");
                txtHeading.setText("Account Management");
            }

            if(heading.equalsIgnoreCase("MprStatmentRequest")){
                txtSubCode.setText("Mpr Statment Request");
                txtHeading.setText("Account Management");
            }
        }

        if(txtHeading.getText().toString().trim().equalsIgnoreCase("Account Management"))
        {
            ((ImageView)findViewById(R.id.imgStar1)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.imgStar2)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.imgStar3)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.imgStar4)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.imgStar5)).setVisibility(View.GONE);
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
        visitingTime.add("9:00AM - 12:00PM");
        visitingTime.add("12:00PM - 4:00PM");
        visitingTime.add("4:00PM - 7:00PM");


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

        if(!txtHeading.getText().toString().trim().equalsIgnoreCase("")) {
            if (edtTID.getText().toString().trim().length() == 0 || edtProblemDetails.getText().toString().trim().length() == 0
                    || edtContactNumber.getText().toString().trim().length() == 0) {
                Constants.showToast(this, "Please fill required details");
            } else if (edtContactNumber.getText().toString().trim().length() < 10) {
                Constants.showToast(this, "Please enter valid mobile number");
            } else if (visitingTime.equals("") || visitingTime.equalsIgnoreCase("Visiting Time")) {
                Constants.showToast(this, "Please provide visiting time");
            } else if (weeklyOff.equals("") || weeklyOff.equalsIgnoreCase("Merchant Week-Off")) {
                Constants.showToast(this, "Please provide weekly off");
            } else if (txtSubCode.getText().toString().equalsIgnoreCase("Roll Required")) {
                if (mTotalRollsRequired.equals("") || mTotalRollsRequired.equalsIgnoreCase("No. Of Rolls")) {
                    Constants.showToast(this, "Please provide required roll");
                } else {
                    callService();
                }
            } else {
                callService();
            }
        }else{
            callService();
        }
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
        String WeeklyOff = weeklyOff;
        String serviceType = txtSubCode.getText().toString().trim();

        if (Constants.isNetworkConnectionAvailable(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetData().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, tid, serviceType, ProblemDetails, WeeklyOff, VisitingTime, contactNumber, RollRequired);
            } else {
                new GetData().execute(Constants.DEMO_SERVICE + "addServiceRequest", MID, MOBILE, tid, serviceType, ProblemDetails, WeeklyOff, VisitingTime, contactNumber, RollRequired);

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

    public class GetData extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Activity_SubLinks.this);
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

                            Constants.showToast(Activity_SubLinks.this, getString(R.string.request_number)+" "+Request_Number);
                            onBackPressed();
                        }
                    }else
                    {
                        Constants.showToast(Activity_SubLinks.this, getString(R.string.network_error));
                    }
                }else
                {
                    Constants.showToast(Activity_SubLinks.this, getString(R.string.network_error));
                }


            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Constants.showToast(Activity_SubLinks.this, getString(R.string.network_error));
            }
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private  ArrayAdapter<String> adapterForSpinner(ArrayList<String> list)
    {
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list)
        {
            @Override
            public boolean isEnabled(int position) {
                if(position == 0)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        return dataAdapter;
    }

}
