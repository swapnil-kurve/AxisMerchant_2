package com.nxg.axismerchant.activity.offers;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.Activity_Notification;
import com.nxg.axismerchant.activity.start.Activity_UserProfile;
import com.nxg.axismerchant.classes.Constants;
import com.nxg.axismerchant.classes.EncryptDecrypt;
import com.nxg.axismerchant.classes.EncryptDecryptRegister;
import com.nxg.axismerchant.classes.HTTPUtils;
import com.nxg.axismerchant.classes.Notification;
import com.nxg.axismerchant.classes.Promotions;
import com.nxg.axismerchant.database.DBHelper;

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
import java.util.List;

public class Activity_OffersNotices extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView listOffers;
    private OffersAdapter offersAdapter;
    private String[] tabs ;
    DBHelper dbHelper;
    ArrayList<Promotions> promotionsArrayList;
    Promotions promotions;
    TextView txtEmptyMsg;
    String MID,MOBILE;
    SQLiteDatabase mDatabase;
    EncryptDecrypt encryptDecrypt;
    EncryptDecryptRegister encryptDecryptRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers_notices);

        listOffers = (ListView) findViewById(R.id.listOffers);
        txtEmptyMsg = (TextView) findViewById(R.id.txtEmptyMsg);

        listOffers.setOnItemClickListener(this);

        encryptDecrypt = new EncryptDecrypt();
        encryptDecryptRegister = new EncryptDecryptRegister();

        tabs = getResources().getStringArray(R.array.offers_notices);

        promotionsArrayList = new ArrayList<>();

        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        ImageView imgProfile = (ImageView) findViewById(R.id.imgProfile);
        ImageView imgNotification = (ImageView) findViewById(R.id.imgNotification);
        imgBack.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        imgNotification.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        MID = preferences.getString("MerchantID","0");
        MOBILE = preferences.getString("MobileNum","0");
        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);

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

        if (!isTableExists()) {
            if (Constants.isNetworkConnectionAvailable(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new GetLatestPromotions().executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, Constants.DEMO_SERVICE + "getLatestPramotions", MID, MOBILE);//arrTitle[pageNO]);
                } else {
                    new GetLatestPromotions().execute(Constants.DEMO_SERVICE + "getLatestPramotions", MID, MOBILE);//arrTitle[pageNO]);

                }
            } else {
                Constants.showToast(this, getString(R.string.no_internet));
            }

        } else {

            retrieveFromDatabase();
            if(promotionsArrayList.size() > 0)
            {
                offersAdapter = new OffersAdapter(this,promotionsArrayList);
                listOffers.setAdapter(offersAdapter);
            }else
            {
                listOffers.setVisibility(View.GONE);
                txtEmptyMsg.setVisibility(View.VISIBLE);
            }
        }

        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(promotionsArrayList.get(position).getmWithOption().equalsIgnoreCase("Yes")) {
            UpdateReadStatusIntoPromotionTable(promotionsArrayList.get(position).getmPromotionID());
            Intent intent = new Intent(this, Activity_OfferDetails.class);
            intent.putExtra("PromotionId", promotionsArrayList.get(position).getmPromotionID());
            intent.putExtra("PromoImg", promotionsArrayList.get(position).getmImgPath());
            startActivity(intent);
        }
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
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
        }
    }


    public boolean isTableExists() {
        dbHelper = new DBHelper(this);
        mDatabase = dbHelper.getReadableDatabase();

        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = dbHelper.getReadableDatabase();
        }

        if (!mDatabase.isReadOnly()) {
            mDatabase.close();
            mDatabase = dbHelper.getReadableDatabase();
        }

        Cursor cursor = mDatabase.rawQuery("select * from "+DBHelper.TABLE_NAME_PROMOTIONS, null);
        if (cursor != null) {
            Log.e("Data Count", "" + cursor.getCount());
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }



    private class OffersAdapter extends BaseAdapter
    {
        private Context context;
        private ArrayList<Promotions> promotionsArrayList;
        public OffersAdapter(Activity_OffersNotices activity_offersNotices, ArrayList<Promotions> promotionsArrayList) {
            context = activity_offersNotices;
            this.promotionsArrayList = promotionsArrayList;
        }

        @Override
        public int getCount() {
            return promotionsArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return promotionsArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_row_for_offers,null);
            TextView txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
            TextView txtSecondaryText = (TextView) convertView.findViewById(R.id.txtSecondaryText);
            ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);

            txtTitle.setText(promotionsArrayList.get(position).getmSubTitle());
            txtSecondaryText.setText(promotionsArrayList.get(position).getmMessage());

            if(promotionsArrayList.get(position).getmWithOption().equalsIgnoreCase("Yes")){
                imgIcon.setImageResource(R.mipmap.gift);
            }else
            {
                imgIcon.setImageResource(R.mipmap.offers);
            }

            if(promotionsArrayList.get(position).getmReadStatus().equalsIgnoreCase("Unread"))
            {
                txtTitle.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Futura_LightBold.ttf"));
//                convertView.setBackgroundColor(Color.WHITE);
            }else
            {
                txtTitle.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/futura_light.TTF"));
//                convertView.setBackgroundColor(getResources().getColor(R.color.metal_gray));
            }

            return convertView;
        }
    }



    private void retrieveFromDatabase() {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if(promotionsArrayList.size()>0)
            promotionsArrayList.clear();

        Cursor crs = db.rawQuery("select DISTINCT "+ DBHelper.UID + ","+ DBHelper.PROMOTION_ID +"," + DBHelper.TITLE +","+ DBHelper.SUB_TITLE +","
                + DBHelper.MESSAGE +"," + DBHelper.IMG_URL +","
                + DBHelper.PROMOTION_TYPE+"," + DBHelper.WITH_OPTION +"," + DBHelper.READ_STATUS +"," +DBHelper.STATUS + " from " + DBHelper.TABLE_NAME_PROMOTIONS
                +" order by CAST("+DBHelper.UID+" AS Integer) desc", null);

        while (crs.moveToNext()) {
            String mUID = crs.getString(crs.getColumnIndex(DBHelper.UID));
            String mPromotionID = crs.getString(crs.getColumnIndex(DBHelper.PROMOTION_ID));
            String mTitle = crs.getString(crs.getColumnIndex(DBHelper.TITLE));
            String mSubTitle = crs.getString(crs.getColumnIndex(DBHelper.SUB_TITLE));
            String mMessage = crs.getString(crs.getColumnIndex(DBHelper.MESSAGE));
            String mImgUrl = crs.getString(crs.getColumnIndex(DBHelper.IMG_URL));
            String mPromotionType = crs.getString(crs.getColumnIndex(DBHelper.PROMOTION_TYPE));
            String mWithOption = crs.getString(crs.getColumnIndex(DBHelper.WITH_OPTION));
            String mStatus = crs.getString(crs.getColumnIndex(DBHelper.STATUS));
            String mReadStatus = crs.getString(crs.getColumnIndex(DBHelper.READ_STATUS));

            promotions = new Promotions(mUID,mTitle,mMessage,mSubTitle,mImgUrl,mPromotionType,mPromotionID,mWithOption,mStatus,mReadStatus,"");
            promotionsArrayList.add(promotions);
        }
    }

    private void UpdateReadStatusIntoPromotionTable(String promotionID) {
        dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.READ_STATUS, "Read");

        long id = db.update(DBHelper.TABLE_NAME_PROMOTIONS,values,DBHelper.PROMOTION_ID +" = "+promotionID, null);
        Log.v("id", String.valueOf(id));
    }


    private class GetLatestPromotions extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Activity_OffersNotices.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            String str = "";
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

            try{
                if(!data.equals("")) {
                    JSONArray jsonArray = new JSONArray(data);
                    JSONObject object = jsonArray.getJSONObject(0);
                    JSONArray rowsResponse = object.getJSONArray("rowsResponse");
                    JSONObject obj = rowsResponse.getJSONObject(0);
                    String result = obj.optString("result");
                    result = encryptDecryptRegister.decrypt(result);
                    if (result.equals("Success")) {
                        JSONObject object2 = jsonArray.getJSONObject(1);
                        JSONArray getLatestMerchantUserTrans = object2.getJSONArray("getLatestPramotions");

                        for (int i = 0; i < getLatestMerchantUserTrans.length(); i++) {
                            JSONObject object1 = getLatestMerchantUserTrans.getJSONObject(i);
                            String promotionId = object1.optString("promotionId");
                            String promotionText = object1.optString("promotionText");
                            String promotionImage = object1.optString("promotionImage");
                            String onDate = object1.optString("onDate");
                            String withOptions = object1.optString("withOptions");
                            String mProduct = object1.optString("mProduct");
                            String merCategory = object1.optString("merCategory");
                            String promotionImageurl = object1.optString("promotionImageurl");
                            String presponse = object1.optString("presponse");
                            String title = object1.optString("title");
                            String SubTitle = object1.optString("SubTitle");
                            String promotionType = object1.optString("promotionType");
                            String message = object1.optString("message");
                            String promotype = object1.optString("promotype");

                            promotionId = encryptDecrypt.decrypt(promotionId);
                            promotionText = encryptDecrypt.decrypt(promotionText);
                            promotionImage = encryptDecrypt.decrypt(promotionImage);
                            onDate = encryptDecrypt.decrypt(onDate);
                            withOptions = encryptDecrypt.decrypt(withOptions);
                            mProduct = encryptDecrypt.decrypt(mProduct);
                            merCategory = encryptDecrypt.decrypt(merCategory);
                            promotionImageurl = encryptDecrypt.decrypt(promotionImageurl);
                            presponse = encryptDecrypt.decrypt(presponse);
                            title = encryptDecrypt.decrypt(title);
                            SubTitle = encryptDecrypt.decrypt(SubTitle);
                            promotionType = encryptDecrypt.decrypt(promotionType);
                            message = encryptDecrypt.decrypt(message);
                            promotype = encryptDecrypt.decrypt(promotype);

                            InsertIntoPromotionTable(title,message,SubTitle,promotionImageurl,promotionType,promotionId,withOptions);
                        }

                        retrieveFromDatabase();
                        if(promotionsArrayList.size() > 0)
                        {
                            offersAdapter = new OffersAdapter(Activity_OffersNotices.this,promotionsArrayList);
                            listOffers.setAdapter(offersAdapter);
                        }else
                        {
                            listOffers.setVisibility(View.GONE);
                            txtEmptyMsg.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Constants.showToast(Activity_OffersNotices.this, getString(R.string.no_details));

                    }
                    progressDialog.dismiss();
                }
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }

        }
    }


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
}
