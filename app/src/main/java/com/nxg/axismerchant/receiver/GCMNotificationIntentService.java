package com.nxg.axismerchant.receiver;

/**
 * Created by Dell on 30-11-2015.
 */

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.offers.Activity_OffersNotices;
import com.nxg.axismerchant.activity.qr_pay.Activity_QRPayHome;
import com.nxg.axismerchant.activity.qr_pay.Activity_QRSignUp;
import com.nxg.axismerchant.activity.qr_pay.Activity_QRTransactionDetails;
import com.nxg.axismerchant.activity.sms.Activity_SMSPayHome;
import com.nxg.axismerchant.activity.sms.Activity_SMSSignUp;
import com.nxg.axismerchant.activity.sms.Activity_TransactionStatusDetails;
import com.nxg.axismerchant.activity.start.Activity_Home;
import com.nxg.axismerchant.activity.start.Activity_Main;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.database.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;


public class GCMNotificationIntentService extends IntentService {
    // Sets an ID for the profile, so it can be updated
    public static final int notifyID = 9001;
    NotificationCompat.Builder builder;
    DBHelper dbHelper;
    SharedPreferences preferences;
    public GCMNotificationIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), ""+extras.get("message"), ""+extras.get("subtitle"), ""+extras.get("imgPath"), ""+extras.get("promotionType"),""+extras.get("PromotionId"),""+extras.get("withOption"),""+extras.get("invNo"),""+extras.get("transStatus"),""+extras.get("GCM_type"),""+extras.get("reqStatus"));
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
                sendNotification("Deleted messages on server: "
                        + extras.toString(), ""+extras.get("message"), ""+extras.get("subtitle"), ""+extras.get("imgPath"), ""+extras.get("promotionType"),""+extras.get("PromotionId"),""+extras.get("withOption"),""+extras.get("invNo"),""+extras.get("transStatus"),""+extras.get("GCM_type"),""+extras.get("reqStatus"));
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {
                sendNotification(""+ extras.get("title"), ""+extras.get("message"), ""+extras.get("subtitle"), ""+extras.get("imgPath"), ""+extras.get("promotionType"),""+extras.get("PromotionId"),""+extras.get("withOption"),""+extras.get("invNo"),""+extras.get("transStatus"),""+extras.get("GCM_type"),""+extras.get("reqStatus"));
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Check conditions and Show notification
     * @param title
     * @param message
     * @param SubTitle
     * @param imgPath
     * @param promotionType
     * @param promotionID
     * @param withOption
     * @param invNo
     * @param transStatus
     * @param gcm_type
     * @param reqStatus
     */
    private void sendNotification(String title, String message, String SubTitle, String imgPath, String promotionType, String promotionID, String withOption, String invNo, String transStatus, String gcm_type, String reqStatus) {
        Intent resultIntent;

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String currentDate = sdf.format(new Date());

        preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        String status = preferences.getString("KeepLoggedIn","false");            //Get keep logged in status

        if(status.equals("false")) { // This block executes when Keep logged in not checked or user logged out
            resultIntent = new Intent(this, Activity_Main.class);
            if(gcm_type.equalsIgnoreCase("Offers"))
            {
                InsertIntoPromotionTable(title, message, SubTitle, imgPath, promotionType, promotionID, withOption);
            }else if(gcm_type.equalsIgnoreCase("notification")){

                InsertIntoNotification(currentDate,message);
                resultIntent = new Intent(this, Activity_Notification.class);
            }else if(gcm_type.equalsIgnoreCase("SMS_Transaction_Status") || gcm_type.equalsIgnoreCase("SMS_Refund_Transaction"))
            {
                preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Invoice Number",invNo);
                editor.apply();

                UpdateIntoEPay(invNo, transStatus);
                InsertIntoNotification(currentDate,message);
            }else if(gcm_type.equalsIgnoreCase("SMS_Request_Status"))
            {
                preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("SMSRequestValidated",reqStatus);
                editor.apply();
                InsertIntoNotification(currentDate,message);
            }else if(gcm_type.equalsIgnoreCase("QR_Transaction_Status") || gcm_type.equalsIgnoreCase("QR_Refund_Transaction"))
            {
                preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Invoice Number",invNo);
                editor.apply();

                UpdateIntoEPay(invNo, transStatus);
                InsertIntoNotification(currentDate,message);
            }else if(gcm_type.equalsIgnoreCase("QR_Request_Status"))
            {
                preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("QRRequestValidated",reqStatus);
                editor.apply();
                InsertIntoNotification(currentDate,message);
            }
        }else       //// This block executes when Keep logged in checked or user logged In
        {
            if(gcm_type.equalsIgnoreCase("Offers"))
            {
                InsertIntoPromotionTable(title, message, SubTitle, imgPath, promotionType, promotionID, withOption);
                resultIntent = new Intent(this, Activity_OffersNotices.class);
            }else if(gcm_type.equalsIgnoreCase("notification"))
            {

                InsertIntoNotification(currentDate,message);
                resultIntent = new Intent(this, Activity_Notification.class);
            }else if(gcm_type.equalsIgnoreCase("SMS_Transaction_Status") || gcm_type.equalsIgnoreCase("SMS_Refund_Transaction"))
            {
                preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Invoice Number",invNo);
                editor.apply();

                UpdateIntoEPay(invNo, transStatus);
                InsertIntoNotification(currentDate,message);
                if(gcm_type.equalsIgnoreCase("SMS_Refund_Transaction"))
                    resultIntent = new Intent(this, Activity_Notification.class);
                else
                    resultIntent = new Intent(this, Activity_TransactionStatusDetails.class);
            }else if(gcm_type.equalsIgnoreCase("SMS_Request_Status"))
            {
                preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("SMSRequestValidated",reqStatus);
                editor.apply();
                if(reqStatus.equalsIgnoreCase("pending"))
                    resultIntent = new Intent(this, Activity_SMSSignUp.class);
                else
                    resultIntent = new Intent(this, Activity_SMSPayHome.class);
                InsertIntoNotification(currentDate,message);
            }else if(gcm_type.equalsIgnoreCase("QR_Transaction_Status") || gcm_type.equalsIgnoreCase("QR_Refund_Transaction"))
            {
                preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Invoice Number",invNo);
                editor.apply();

                if(gcm_type.equalsIgnoreCase("QR_Refund_Transaction"))
                    resultIntent = new Intent(this, Activity_Notification.class);
                else
                    resultIntent = new Intent(this, Activity_QRTransactionDetails.class);
            }else if(gcm_type.equalsIgnoreCase("QR_Request_Status"))
            {
                preferences = getSharedPreferences(Constants.EPaymentData, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("QRRequestValidated",reqStatus);
                editor.apply();
                if(reqStatus.equalsIgnoreCase("pending"))
                    resultIntent = new Intent(this, Activity_QRSignUp.class);
                else
                    resultIntent = new Intent(this, Activity_QRPayHome.class);
                InsertIntoNotification(currentDate,message);
            }else
            {
                resultIntent = new Intent(this, Activity_Home.class);
            }
        }


        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(title))
                .setContentTitle(Html.fromHtml(title))
                .setContentText(Html.fromHtml(message))
                .setSmallIcon(R.mipmap.axis_mnemonic);

        mNotifyBuilder.setContentIntent(resultPendingIntent);

        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        mNotifyBuilder.setDefaults(defaults);
        mNotifyBuilder.setAutoCancel(true);
        mNotificationManager.notify(notifyID, mNotifyBuilder.build());
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.axis_mnemonic : R.mipmap.ic_launcher;
    }
    /**
     * Update Refund Status of SMS Pay into Table
     * @param invNo
     * @param transStatus
     */
    private void UpdateIntoEPay(String invNo, String transStatus) {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.STATUS, transStatus);
        if(transStatus.equalsIgnoreCase("Success"))
            values.put(DBHelper.IS_REFUND, "0");

        long id = db.update(DBHelper.TABLE_NAME_E_PAYMENT,values,DBHelper.INVOICE_NO +" = "+invNo, null);
    }


    /**
     * Insert Offers into Table to maintain Offers list
     * @param title
     * @param message
     * @param SubTitle
     * @param imgPath
     * @param promotionType
     * @param promotionID
     * @param withOption
     */

    private void InsertIntoPromotionTable(String title, String message, String SubTitle, String imgPath, String promotionType, String promotionID, String withOption) {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.TITLE, title);
        values.put(DBHelper.SUB_TITLE, SubTitle);
        values.put(DBHelper.MESSAGE, message);
        values.put(DBHelper.IMG_URL, imgPath);
        values.put(DBHelper.PROMOTION_TYPE, promotionType);
        values.put(DBHelper.PROMOTION_ID, promotionID);
        values.put(DBHelper.WITH_OPTION, withOption);
        values.put(DBHelper.STATUS, "Awaiting");
        values.put(DBHelper.READ_STATUS, "Unread");

        long id = db.insert(DBHelper.TABLE_NAME_PROMOTIONS,null, values);
        Log.v("id", String.valueOf(id));
    }


    /**
     * Insert Notification into Table to maintain notification list
     * @param currentDate
     * @param message
     */
    private void InsertIntoNotification(String currentDate, String message) {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.ON_DATE,currentDate);
        values.put(DBHelper.MESSAGE, message);
        values.put(DBHelper.READ_STATUS, "Unread");

        long id = db.insert(DBHelper.TABLE_NAME_NOTIFICATION,null, values);
        Log.v("id", String.valueOf(id));
    }

}
