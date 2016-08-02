package com.nxg.axismerchant.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.database.DBHelper;

import java.util.ArrayList;

public class Activity_Notification extends AppCompatActivity implements View.OnClickListener {

    ListView listNotify;
    ArrayList<Notification> notificationArrayList;
    NotificationAdapter adapter;
    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        listNotify = (ListView) findViewById(R.id.listNotification);
        notificationArrayList = new ArrayList<>();

        dbHelper = new DBHelper(this);

        notificationArrayList = retrieveFromDatabase(this, dbHelper);

        adapter = new NotificationAdapter(this, notificationArrayList);
        listNotify.setAdapter(adapter);

        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);

        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);

        UpdateIntoNotification();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgProfile:
                startActivity(new Intent(this, Activity_UserProfile.class));

        }
    }


    private class NotificationAdapter extends BaseAdapter
    {
        Context context;
        ArrayList<Notification> notificationArrayList;

        public NotificationAdapter(Activity_Notification activity_notification, ArrayList<Notification> notificationArrayList) {
            context = activity_notification;
            this.notificationArrayList = notificationArrayList;
        }

        @Override
        public int getCount() {
            return notificationArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return notificationArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.custom_row_for_notification, null);

            TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMsg);

            txtDate.setText(notificationArrayList.get(position).getDate());
            txtMessage.setText(notificationArrayList.get(position).getMessage());

            return convertView;
        }
    }

    private void UpdateIntoNotification() {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.READ_STATUS, "Read");

        long id = db.update(DBHelper.TABLE_NAME_NOTIFICATION,values,null, null);

        Log.e("Update Notifications",""+id);
    }

    private ArrayList retrieveFromDatabase(Context context, DBHelper dbHelper) {
        dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Notification notification;
        String str = "Unread";
        ArrayList<Notification> notificationArrayList = new ArrayList<>();

        Cursor crs = db.rawQuery("select DISTINCT "+ DBHelper.UID + ","+ DBHelper.MESSAGE +"," + DBHelper.READ_STATUS +","+ DBHelper.ON_DATE
                + " from " + DBHelper.TABLE_NAME_NOTIFICATION +" order by CAST("+DBHelper.UID+" AS Integer) desc", null);

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
}
