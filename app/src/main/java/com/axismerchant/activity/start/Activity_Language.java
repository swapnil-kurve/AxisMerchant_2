package com.axismerchant.activity.start;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.AppActivity;
import com.axismerchant.classes.Constants;

import java.util.Locale;

public class Activity_Language extends AppActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    RadioGroup rdGrpLanguage;
    String currentLanguage = "en";
    Configuration config;
    TextView txtProceed, txtLabel, txtTitle;
    int fromHome = 0; // 0 for No and 1 for yes
    Typeface kannadaFont,teluguFont, hindiFont, bengaliFont, tamilFont;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CustomizedExceptionHandler("/mnt/sdcard/"));

        setContentView(R.layout.activity_language);

        /**
         * Initialize the fonts.
         */
        Initialize();


        txtProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fromHome == 0) {
                    Intent intent = new Intent(Activity_Language.this, Activity_Main.class);
                    intent.putExtra("EntryType", "SignUp");
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(Activity_Language.this, Activity_Home.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

            }
        });

    }

    private void Initialize() {
        hindiFont = Typeface.createFromAsset(getAssets(), "fonts/mangal.ttf");
        tamilFont = Typeface.createFromAsset(getAssets(), "fonts/tamil.TTF");
        bengaliFont = Typeface.createFromAsset(getAssets(), "fonts/bengali.TTF");
        teluguFont = Typeface.createFromAsset(getAssets(), "fonts/telugu.TTF");
        kannadaFont = Typeface.createFromAsset(getAssets(), "fonts/kannada.TTF");

        rdGrpLanguage = (RadioGroup) findViewById(R.id.radioGrpLangauage);
        rdGrpLanguage.setOnCheckedChangeListener(this);

        txtProceed = (TextView) findViewById(R.id.txtProceed);
        txtLabel = (TextView) findViewById(R.id.txtLabel);
        txtTitle = (TextView) findViewById(R.id.txtTitle);

        ImageView imgBack = (ImageView) findViewById(R.id.imgBack);

        imgBack.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey("FromHome"))
        {
            fromHome = 1;
            imgBack.setVisibility(View.VISIBLE);
            View mToolbar = findViewById(R.id.toolbar);
            mToolbar.setPadding(0,0,0,0);
        }else {
            checkLoginStatus();
        }

        SharedPreferences preferences = getSharedPreferences(Constants.LanguageData, MODE_PRIVATE);
        anyLang = preferences.getInt("Selected_Language", 0);
        switch (anyLang) {
            case 0:
                ((RadioButton) findViewById(R.id.rdEnglish)).setChecked(true);
                break;
            case 1:
                ((RadioButton) findViewById(R.id.rdHindi)).setChecked(true);
                break;
            case 2:
                ((RadioButton) findViewById(R.id.rdTamil)).setChecked(true);
                break;
            case 3:
                ((RadioButton) findViewById(R.id.rdTelugu)).setChecked(true);
                break;
            case 4:
                ((RadioButton) findViewById(R.id.rdKannada)).setChecked(true);
                break;
            case 5:
                ((RadioButton) findViewById(R.id.rdBengali)).setChecked(true);
                break;
        }
    }

    private void checkLoginStatus() {
        SharedPreferences preferences = getSharedPreferences(Constants.LoginPref, Context.MODE_PRIVATE);
        if(preferences.contains("LoggedIn"))
        {
            String mSignUpStatus = preferences.getString("LoggedIn","false");
            if(mSignUpStatus.equals("true")) {

                String status = preferences.getString("KeepLoggedIn","false");
                if(status.equals("true")) {
                    startActivity(new Intent(this, Activity_Home.class));
                    finish();
                }else {
                    Intent intent = new Intent(this, Activity_Main.class);
                    intent.putExtra("EntryType","SignIn");
                    startActivity(intent);
                    finish();
                }
            }
        }else{
            fromHome = 0;
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.imgBack:
                onBackPressed();
                break;

        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        switch (i)
        {
            case R.id.rdEnglish:
                anyLang = 0;
                currentLanguage = "en";

                Locale locale = new Locale(currentLanguage);
                Locale.setDefault(locale);

                config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());

                txtProceed.setText(getString(R.string.proceed));
                txtProceed.setTypeface(Typeface.DEFAULT);
                break;

            case R.id.rdHindi:
                anyLang = 1;
                currentLanguage = "hi";
                locale = new Locale(currentLanguage);
                Locale.setDefault(locale);

                config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());

                txtProceed.setText(getString(R.string.proceed));
                txtProceed.setTypeface(hindiFont);
                break;

            case R.id.rdTamil:
                anyLang = 2;
                currentLanguage = "ta";

                locale = new Locale(currentLanguage);
                Locale.setDefault(locale);

                config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());

                txtProceed.setText(getString(R.string.proceed));
                txtProceed.setTypeface(tamilFont);

                break;

            case R.id.rdTelugu:
                anyLang = 3;
                currentLanguage = "te";

                locale = new Locale(currentLanguage);
                Locale.setDefault(locale);

                config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());

                txtProceed.setText(getString(R.string.proceed));
                txtProceed.setTypeface(teluguFont);
                break;

            case R.id.rdKannada:
                anyLang = 4;
                currentLanguage = "kn";

                locale = new Locale(currentLanguage);
                Locale.setDefault(locale);

                config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());

                txtProceed.setText(getString(R.string.proceed));
                txtProceed.setTypeface(kannadaFont);
                break;

            case R.id.rdBengali:
                anyLang = 5;
                currentLanguage = "bn";

                locale = new Locale(currentLanguage);
                Locale.setDefault(locale);

                config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());

                txtProceed.setText(getString(R.string.proceed));
                txtProceed.setTypeface(bengaliFont);
                break;


        }

        SharedPreferences preferences = getSharedPreferences(Constants.LanguageData, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("Selected_Language", anyLang);
        editor.putString("CurrentLanguage", currentLanguage);
        editor.apply();
    }
}
