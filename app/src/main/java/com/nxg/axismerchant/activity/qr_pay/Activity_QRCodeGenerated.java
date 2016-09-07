package com.nxg.axismerchant.activity.qr_pay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.Contents_QRCode;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.classes.QRCodeEncoder;
import com.nxg.axismerchant.database.DBHelper;

import java.util.ArrayList;

/**
 * Created by vismita.jain on 7/1/16.
 */
public class Activity_QRCodeGenerated extends Activity implements View.OnClickListener
{
    TextView txtOk;
    ImageView imgBack, imgNotification, imgProfile;
    String id, name, mcc, city, countryCode, currencyCode, amount="", primaryId="", secondaryId="";
    int checkId, checkName, check_mcc, checkCity, checkCountryCode, checkCurrencyCode, checkAmount, checkPrimaryId, checkSecondaryId;
    String checkIdLength, checkNameLength, check_mccLength, checkCityLength, checkCountryCodeLength, checkCurrencyCodeLength, checkAmountLength, checkPrimaryIdLength, checkSecondaryLength;
    ArrayList<String> alphanumeric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_generated);

        TextView txtNotification = (TextView) findViewById(R.id.txtNotificationCount);
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<Notification> notificationArrayList = Constants.retrieveFromDatabase(this, dbHelper);
        if(notificationArrayList.size() > 0)
        {
            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setText(String.valueOf(notificationArrayList.size()));
        }

        txtOk = (TextView) findViewById(R.id.txtOk);
        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgNotification = (ImageView) findViewById(R.id.imgNotification);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);

        alphanumeric = numericToAlpha();


        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            amount = bundle.getString("Amount");
            checkAmount = amount.length();
            checkAmountLength = alphanumeric.get(checkAmount);

            primaryId = bundle.getString("Primary_Id");
            secondaryId = bundle.getString("Secondary_Id");
        }

        getData();


        Log.e("id", id);
        Log.e("name", name);
        Log.e("mcc", mcc);
        Log.e("city", city);
        Log.e("countryCode", countryCode);
        Log.e("currencyCode", currencyCode);

        Log.e("checkId", checkId+"");
        Log.e("checkName", checkName+"");
        Log.e("check_mcc", check_mcc+"");
        Log.e("checkCity", checkCity+"");
        Log.e("checkCountryCode", checkCountryCode+"");
        Log.e("checkCurrencyCode", checkCurrencyCode+"");

        checkIdLength = alphanumeric.get(checkId);
        checkNameLength = alphanumeric.get(checkName);
        check_mccLength = alphanumeric.get(check_mcc);
        checkCityLength = alphanumeric.get(checkCity);
        checkCountryCodeLength = alphanumeric.get(checkCountryCode);
        checkCurrencyCodeLength = alphanumeric.get(checkCurrencyCode);

        Log.e("checkIdLength", checkIdLength+"");
        Log.e("checkNameLength", checkNameLength+"");
        Log.e("check_mccLength", check_mccLength+"");
        Log.e("checkCityLength", checkCityLength+"");
        Log.e("checkCountryCodeLength", checkCountryCodeLength+"");
        Log.e("checkCurrencyCodeLength", checkCurrencyCodeLength+"");

        if(checkId !=0)//if(checkId >8 || checkId <15)
        {
            id ="0"+checkIdLength+id;
            Log.e("id new", id);
            if(checkName <= 25)
            {
                name= "1"+checkNameLength+name;
                Log.e("name new", name);
                if(check_mcc == 4)
                {
                    mcc = "2"+check_mccLength+mcc;
                    Log.e("mcc new", mcc);
                    if(checkCity <=13)
                    {
                        city = "3"+checkCityLength+city;
                        Log.e("city new", city);
                        if(checkCountryCode == 2)
                        {
                            countryCode="4"+checkCountryCodeLength+countryCode;
                            Log.e("countryCode new", countryCode);
                            if(checkCurrencyCode == 3)
                            {
                                currencyCode = "5"+checkCurrencyCodeLength+currencyCode;
                                Log.e("currencyCode new", currencyCode);

                                String qrInputText = id+name+mcc+city+countryCode+currencyCode;
                                if(!amount.equalsIgnoreCase(""))
                                {
                                    if(checkAmount <= 12)
                                    {
                                        amount = "6" + checkAmountLength + amount;
                                        qrInputText = qrInputText+amount;
                                    }
                                }

                                if(!primaryId.equals("")) {
                                    primaryId = "7" + alphanumeric.get(primaryId.length()) + primaryId;
                                    qrInputText = qrInputText+primaryId;
                                }

                                if(!secondaryId.equals("")) {
                                    secondaryId = "8" + alphanumeric.get(secondaryId.length()) + secondaryId;
                                    qrInputText = qrInputText+secondaryId;
                                }

                                Log.e("GenerateQRCode", qrInputText);

                                //Find screen size
                                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                                Display display = manager.getDefaultDisplay();
                                Point point = new Point();
                                display.getSize(point);
                                int width = point.x;
                                int height = point.y;
                                int smallerDimension = width < height ? width : height;
                                smallerDimension = smallerDimension * 3/4;

                                //Encode with a QR Code image
                                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                                        null,
                                        Contents_QRCode.Type.TEXT,
                                        BarcodeFormat.QR_CODE.toString(),
                                        smallerDimension);
                                try {
                                    Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                                    ImageView myImage = (ImageView) findViewById(R.id.img_qrCode);
                                    myImage.setImageBitmap(bitmap);
                                } catch (WriterException e) {
                                }
                            }
                        }
                    }
                }
            }
        }

        imgBack.setOnClickListener(this);
        imgNotification.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        txtOk.setOnClickListener(this);
    }

    private void getData() {
        SharedPreferences pref = getSharedPreferences(Constants.ProfileInfo, Context.MODE_PRIVATE);
        if(pref.contains("merLegalName"))
        {
            id = pref.getString("mvisaId","");
            name = pref.getString("merLegalName","");
            mcc = pref.getString("mcc","");
            city = pref.getString("mCity","");
            countryCode = pref.getString("COUNTRY_Code","");
            currencyCode = pref.getString("currencyCode","");

            if(id.length() > 15)
                id = id.substring(0,15);
            if(name.length() > 25)
                name = name.substring(0,25);
            if(mcc.length() > 4)
                mcc = mcc.substring(0,4);
            if(city.length() > 13)
                city = city.substring(0,13);
            if(countryCode.length() > 2)
                countryCode = countryCode.substring(0,2);
            if(currencyCode.length() > 3)
                currencyCode = countryCode.substring(0,3);
            if(amount.length() > 12)
                amount = amount.substring(0,12);
            if(primaryId.length() > 26)
                primaryId = primaryId.substring(0,26);
            if(secondaryId.length() > 26)
                secondaryId = secondaryId.substring(0,26);

            checkId = id.length();
            checkName = name.length();
            check_mcc = mcc.length();
            checkCity = city.length();
            checkCountryCode = countryCode.length();
            checkCurrencyCode = currencyCode.length();
            checkAmount = amount.length();
            checkPrimaryId = primaryId.length();
            checkSecondaryId = secondaryId.length();


        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.txtOk:
                onBackPressed();
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

    public ArrayList numericToAlpha() {
        alphanumeric = new ArrayList<>();
        alphanumeric.add("0");
        alphanumeric.add("1");
        alphanumeric.add("2");
        alphanumeric.add("3");
        alphanumeric.add("4");
        alphanumeric.add("5");
        alphanumeric.add("6");
        alphanumeric.add("7");
        alphanumeric.add("8");
        alphanumeric.add("9");
        alphanumeric.add("A");
        alphanumeric.add("B");
        alphanumeric.add("C");
        alphanumeric.add("D");
        alphanumeric.add("E");
        alphanumeric.add("F");
        alphanumeric.add("G");
        alphanumeric.add("H");
        alphanumeric.add("I");
        alphanumeric.add("J");
        alphanumeric.add("K");
        alphanumeric.add("L");
        alphanumeric.add("M");
        alphanumeric.add("N");
        alphanumeric.add("O");
        alphanumeric.add("P");
        alphanumeric.add("Q");
        alphanumeric.add("R");
        alphanumeric.add("S");
        alphanumeric.add("T");
        alphanumeric.add("U");
        alphanumeric.add("V");
        alphanumeric.add("W");
        alphanumeric.add("X");
        alphanumeric.add("Y");
        alphanumeric.add("Z");

        return alphanumeric;
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

}