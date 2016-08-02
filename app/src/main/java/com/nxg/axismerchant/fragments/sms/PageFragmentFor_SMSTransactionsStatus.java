package com.nxg.axismerchant.fragments.sms;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.sms.Activity_TransactionStatusDetails;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.SMSPayStatus;
import com.nxg.axismerchant.database.DBHelper;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageFragmentFor_SMSTransactionsStatus extends Fragment implements AdapterView.OnItemClickListener {

    public static final String ARG_OBJECT = "object";
    ListView listTransactions;
    TextView txtMessage;
    ArrayList<SMSPayStatus> statusArrayList;
    SMSPayStatus smsPayStatus;
    DBHelper dbHelper;
    DataAdapter dataAdapter;
    EncryptDecryptRegister encryptDecryptRegister;
    EncryptDecrypt encryptDecrypt;
    private String[] arrTitle;
    private String pageTitle, MID,MOBILE;
    int pageNO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.page_fragment_for_transactions, container, false);
        Bundle args = getArguments();

        getInitialize(view);

        pageNO = args.getInt(ARG_OBJECT, 0);

        if(pageNO >0)
        {
            pageTitle = arrTitle[pageNO];
        }

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        Constants.getIMEI(getActivity());
        Constants.retrieveMPINFromDatabase(getActivity());

        retrieveFromDatabase();

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void getInitialize(View view) {
        listTransactions = (ListView) view.findViewById(R.id.listTransactions);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        statusArrayList = new ArrayList<>();

        listTransactions.setOnItemClickListener(this);

        arrTitle = getResources().getStringArray(R.array.sms_pay_status_arr);

        txtMessage = (TextView) view.findViewById(R.id.txtMessage);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.listTransactions) {
            Intent intent = new Intent(getActivity(), Activity_TransactionStatusDetails.class);
            intent.putExtra("XnID", statusArrayList.get(position).getInvoiceNum());
            startActivity(intent);
        }
    }


    private void retrieveFromDatabase() {
        dbHelper = new DBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor crs;
        if(pageNO == 0) {
            crs = db.rawQuery("select DISTINCT " + DBHelper.UID + "," + DBHelper.CUST_MOBILE + ","
                    + DBHelper.AMOUNT + "," + DBHelper.REMARK + ","
                    + DBHelper.INVOICE_NO + "," + DBHelper.STATUS + "," + DBHelper.TRANS_DATE+ "," + DBHelper.IS_REFUND+ " from " + DBHelper.TABLE_NAME_E_PAYMENT +" order by CAST("+DBHelper.INVOICE_NO+" AS Integer) desc", null);
        }else {
            crs = db.rawQuery("select DISTINCT " + DBHelper.UID + "," + DBHelper.CUST_MOBILE + ","
                    + DBHelper.AMOUNT + "," + DBHelper.REMARK + ","
                    + DBHelper.INVOICE_NO + "," + DBHelper.STATUS + "," + DBHelper.TRANS_DATE+"," + DBHelper.IS_REFUND+ " from " + DBHelper.TABLE_NAME_E_PAYMENT+
                    " where " + DBHelper.STATUS+" = ?"+" order by CAST("+DBHelper.INVOICE_NO+" AS Integer) desc", new String[] {pageTitle});
        }

        statusArrayList.clear();
        while (crs.moveToNext()) {
            String mUID = crs.getString(crs.getColumnIndex(DBHelper.UID));
            String mCustMobile = crs.getString(crs.getColumnIndex(DBHelper.CUST_MOBILE));
            String mAmount = crs.getString(crs.getColumnIndex(DBHelper.AMOUNT));
            String mRemark = crs.getString(crs.getColumnIndex(DBHelper.REMARK));
            String mInvoiceNumber = crs.getString(crs.getColumnIndex(DBHelper.INVOICE_NO));
            String mStatus = crs.getString(crs.getColumnIndex(DBHelper.STATUS));
            String mTransDate = crs.getString(crs.getColumnIndex(DBHelper.TRANS_DATE));
            String mIsRefund = crs.getString(crs.getColumnIndex(DBHelper.IS_REFUND));

            smsPayStatus = new SMSPayStatus(mUID,mCustMobile, mAmount, mInvoiceNumber, mStatus, mRemark,mIsRefund,mTransDate);
            statusArrayList.add(smsPayStatus);
        }

        dataAdapter = new DataAdapter(getActivity(), statusArrayList);
        listTransactions.setAdapter(dataAdapter);

        dataAdapter.notifyDataSetChanged();
    }


    private class DataAdapter extends BaseAdapter {
        ArrayList<SMSPayStatus> statusArrayList;
        Context context;

        public DataAdapter(Context context, ArrayList<SMSPayStatus> statusArrayList) {
            this.context = context;
            this.statusArrayList = statusArrayList;
        }

        @Override
        public int getCount() {
            return statusArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return statusArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_row_for_sms_pay_status, null);

            TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
            TextView txtXnID = (TextView) view.findViewById(R.id.txtTransactionID);
            TextView txtStatus = (TextView) view.findViewById(R.id.txtStatus);
            TextView txtAmount = (TextView) view.findViewById(R.id.txtAmount);
            TextView txtMobile = (TextView) view.findViewById(R.id.txtMobileNo);
            ImageView imgStatusSMS = (ImageView) view.findViewById(R.id.imgStatusSMS);
            View lyRefundLayout = view.findViewById(R.id.refundLayout);

            try {
                if(statusArrayList.get(position).getCustMobile().length()>10){
                    String mob = statusArrayList.get(position).getCustMobile();
                    txtMobile.setText(mob.substring(mob.length()-10,mob.length()));
                }else {
                    txtMobile.setText(statusArrayList.get(position).getCustMobile());
                }
                txtAmount.setText(getResources().getString(R.string.Rs) + statusArrayList.get(position).getAmount());
                txtStatus.setText(statusArrayList.get(position).getStatus());
                txtDate.setText(statusArrayList.get(position).getTransDate().split("\\s+")[0]);
//                txtDate.setText(statusArrayList.get(position).getIsRefund());
                txtXnID.setText(statusArrayList.get(position).getInvoiceNum());

                if (statusArrayList.get(position).getStatus().equals("Pending")) {
                    imgStatusSMS.setImageResource(R.mipmap.pending);
                    txtStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                } else if (statusArrayList.get(position).getStatus().equals("Success")) {
                    imgStatusSMS.setImageResource(R.mipmap.successfull);
                    txtStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                    if(statusArrayList.get(position).getIsRefund().equalsIgnoreCase("1"))
                        lyRefundLayout.setVisibility(View.GONE);
                    else
                        lyRefundLayout.setVisibility(View.VISIBLE);

                } else {
                    imgStatusSMS.setImageResource(R.mipmap.fail);
                    txtStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }

            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return view;
        }
    }
}
