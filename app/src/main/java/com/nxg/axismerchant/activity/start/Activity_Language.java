package com.nxg.axismerchant.activity.start;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.AppActivity;
import com.nxg.axismerchant.classes.Constants;

public class Activity_Language extends AppActivity {

//    RadioButton rdEnglish, rdHindi, rdTamil, rdTelugu, rdKannada, rdBengali;
    RadioGroup rdGrpLanguage;
    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_language);

        rdGrpLanguage = (RadioGroup) findViewById(R.id.radioGrpLangauage);

        TextView txtProceed = (TextView) findViewById(R.id.txtProceed);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey("FromHome"))
        {
            i = 1;

        }else {
            checkLoginStatus();
        }

        txtProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectLanguage();
                if(anyLang == 0)
                    Constants.showToast(Activity_Language.this, "Please select language");
                else {
                    if(i==0){
                        Intent intent = new Intent(Activity_Language.this, Activity_Main.class);
                        intent.putExtra("EntryType","SignUp");
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(Activity_Language.this, Activity_Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

    }

    private void selectLanguage() {
        int selectedId = rdGrpLanguage.getCheckedRadioButtonId();

        SharedPreferences preferences = getSharedPreferences(Constants.LanguageData, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        switch (selectedId)
        {
            case R.id.rdEnglish:
                anyLang = 1;
                break;

            case R.id.rdHindi:
                anyLang = 2;
                break;

            case R.id.rdTamil:
                anyLang = 3;
                break;

            case R.id.rdTelugu:
                anyLang = 4;
                break;

            case R.id.rdKannada:
                anyLang = 5;
                break;

            case R.id.rdBengali:
                anyLang = 6;
                break;

        }

        editor.putInt("Selected_Language", anyLang);
        editor.apply();


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
            i = 0;
        }

    }

}
