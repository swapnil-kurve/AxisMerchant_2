package com.axismerchant.activity.start;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.axismerchant.R;
import com.axismerchant.activity.Activity_Notification;
import com.axismerchant.classes.Constants;
import com.axismerchant.fragments.profile.BusinessDetailsFragment;
import com.axismerchant.fragments.profile.SubUserFragment;

import java.util.ArrayList;
import java.util.List;

public class Activity_UserProfile extends AppCompatActivity implements View.OnClickListener {
    ImageView imgUserProfile;
    SharedPreferences preferences;
    private final static int RESULT_SELECT_IMAGE = 100;
    private static final String TAG = "GalleryUtil";
    String  picturePath;
    Intent returnFromGalleryIntent;
    private int flag = 0;
    private final static int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 111;
    SubUserFragment userFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getInitialize();

        changeToBusinessDetails();

        /**
         * Run time permissions for Android M
         */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int hasReadExternalPermission = checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE);
            int hasWriteExternalPermission = checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE);

            List<String> permissions = new ArrayList<>();

            if( hasReadExternalPermission != PackageManager.PERMISSION_GRANTED ) {
                permissions.add( Manifest.permission.READ_EXTERNAL_STORAGE );
            }

            if( hasWriteExternalPermission != PackageManager.PERMISSION_GRANTED ) {
                permissions.add( Manifest.permission.WRITE_EXTERNAL_STORAGE );
            }


            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_SOME_FEATURES_PERMISSIONS);
            }

        }
        /************************/

        preferences = getSharedPreferences(Constants.ProfileInfo, Context.MODE_PRIVATE);
        if(preferences.contains("ProfileImage"))
        {
            String picturePath = preferences.getString("ProfileImage",null);

            Glide.with(this).load(picturePath).asBitmap().centerCrop().into(new BitmapImageViewTarget(imgUserProfile) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getResources(), resource);
                    circularBitmapDrawable.setCornerRadius(160);
                    imgUserProfile.setImageDrawable(circularBitmapDrawable);
                }
            });
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
    protected void onResume() {
        super.onResume();
        Constants.retrieveMPINFromDatabase(this);
        Constants.getIMEI(this);
    }

    private void getInitialize() {
        View layoutBusinessDetails = findViewById(R.id.layoutBusinessDetail);
        View layoutSubUser = findViewById(R.id.layoutSubUser);
        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);
        imgUserProfile = (ImageView) findViewById(R.id.imgUserProfile);

        imgUserProfile.setOnClickListener(this);
        layoutBusinessDetails.setOnClickListener(this);
        imgBack.setOnClickListener(this);

        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        if(preferences.getString("isAdmin","False").equals("True")){
            layoutSubUser.setOnClickListener(this);
        }else{
            (findViewById(R.id.layoutSubUser)).setVisibility(View.GONE);
        }

    }

    private void changeToBusinessDetails()
    {
        (findViewById(R.id.viewBusinessDetails)).setBackgroundColor(Color.WHITE);
        (findViewById(R.id.viewSubUser)).setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        BusinessDetailsFragment detailsFragment = new BusinessDetailsFragment();
        getFragmentManager().beginTransaction().replace(R.id.container,detailsFragment).commit();
    }

    private void changeToSubUser()
    {
        (findViewById(R.id.viewSubUser)).setBackgroundColor(Color.WHITE);
        (findViewById(R.id.viewBusinessDetails)).setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        userFragment = new SubUserFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, userFragment).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
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

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.layoutBusinessDetail:
                changeToBusinessDetails();
                flag = 0;
                break;

            case R.id.layoutSubUser:
                changeToSubUser();
                flag = 1;
                break;

            case R.id.imgBack:
                onBackPressed();
                break;

            case R.id.imgUserProfile:
                try{
                    //Pick Image From Gallery
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_SELECT_IMAGE);

                }catch(Exception e){

                }
                break;

            case R.id.imgNotification:
                startActivity(new Intent(this, Activity_Notification.class));
                break;
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case RESULT_SELECT_IMAGE:

                if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                    try{
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        picturePath = cursor.getString(columnIndex);
                        cursor.close();

                        //return Image Path to the Main Activity
                        returnFromGalleryIntent = new Intent();
                        returnFromGalleryIntent.putExtra("picturePath", picturePath);
                        setResult(RESULT_OK, returnFromGalleryIntent);

                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                        imgUserProfile.setImageBitmap(bitmap);
                        imgUserProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imgUserProfile.setBackgroundResource(R.drawable.circular_textview);

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("ProfileImage",picturePath);
                        editor.apply();

                        Glide.with(this).load(picturePath).asBitmap().centerCrop().into(new BitmapImageViewTarget(imgUserProfile) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                                circularBitmapDrawable.setCornerRadius(160);
                                imgUserProfile.setImageDrawable(circularBitmapDrawable);
                            }
                        });

                    }catch(Exception e){
                        Intent returnFromGalleryIntent = new Intent();
                        setResult(RESULT_CANCELED, returnFromGalleryIntent);
                    }
                }else{
                    Log.i(TAG, "RESULT_CANCELED");
                    Intent returnFromGalleryIntent = new Intent();
                    setResult(RESULT_CANCELED, returnFromGalleryIntent);
                }
                break;

        }
    }

    @Override
    public void onBackPressed() {
        if(flag == 1){
            boolean groupsCollapsed = false;
            for (int i=0; i<userFragment.expandableListView.getCount(); ++i) {
                if (userFragment.expandableListView.isGroupExpanded(i)) {
                    userFragment.expandableListView.collapseGroup(i);
                    groupsCollapsed = true;
                }
            }

            // If no groups collapsed, call the default back button
            if (!groupsCollapsed) {
                changeToBusinessDetails();
                flag = 0;
            }

        }else {
            super.onBackPressed();
        }
    }
}
