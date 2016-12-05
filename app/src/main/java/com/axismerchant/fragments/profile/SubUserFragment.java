package com.axismerchant.fragments.profile;


import android.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.start.Activity_Main;
import com.axismerchant.activity.start.CustomizedExceptionHandler;
import com.axismerchant.classes.Constants;
import com.axismerchant.classes.EncryptDecrypt;
import com.axismerchant.classes.EncryptDecryptRegister;
import com.axismerchant.classes.ExpandableListAdapter;
import com.axismerchant.classes.HTTPUtils;
import com.axismerchant.classes.UserList;

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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubUserFragment extends Fragment implements View.OnClickListener, ExpandableListView.OnChildClickListener, AdapterView.OnItemSelectedListener, ExpandableListView.OnGroupExpandListener {

    public static int editFlag = 0;
    public ExpandableListView expandableListView;
    EditText edtUserName, edtMobileNo, edtEmailId;
    String mUserName = "", mMobileNo = "", mEmailID = "", mMVisaId = "", MID, MOBILE;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    Spinner spinMVisaID;
    ArrayList<UserList> userListArrayList;
    ArrayList<String> mVisaArrayList;
    UserList userList;
    TextView txtCreateNewUser,txtSubmit;
    View lyCreateUser, lyUserList;
    private String blockCharacterSet = "~#^|$%&*!()-+?,.<>@:;";
    private String blockNumberSet = "1234567890~#^|$%&*!()-+?,.<>@:;";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_user, container, false);

        getInitialize(view);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = encryptDecryptRegister.decrypt(preferences.getString("MerchantID", "0"));
        MOBILE = encryptDecryptRegister.decrypt(preferences.getString("MobileNum", "0"));
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());

        getUserList();


        return view;
    }


    private void getUserList() {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetUserList().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getMerchantAddedUsers", MID, MOBILE, Constants.SecretKey, Constants.AuthToken,Constants.IMEI);
            } else {
                new GetUserList().execute(Constants.DEMO_SERVICE + "getMerchantAddedUsers", MID, MOBILE,Constants.SecretKey, Constants.AuthToken,Constants.IMEI);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }

    }

    private void getInitialize(View view) {

        edtEmailId = (EditText) view.findViewById(R.id.edtEmail);
        edtMobileNo = (EditText) view.findViewById(R.id.edtMobileNumber);
        edtUserName = (EditText) view.findViewById(R.id.edtUsername);
        spinMVisaID = (Spinner) view.findViewById(R.id.spinnermVisaId);
        txtSubmit = (TextView) view.findViewById(R.id.txtSubmit);
        txtCreateNewUser = (TextView) view.findViewById(R.id.txtCreateNewUser);
        lyCreateUser = view.findViewById(R.id.lyCreateUser);
        lyUserList = view.findViewById(R.id.lyTop);

        userListArrayList = new ArrayList<>();

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        expandableListView = (ExpandableListView) view.findViewById(R.id.expandable_listview);

        txtSubmit.setOnClickListener(this);
        txtCreateNewUser.setOnClickListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnGroupExpandListener(this);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.ProfileInfo, Context.MODE_PRIVATE);
        //Retrieve the values
        Set<String> set = preferences.getStringSet("mVisaIds", null);

        if(set != null) {
            mVisaArrayList = new ArrayList<>(set);
        }else
        {
            mVisaArrayList = new ArrayList<>();
        }

        setMVisaID("");
        spinMVisaID.setOnItemSelectedListener(this);

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

        edtMobileNo.setFilters(filter);

        InputFilter[] numFilter = new InputFilter[1];
        numFilter[0] = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockNumberSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };

        edtUserName.setFilters(numFilter);

    }

    private void setMVisaID(String mMVisaId) {
        ArrayList mVisaList = new ArrayList();
        if (mMVisaId.equalsIgnoreCase(""))
            mVisaList.add("mVisa Id");
        else
            mVisaList.add(mMVisaId);
        for (int i = 0; i < mVisaArrayList.size(); i++) {
            mVisaList.add(encryptDecrypt.decrypt(mVisaArrayList.get(i)));
        }

        ArrayAdapter<String> dataAdapter = adapterForSpinner(mVisaList);
        // attaching data adapter to spinner
        spinMVisaID.setAdapter(dataAdapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtSubmit:
                hideKeyboard();
                getData();
                break;

            case R.id.txtCreateNewUser:
                lyCreateUser.setVisibility(View.VISIBLE);
                lyUserList.setVisibility(View.GONE);
                editFlag = 0;
                break;
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputManager.isAcceptingText()) {
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    private void getData() {

        try {
            if (editFlag == 0) {
                mUserName = edtUserName.getText().toString().trim();
                mMobileNo = edtMobileNo.getText().toString().trim();
                mEmailID = edtEmailId.getText().toString().trim();

                if (mUserName.equals("") || mMobileNo.equals("")) {
                    Constants.showToast(getActivity(), getString(R.string.sub_user_not_entered));
                } else if (mMobileNo.equals("") || mMobileNo.length() < 10) {
                    Constants.showToast(getActivity(), getString(R.string.invalid_mobile_number));
                } else if (mMobileNo.startsWith("7") || mMobileNo.startsWith("8") || mMobileNo.startsWith("9")) {

                    if (mEmailID.length() != 0) {
                        if (Constants.isValidEmail(mEmailID)) {
                            createSubUser();
                        } else {
                            Constants.showToast(getActivity(), getString(R.string.invalid_email_id));
                        }
                    } else {
                        createSubUser();
                    }

                } else {
                    Constants.showToast(getActivity(), getString(R.string.enter_valid_mobile));
                }
            } else if (editFlag == 1) {
                updateUserList();
            }
        } catch (Exception e) {
            Constants.showToast(getActivity(), getString(R.string.enter_valid_details));
        }
    }

    private void updateUserList() {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new UpdateSubUser().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "updateUser", MID, MOBILE, userList.getMobileNo(), userList.getRegUsersID(), userList.getUserName(), userList.getEmailid(), mMVisaId,Constants.SecretKey, Constants.AuthToken,Constants.IMEI);
            } else {
                new UpdateSubUser().execute(Constants.DEMO_SERVICE + "updateUser", MID, MOBILE, userList.getMobileNo(), userList.getRegUsersID(), userList.getUserName(), userList.getEmailid(), mMVisaId,Constants.SecretKey, Constants.AuthToken,Constants.IMEI);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }
    }

    private void createSubUser() {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CreateSubUser().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "addUser", MID, MOBILE, mUserName, mMobileNo, mEmailID, mMVisaId,Constants.SecretKey, Constants.AuthToken,Constants.IMEI);
            } else {
                new CreateSubUser().execute(Constants.DEMO_SERVICE + "addUser", MID, MOBILE, mUserName, mMobileNo, mEmailID, mMVisaId,Constants.SecretKey, Constants.AuthToken,Constants.IMEI);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {

        userList = userListArrayList.get(groupPosition);

        edtUserName.setText(userList.getUserName());
        edtMobileNo.setText(userList.getMobileNo());
        edtEmailId.setText(userList.getEmailid());
        if(userList.getAssignedMVisaID().equalsIgnoreCase("") || userList.getAssignedMVisaID().equalsIgnoreCase("null"))
            spinMVisaID.setPrompt("mVisa Id");
        else {
            setMVisaID(userList.getAssignedMVisaID());
//            spinMVisaID.setPrompt(userList.getAssignedMVisaID());
        }

        edtUserName.setEnabled(false);
        edtMobileNo.setEnabled(false);
        edtEmailId.setEnabled(false);
        spinMVisaID.setEnabled(true);

        lyCreateUser.setVisibility(View.VISIBLE);
        lyUserList.setVisibility(View.GONE);
        txtSubmit.setText(getString(R.string.update));

        editFlag = 1;

        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinnermVisaId:
                mMVisaId = spinMVisaID.getSelectedItem().toString().trim();
                if(mMVisaId.equalsIgnoreCase("mVisa Id"))
                {
                    mMVisaId = "";
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onGroupExpand(int i) {

    }

    private void setAdapter() {
        ExpandableListAdapter listAdapter;
        HashMap<String, UserList> listDataChild = new HashMap<>();
        ArrayList<String> usernameArrayList = new ArrayList<>();

        if (userListArrayList.size() != 0) {
            int i;
            for (i = 0; i < userListArrayList.size(); i++) {
                usernameArrayList.add(userListArrayList.get(i).getUserName());

                listDataChild.put(userListArrayList.get(i).getRegUsersID(), userListArrayList.get(i));
            }
            listAdapter = new ExpandableListAdapter(getActivity(), usernameArrayList, listDataChild, userListArrayList);
            listAdapter.notifyDataSetChanged();
            expandableListView.setAdapter(listAdapter);
            txtCreateNewUser.setVisibility(View.VISIBLE);
        } else {
            txtCreateNewUser.setVisibility(View.GONE);
            lyCreateUser.setVisibility(View.VISIBLE);
        }
    }

    private ArrayAdapter<String> adapterForSpinner(ArrayList<String> list) {
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list) {
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

    private class CreateSubUser extends AsyncTask<String, Void, String> {
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.userName), encryptDecrypt.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecrypt.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.email_id), encryptDecrypt.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mvisa_id), encryptDecrypt.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[7])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[8])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[9])));

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
            CustomizedExceptionHandler.writeToFile(str);
            return str;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            try {
                if(data != null) {
                    JSONObject object1 = new JSONObject(data);
                    JSONArray addUser = object1.getJSONArray("addUser");

                    JSONObject obj = addUser.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        Constants.showToast(getActivity(), getString(R.string.sub_user_created));

                        getUserList();

                        lyCreateUser.setVisibility(View.GONE);
                        lyUserList.setVisibility(View.VISIBLE);

                        edtUserName.setText("");
                        edtMobileNo.setText("");
                        edtEmailId.setText("");

                    } else if (result.equalsIgnoreCase("Already exists")) {
                        Constants.showToast(getActivity(), getString(R.string.sub_user_mobile_already_exists));
                    } else if (result.equalsIgnoreCase("Invalid Merchantid and MobileNo")) {
                        Constants.showToast(getActivity(), result);
                    } else {
                        Constants.showToast(getActivity(), getString(R.string.invalid_details));
                    }
                    progressDialog.dismiss();
                }else {
                    progressDialog.dismiss();
                    Constants.showToast(getActivity(), getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }

    private class GetUserList extends AsyncTask<String, Void, String> {
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
            } catch (ParseException e1) {
                progressDialog.dismiss();
            } catch (IOException e) {
                progressDialog.dismiss();
            }
            CustomizedExceptionHandler.writeToFile(str);
            return str;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            try {
                if (data != null) {
                    JSONArray transaction = new JSONArray(data);
                    JSONObject object1 = transaction.getJSONObject(0);

                    JSONArray rowResponse = object1.getJSONArray("rowsResponse");
                    JSONObject obj = rowResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object = transaction.getJSONObject(1);
                        JSONArray transactionBetDates = object.getJSONArray("getMerchantAddedUsers");
                        userListArrayList.clear();
                        for (int i = 0; i < transactionBetDates.length(); i++) {

                            JSONObject object2 = transactionBetDates.getJSONObject(i);
                            String regUsersID = object2.optString("regUsersID");
                            String merchantId = object2.optString("merchantId");
                            String mobileNo = object2.optString("mobileNo");
                            String userName = object2.optString("userName");
                            String addedDate = object2.optString("addedDate");
                            String emailid = object2.optString("emailid");
                            String isRegistered = object2.optString("isRegistered");
                            String assignedMVisaID = object2.optString("assignedMVisaID");

                            regUsersID = encryptDecryptRegister.decrypt(regUsersID);
                            merchantId = encryptDecryptRegister.decrypt(merchantId);
                            mobileNo = encryptDecryptRegister.decrypt(mobileNo);
                            userName = encryptDecryptRegister.decrypt(userName);
                            addedDate = encryptDecryptRegister.decrypt(addedDate);
                            emailid = encryptDecryptRegister.decrypt(emailid);
                            isRegistered = encryptDecryptRegister.decrypt(isRegistered);
                            assignedMVisaID = encryptDecryptRegister.decrypt(assignedMVisaID);


                            userList = new UserList(regUsersID, merchantId, mobileNo, userName, addedDate, emailid, isRegistered, assignedMVisaID);
                            userListArrayList.add(userList);
                        }

                        setAdapter();
                        progressDialog.dismiss();

                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(getActivity(), getString(R.string.session_expired));
                        progressDialog.dismiss();
                        logout();
                    } else {
                        progressDialog.dismiss();
                        txtCreateNewUser.setVisibility(View.GONE);
                        lyCreateUser.setVisibility(View.VISIBLE);
                        Constants.showToast(getActivity(), getString(R.string.no_subuser));

                    }
                    progressDialog.dismiss();
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
            }
        }
    }

    private class UpdateSubUser extends AsyncTask<String, Void, String> {
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
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.merchant_id), encryptDecryptRegister.encrypt(arg0[1])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mobile_no), encryptDecryptRegister.encrypt(arg0[2])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.userMobileNo), encryptDecrypt.encrypt(arg0[3])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.regUsersId), encryptDecrypt.encrypt(arg0[4])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.userName), encryptDecrypt.encrypt(arg0[5])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.emailid), encryptDecrypt.encrypt(arg0[6])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.mvisa_id), encryptDecrypt.encrypt(arg0[7])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.isActive), encryptDecrypt.encrypt("Yes")));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.secretKey), encryptDecryptRegister.encrypt(arg0[8])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.authToken), encryptDecryptRegister.encrypt(arg0[9])));
                nameValuePairs.add(new BasicNameValuePair(getString(R.string.imei_no), encryptDecryptRegister.encrypt(arg0[10])));

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
            CustomizedExceptionHandler.writeToFile(str);
            return str;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            try {
                if(data != null) {
                    JSONArray array = new JSONArray(data);
                    JSONObject object0 = array.getJSONObject(0);
                    JSONArray rowsResponse = object0.getJSONArray("rowsResponse");

                    JSONObject obj = rowsResponse.getJSONObject(0);
                    String result = obj.optString("result");

                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object1 = array.getJSONObject(1);
                        JSONArray updateUser = object1.getJSONArray("updateUser");

                        JSONObject obj0 = updateUser.getJSONObject(0);
                        String res = obj0.optString("res");

                        res = encryptDecrypt.decrypt(res);

                        if(res.equalsIgnoreCase("Success")) {
                            Constants.showToast(getActivity(), getString(R.string.sub_user_updated));

                            getUserList();

                            lyCreateUser.setVisibility(View.GONE);
                            lyUserList.setVisibility(View.VISIBLE);

                            edtUserName.setText("");
                            edtMobileNo.setText("");
                            edtEmailId.setText("");
                            spinMVisaID.setPrompt("mVisa Id");
                        }else
                        {
                            Constants.showToast(getActivity(), getString(R.string.invalid_details));
                        }

                    }else if(result.equalsIgnoreCase("SessionFailure")){
                        Constants.showToast(getActivity(), getString(R.string.session_expired));
                        logout();
                    } else {
                        Constants.showToast(getActivity(), getString(R.string.invalid_details));
                    }
                    progressDialog.dismiss();
                }else
                {
                    Constants.showToast(getActivity(), getString(R.string.network_error));
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
            }

        }
    }


}
