package com.axismerchant.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Dell on 03-03-2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "AppData";
    public static final String TABLE_NAME_PROMOTIONS = "PromotionsTable";
    public static final String TABLE_NAME_E_PAYMENT = "EPayment";
    public static final String TABLE_NAME_MPIN = "mpin";
    public static final String TABLE_NAME_NOTIFICATION = "notification";
    public static final String TABLE_NAME_MIS_XN_REPORT = "TransactionReport";
    public static final String UID = "_id";
    public static final String PROMOTION_ID = "promo_id";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String PROMOTION_TYPE = "promo_type";
    public static final String IMG_URL = "img_url";
    public static final String SUB_TITLE = "sub_title";
    public static final String WITH_OPTION = "with_option";
    public static final String READ_STATUS = "read_status";
    public static final String STATUS = "status";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String ON_DATE = "on_date";
    public static final String IS_REFUND = "is_refund";
    public static final String TRANS_DATE = "trans_date";
    public static final String INVOICE_NO = "invoice_no";
    public static final String CUST_MOBILE = "cust_mobile";
    public static final String AMOUNT = "amount";
    public static final String REMARK = "remark";
    public static final String MIS_TR_TOTAL_Xn = "totalTransaction";
    public static final String MIS_TR_AVG_TCKT_SIZE = "avgTicketSize";
    public static final String MIS_TR_XN_VOLUME = "volume";
    public static final String MIS_TR_XN_DATE = "transDate";
    public static final String MIS_TR_TDATE = "tDate";
    public static final String MIS_TR_TTYPE = "tType";
    public static final String MPIN = "mpin";
    public static final String DROP_TABLE_MIS_XN_REPORT = "DROP TABLE IF EXISTS " + TABLE_NAME_MIS_XN_REPORT;
    private static final int DATABASE_VERSION = 2;
    private static final String CREATE_TABLE_PROMOTIONS = "CREATE TABLE "+ TABLE_NAME_PROMOTIONS +" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+PROMOTION_ID+" VARCHAR(255),"
            +TITLE+" VARCHAR(255),"+SUB_TITLE+" VARCHAR(255),"
            +MESSAGE+" VARCHAR(255),"+IMG_URL+" VARCHAR(255),"
            +PROMOTION_TYPE+" VARCHAR(255),"+WITH_OPTION+" VARCHAR(255), "+READ_STATUS+" VARCHAR(255), "+STATUS+" VARCHAR(255), "+ON_DATE+" VARCHAR(255));";
    private static final String CREATE_TABLE_E_PAYMENT = "CREATE TABLE "+ TABLE_NAME_E_PAYMENT +" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +CUST_MOBILE+" VARCHAR(255),"+AMOUNT+" VARCHAR(255),"+REMARK+" VARCHAR(255),"
            +INVOICE_NO+" VARCHAR(255), "+IS_FAVORITE+" VARCHAR(255), "+IS_REFUND+" VARCHAR(255), "+TRANS_DATE+" VARCHAR(255), "+STATUS+" VARCHAR(255));";
    private static final String CREATE_TABLE_MIS_TRANSACTION_REPORT = "CREATE TABLE "+ TABLE_NAME_MIS_XN_REPORT +" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +MIS_TR_TOTAL_Xn+" VARCHAR(255),"+MIS_TR_AVG_TCKT_SIZE+" VARCHAR(255),"+MIS_TR_XN_VOLUME+" VARCHAR(255),"
            +MIS_TR_XN_DATE+" VARCHAR(255), "+MIS_TR_TDATE+" VARCHAR(255), "+MIS_TR_TTYPE+" VARCHAR(255));";
    private static final String CREATE_TABLE_MPIN = "CREATE TABLE "+ TABLE_NAME_MPIN +" ("+MPIN+" VARCHAR(255));";

    private static final String CREATE_TABLE_NOTIFICATION = "CREATE TABLE "+ TABLE_NAME_NOTIFICATION +" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +MESSAGE+" VARCHAR(255), "+READ_STATUS+" VARCHAR(255), "+PROMOTION_ID+" VARCHAR(255), "+ON_DATE+" VARCHAR(255));";

    private static final String DROP_TABLE_PROMOTIONS = "DROP TABLE IF EXISTS "+TABLE_NAME_PROMOTIONS;
    private static final String DROP_TABLE_E_PAYMENT = "DROP TABLE IF EXISTS "+TABLE_NAME_E_PAYMENT;
    private static final String DROP_TABLE_MPIN = "DROP TABLE IF EXISTS "+TABLE_NAME_MPIN;
    private static final String DROP_TABLE_NOTIFICATION = "DROP TABLE IF EXISTS "+TABLE_NAME_NOTIFICATION;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_PROMOTIONS);
            db.execSQL(CREATE_TABLE_E_PAYMENT);
            db.execSQL(CREATE_TABLE_MPIN);
            db.execSQL(CREATE_TABLE_NOTIFICATION);
            db.execSQL(CREATE_TABLE_MIS_TRANSACTION_REPORT);
        }catch (Exception e)
        {
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try{
            db.execSQL(DROP_TABLE_PROMOTIONS);
            db.execSQL(DROP_TABLE_E_PAYMENT);
            db.execSQL(DROP_TABLE_MPIN);
            db.execSQL(DROP_TABLE_NOTIFICATION);
            db.execSQL(DROP_TABLE_MIS_XN_REPORT);
            onCreate(db);
        }catch (Exception e)
        {
        }
    }
}
