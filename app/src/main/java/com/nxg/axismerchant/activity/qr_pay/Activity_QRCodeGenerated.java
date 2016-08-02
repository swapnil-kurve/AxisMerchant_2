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
    int checkId, checkName, check_mcc, checkCity, checkCountryCode, checkCurrencyCode, checkAmount;

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


                SharedPreferences preferences = getSharedPreferences(Constants.QRPaymentData,MODE_PRIVATE);
                if(preferences.contains("Amount")) {
                    amount = preferences.getString("Amount", "0");
                    checkAmount = amount.length();
                }
                if(preferences.contains("Primary_Id"))
                    primaryId = preferences.getString("Primary_Id","0");
                if(preferences.contains("Secondary_Id"))
                    secondaryId = preferences.getString("Secondary_Id","0");

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

            if(checkId !=0)//if(checkId >8 || checkId <15)
            {
                id ="0"+checkId+id;
                Log.e("id new", id);
                if(checkName <= 25)
                {
                    name= "1"+checkName+name;
                    Log.e("name new", name);
                    if(check_mcc == 4)
                    {
                        mcc = "2"+check_mcc+mcc;
                        Log.e("mcc new", mcc);
                        if(checkCity <=13)
                        {
                            city = "3"+checkCity+city;
                            Log.e("city new", city);
                            if(checkCountryCode == 2)
                            {
                                countryCode="4"+checkCountryCode+countryCode;
                                Log.e("countryCode new", countryCode);
                                if(checkCurrencyCode == 3)
                                {
                                    currencyCode = "5"+checkCurrencyCode+currencyCode;
                                    Log.e("currencyCode new", currencyCode);
                                    if(checkAmount <= 12)
                                    {
                                        String qrInputText = id+name+mcc+city+countryCode+currencyCode;

                                        if(!amount.equals("")) {
                                            amount = "6" + checkAmount + amount;
                                            qrInputText = qrInputText+amount;
                                        }
                                        if(!primaryId.equals("")) {
                                            primaryId = "7" + primaryId.length() + primaryId;
                                            qrInputText = qrInputText+primaryId;
                                        }
                                        if(!secondaryId.equals("")) {
                                            secondaryId = "8" + secondaryId.length() + secondaryId;
                                            qrInputText = qrInputText+secondaryId;
                                        }
                                        Log.e("amount new", amount);

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
                                            e.printStackTrace();
                                        }
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

            checkId = id.length();
            checkName = name.length();
            check_mcc = mcc.length();
            checkCity = city.length();
            checkCountryCode = countryCode.length();
            checkCurrencyCode = currencyCode.length();

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


}
