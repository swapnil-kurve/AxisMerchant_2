package com.nxg.axismerchant.classes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.nxg.axismerchant.database.DBHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Dell on 24-02-2016.
 */
public class Constants {

    public static String MPIN = "";
    public static String IMEI = "";
    public static String MERCHANT_ID = "";
    public static String MOBILE_NUM = "";
    public static String secretekeyDatabase="";

    public static final String LoginPref = "LoginPref";
    public static final String UserDetails = "UserDetails";
    public static final String ProfileInfo = "ProfileInfo";
    public static final String EPaymentData = "EPaymentData";
    public static final String QRPaymentData = "QRPaymentData";
    public static final String LanguageData = "LanguageData";

    public static String GOOGLE_PROJ_ID = "660348263150";
    public static String API = "AIzaSyBX5KAIoDg-k3Wt2sjSLB1B4S8RHDlxdYY";

//    public static final String DEMO_SERVICE = "http://demo.nxglabs.in/mservices.asmx/";

    public static final String DEMO_SERVICE = "http://merchantportal.paycraftsol.com/mservices.asmx/";
    public static final String DEMO_SERVICE_REFUND ="http://merchantportal.paycraftsol.com/";

//    public static final String DEMO_SERVICE = "http://192.168.88.14:9006/mservices.asmx/";
//    public static final String DEMO_SERVICE_REFUND = "http://192.168.88.14:9006/";

    public static final String[] FORCE_TLS_PROTOCOL = {"TLSv1.2"};
    public static String ServiceRef = "http://192.168.2.162:8094/";

    public static void showToast(Activity activity, String msg)
    {
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }


    //To get IMEI number of device
    public static void getIMEI(Context context){

        TelephonyManager mngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = mngr.getDeviceId();
    }


    //To check Internet connectivity of device
    public static boolean isNetworkConnectionAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }


    // To Validate email id
    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }



    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    // To retrieve stored MPIN from database
    public static void retrieveMPINFromDatabase(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor crs = db.rawQuery("select DISTINCT "+ DBHelper.MPIN + " from " + DBHelper.TABLE_NAME_MPIN, null);

        while (crs.moveToNext()) {
            MPIN = crs.getString(crs.getColumnIndex(DBHelper.MPIN));
        }

    }


    // To retrieve Notifications from database
    public static ArrayList retrieveFromDatabase(Context context, DBHelper dbHelper) {
        dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Notification notification;
        String str = "Unread";
        ArrayList<Notification> notificationArrayList = new ArrayList<>();

        Cursor crs = db.rawQuery("select DISTINCT "+ DBHelper.UID + ","+ DBHelper.MESSAGE +"," + DBHelper.READ_STATUS +","+ DBHelper.ON_DATE
                + " from " + DBHelper.TABLE_NAME_NOTIFICATION +" where "+DBHelper.READ_STATUS +" = '"+str+"'", null);

        while (crs.moveToNext()) {
            String mUID = crs.getString(crs.getColumnIndex(DBHelper.UID));
            String mMessage = crs.getString(crs.getColumnIndex(DBHelper.MESSAGE));
            String mDate = crs.getString(crs.getColumnIndex(DBHelper.ON_DATE));
            String mReadStatus = crs.getString(crs.getColumnIndex(DBHelper.READ_STATUS));

            notification = new Notification(mUID,mMessage,mDate,mReadStatus);
            notificationArrayList.add(notification);
        }
        return notificationArrayList;
    }


    //  To check device's SIM support
    public static int isSimSupport(Context context)
    {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int SIM_STATE = telephonyManager.getSimState();

        if(SIM_STATE == TelephonyManager.SIM_STATE_READY) {
            return 1;
        }
        else
        {
            switch(SIM_STATE)
            {
                case TelephonyManager.SIM_STATE_ABSENT: //SimState = "No Sim Found!";
                    return 100;

                case TelephonyManager.SIM_STATE_NETWORK_LOCKED: //SimState = "Network Locked!";
                    return 200;

                case TelephonyManager.SIM_STATE_PIN_REQUIRED: //SimState = "PIN Required to access SIM!";
                    return 300;

                case TelephonyManager.SIM_STATE_PUK_REQUIRED: //SimState = "PUK Required to access SIM!"; // Personal Unblocking Code
                    return 400;

                case TelephonyManager.SIM_STATE_UNKNOWN: //SimState = "Unknown SIM State!";
                    return 600;

                default:
                return 0;
            }
//            return 0;
        }
    }


    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }


    //  To change format of Date
    public static String changeDateFormat(String date)
    {
        String startDateString = date;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date startDate;
        try {
            startDate = df.parse(startDateString);
            String newDateString = df.format(startDate);
            System.out.println(newDateString);
            System.out.println(newDateString.split("/")[1]+newDateString.split("/")[0]+newDateString.split("/")[2]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
