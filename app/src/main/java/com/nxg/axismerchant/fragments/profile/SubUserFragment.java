package com.nxg.axismerchant.fragments.profile;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.ExpandableListAdapter;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.UserList;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class SubUserFragment extends Fragment implements View.OnClickListener {

    EditText edtUserName, edtMobileNo, edtEmailId;
    String mUserName = "", mMobileNo = "", mEmailID = "", MID,MOBILE;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;
    ExpandableListView expandableListView;
    public static int viewFlag = 0;

    ArrayList<UserList> userListArrayList;

    UserList userList;
    TextView txtCreateNewUser;
    View lyCreateUser,lyUserList;
    private String blockCharacterSet = "~#^|$%&*!()-+?,.<>@:;";
    private String blockNumberSet = "1234567890~#^|$%&*!()-+?,.<>@:;";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_user, container, false);

        getInitialize(view);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());

        getUserList();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    private void getUserList() {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetUserList().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE+"getMerchantAddedUsers", MID,MOBILE);
            } else {
                new GetUserList().execute(Constants.DEMO_SERVICE+"getMerchantAddedUsers", MID,MOBILE);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }

    }

    private void getInitialize(View view) {

        edtEmailId = (EditText) view.findViewById(R.id.edtEmail);
        edtMobileNo = (EditText) view.findViewById(R.id.edtMobileNumber);
        edtUserName = (EditText) view.findViewById(R.id.edtUsername);
        TextView txtSubmit = (TextView) view.findViewById(R.id.txtSubmit);
        txtCreateNewUser = (TextView) view.findViewById(R.id.txtCreateNewUser);
        lyCreateUser = view.findViewById(R.id.lyCreateUser);
        lyUserList = view.findViewById(R.id.lyTop);

        userListArrayList = new ArrayList<>();

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        expandableListView = (ExpandableListView) view.findViewById(R.id.expandable_listview);

        txtSubmit.setOnClickListener(this);
        txtCreateNewUser.setOnClickListener(this);

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


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtSubmit:
                getData();
                break;

            case R.id.txtCreateNewUser:
                lyCreateUser.setVisibility(View.VISIBLE);
                lyUserList.setVisibility(View.GONE);
                viewFlag = 1;
                break;
        }
    }

    private void getData() {

        try {
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
        }catch(Exception e)
        {
            Constants.showToast(getActivity(), getString(R.string.enter_valid_details));
        }
    }

    private void createSubUser()
    {
        if (Constants.isNetworkConnectionAvailable(getActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new CreateSubUser().executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "addUser", MID,MOBILE, mUserName, mMobileNo, mEmailID);
            } else {
                new CreateSubUser().execute(Constants.DEMO_SERVICE + "addUser", MID,MOBILE, mUserName, mMobileNo, mEmailID);

            }
        } else {
            Constants.showToast(getActivity(), getString(R.string.no_internet));
        }
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

            try {
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

                } else if(result.equals("Already exists")){
                    Constants.showToast(getActivity(), getString(R.string.sub_user_mobile_already_exists));
                }else
                {
                    Constants.showToast(getActivity(), getString(R.string.invalid_details));
                }
                progressDialog.dismiss();
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
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

            try {
                if (!data.equals("null")) {
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

                            regUsersID = encryptDecryptRegister.decrypt(regUsersID);
                            merchantId = encryptDecryptRegister.decrypt(merchantId);
                            mobileNo = encryptDecryptRegister.decrypt(mobileNo);
                            userName = encryptDecryptRegister.decrypt(userName);
                            addedDate = encryptDecryptRegister.decrypt(addedDate);
                            emailid = encryptDecryptRegister.decrypt(emailid);
                            isRegistered = encryptDecryptRegister.decrypt(isRegistered);


                            userList = new UserList(regUsersID, merchantId, mobileNo, userName, addedDate, emailid, isRegistered);
                            userListArrayList.add(userList);
                        }

                        setAdapter();
                        progressDialog.dismiss();

                    } else {
                        progressDialog.dismiss();
                        txtCreateNewUser.setVisibility(View.GONE);
                        lyCreateUser.setVisibility(View.VISIBLE);
                        Constants.showToast(getActivity(), getString(R.string.no_subuser));

                    }
                }
            }catch(JSONException e){
                progressDialog.dismiss();
                e.printStackTrace();
            }
        }
    }

    private void setAdapter() {
        ExpandableListAdapter listAdapter;
        HashMap<String, UserList> listDataChild = new HashMap<>();
        ArrayList<String> usernameArrayList = new ArrayList<>();

        if(userListArrayList.size() != 0) {
            int i;
            for (i = 0; i < userListArrayList.size(); i++) {
                usernameArrayList.add(userListArrayList.get(i).getUserName());

                listDataChild.put(userListArrayList.get(i).getRegUsersID(),userListArrayList.get(i));
            }
            listAdapter = new ExpandableListAdapter(getActivity(), usernameArrayList, listDataChild, userListArrayList);
            listAdapter.notifyDataSetChanged();
            expandableListView.setAdapter(listAdapter);
            txtCreateNewUser.setVisibility(View.VISIBLE);
        }else
        {
            txtCreateNewUser.setVisibility(View.GONE);
            lyCreateUser.setVisibility(View.VISIBLE);
        }
    }
}
