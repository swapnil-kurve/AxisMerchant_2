package com.nxg.axismerchant.activity.start;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.AppActivity;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.fragments.login.SignInFragment;
import com.nxg.axismerchant.fragments.login.SignUpFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class Activity_Main extends AppActivity {

    SharedPreferences preferences;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    GoogleCloudMessaging gcmObj;
    String regId;
    private final static int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 1111;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());


       /* preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        if(preferences.contains("LoggedIn"))
        {
            String mSignUpStatus = preferences.getString("LoggedIn","false");
            if(mSignUpStatus.equals("true")) {

                String status = preferences.getString("KeepLoggedIn","false");
                if(status.equals("true")) {
                    startActivity(new Intent(this, Activity_Home.class));
                    finish();
                }else {
                    changeToSignIn();
                    (findViewById(R.id.layoutSignUp)).setVisibility(View.GONE);
                }
            }
        }else
            changeToSignUp();*/

        Bundle data = getIntent().getExtras();
        if(data != null && data.containsKey("EntryType"))
        {
            String mEntryType = data.getString("EntryType");
            if(mEntryType.equalsIgnoreCase("SignIn"))
                changeToSignIn();
            else
                changeToSignUp();
        }


        /**
         * Run time permissions for Android M
         */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
//        int hasSMSPermission = checkSelfPermission( Manifest.permission.SEND_SMS );
            List<String> permissions = new ArrayList<String>();
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }

//        if( hasSMSPermission != PackageManager.PERMISSION_GRANTED ) {
//            permissions.add( Manifest.permission.SEND_SMS );
//        }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_SOME_FEATURES_PERMISSIONS);
            }

        }
/************************/


        preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        String loginStatus = preferences.getString("LoggedIn", "false");

        if (loginStatus.equals("false")) {
            registerInBackground();
        } else {
            String mTitle = null;// mMessage = null, mSubTitle = null, mImgPath = null, mPromotionType = null, mPromotionID = null, mWithOption = null;
            Bundle bundle = new Bundle();
            if(getIntent().getExtras() != null)
            {
                mTitle = getIntent().getExtras().getString("Title");
                bundle.putString("Title",mTitle);
            }

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_CODE_SOME_FEATURES_PERMISSIONS: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void changeToSignIn()
    {
        (findViewById(R.id.viewSignIn)).setBackgroundColor(Color.WHITE);
        (findViewById(R.id.viewSignUp)).setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        SignInFragment signInFragment = new SignInFragment();
        getFragmentManager().beginTransaction().replace(R.id.container,signInFragment).commit();
    }

    private void changeToSignUp()
    {
        (findViewById(R.id.viewSignUp)).setBackgroundColor(Color.WHITE);
        (findViewById(R.id.viewSignIn)).setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        SignUpFragment signUpFragment = new SignUpFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, signUpFragment).commit();
    }


    // AsyncTask to register Device in GCM Server
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcmObj == null) {
                        gcmObj = GoogleCloudMessaging
                                .getInstance(getApplicationContext());
                    }
                    regId = gcmObj
                            .register(Constants.GOOGLE_PROJ_ID);
                    msg = "Registration ID :" + regId;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(regId)) {
                    // Store RegId created by GCM Server in SharedPref
                    storeRegIdinSharedPref(getApplicationContext(), regId);
//                    Contents.showToast(Activity_EnterCardDetails.this, "Registered with GCM Server successfully");
                } else {
//                    Constants.showToast(Activity_Main.this, "Reg ID Creation Failed.nnEither you haven't enabled Internet or GCM server is busy right now. Make sure you enabled Internet and try registering again after some time."+msg);
                }
            }
        }.execute(null, null, null);
    }

    // Store  RegId and Email entered by User in SharedPref
    private void storeRegIdinSharedPref(Context context, String regId) {
        SharedPreferences prefs = getSharedPreferences(Constants.UserDetails,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("REG_ID", regId);
        editor.commit();

    }


    /*// Check if Google Playservices is installed in Device or not
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        // When Play services not found in device
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // Show Error dialog to install Play services
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Constants.showToast(Activity_Main.this, "This device doesn't support Play services, App will not work normally");
                finish();
            }
            return false;
        } else {
            *//*Toast.makeText(
                    getApplicationContext(),
                    "This device supports Play services, App will work normally",
                    Toast.LENGTH_LONG).show();*//*
        }
        return true;
    }*/

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    // When Application is resumed, check for Play services support to make sure app will be running normally
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }



}
