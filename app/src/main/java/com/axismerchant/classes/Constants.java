package com.axismerchant.classes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.axismerchant.R;
import com.axismerchant.database.DBHelper;
import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Created by Dell on 24-02-2016.
 */
public class Constants {

    public static final String LoginPref = "LoginPref";
    public static final String UserDetails = "UserDetails";
    public static final String ProfileInfo = "ProfileInfo";
    public static final String EPaymentData = "EPaymentData";
    public static final String LanguageData = "LanguageData";

    public static final String[] FORCE_TLS_PROTOCOL = {"TLSv1.2"};
    public static final String DEMO_SERVICE = "https://merchant.axisbank.co.in/mservices.asmx/";
    public static final String DEMO_SERVICE_REFUND = "https://merchant.axisbank.co.in/";
    public static String MPIN = "";
    public static String IMEI = "";
    public static String MERCHANT_ID = "";
    public static String MOBILE_NUM = "";
    public static String secretekeyDatabase = "";
    public static String AuthToken = "";

    //    public static final String DEMO_SERVICE = "http://merchantportal.paycraftsol.com/mservices.asmx/";
    //    public static final String DEMO_SERVICE_REFUND ="http://merchantportal.paycraftsol.com/";
    //    public static final String DEMO_SERVICE = "http://demo.nxglabs.in/mservices.asmx/";
    public static String SecretKey = "secretKey";
    public static String GOOGLE_PROJ_ID = "660348263150";
    public static String API = "AIzaSyBX5KAIoDg-k3Wt2sjSLB1B4S8RHDlxdYY";
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
        EncryptDecryptRegister encryptDecryptRegister = new EncryptDecryptRegister();

        Cursor crs = null;
        try{
            crs = db.rawQuery("select DISTINCT "+ DBHelper.MPIN + " from " + DBHelper.TABLE_NAME_MPIN, null);

            while (crs.moveToNext()) {
                MPIN = encryptDecryptRegister.decrypt(crs.getString(crs.getColumnIndex(DBHelper.MPIN)));
            }
        }catch (Exception e)
        {

        }finally {
            crs.close();
            db.close();
        }

    }


    // To retrieve Notifications from database
    public static ArrayList retrieveFromDatabase(Context context, DBHelper dbHelper) {
        dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Notification notification;
        String str = "Unread";
        Cursor crs = null;
        ArrayList<Notification> notificationArrayList = new ArrayList<>();

        try {
            crs = db.rawQuery("select DISTINCT " + DBHelper.UID + "," + DBHelper.MESSAGE + "," + DBHelper.READ_STATUS + "," + DBHelper.ON_DATE
                    + " from " + DBHelper.TABLE_NAME_NOTIFICATION + " where " + DBHelper.READ_STATUS + " = '" + str + "'", null);

            while (crs.moveToNext()) {
                String mUID = crs.getString(crs.getColumnIndex(DBHelper.UID));
                String mMessage = crs.getString(crs.getColumnIndex(DBHelper.MESSAGE));
                String mDate = crs.getString(crs.getColumnIndex(DBHelper.ON_DATE));
                String mReadStatus = crs.getString(crs.getColumnIndex(DBHelper.READ_STATUS));

                notification = new Notification(mUID, mMessage, mDate, mReadStatus);
                notificationArrayList.add(notification);
            }
        }catch (Exception e)
        {

        }finally {
            crs.close();
            db.close();
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




    public static void onCoachMark(final Context context, final int[] coachMarks){

        final Dialog dialog = new Dialog(context, R.style.WalkthroughTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.coach_mark);
        dialog.setCanceledOnTouchOutside(true);
        //for dismissing anywhere you touch
        View masterView = dialog.findViewById(R.id.coach_mark_master_view);
        final ImageView imgCoach = (ImageView) dialog.findViewById(R.id.coach_marks_image);
        Glide.with(context).load(coachMarks[0]).into(imgCoach);

        masterView.setOnClickListener(new View.OnClickListener() {
            int i = 1;
            @Override
            public void onClick(View view) {
                if (i < coachMarks.length)
                {
                    Glide.with(context).load(coachMarks[i]).into(imgCoach);
                    i++;
                }else {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public static boolean isRooted(){
        //get build info
        String buildTags = android.os.Build.TAGS;
        if(buildTags != null && buildTags.contains("test-keys")){
            return true;
        }

        // check if /system/app/Superuser.apk is present
        try{
            File file = new File("/system/app/Superuser.apk");
            if(file.exists())
            {
                return true;
            }
        }catch (Exception e)
        {
            //ignore
        }

        //try executing commands
        return canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
    }

    private static boolean canExecuteCommand(String command)
    {
        try{
            int exitValue = Runtime.getRuntime().exec(command).waitFor();
            return exitValue == 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
