package com.nxg.axismerchant.activity.start;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nxg.axismerchant.R;
import com.nxg.axismerchant.activity.AppActivity;
import com.nxg.axismerchant.classes.Constants;

public class Activity_Language extends AppActivity {

    RadioButton rdEnglish, rdHindi, rdTamil, rdTelugu, rdKannada, rdBengali;
    RadioGroup rdGrpLanguage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        rdGrpLanguage = (RadioGroup) findViewById(R.id.radioGrpLangauage);
        rdEnglish = (RadioButton) findViewById(R.id.rdEnglish);
        rdHindi = (RadioButton) findViewById(R.id.rdHindi);
        rdTamil = (RadioButton) findViewById(R.id.rdTamil);
        rdTelugu = (RadioButton) findViewById(R.id.rdTelugu);
        rdKannada = (RadioButton) findViewById(R.id.rdKannada);
        rdBengali = (RadioButton) findViewById(R.id.rdBengali);

        TextView txtProceed = (TextView) findViewById(R.id.txtProceed);

        txtProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                if(anyLang == 0)
                    Constants.showToast(Activity_Language.this, "Please select language");
                else {
                    startActivity(new Intent(Activity_Language.this, Activity_Main.class));
                    finish();
                }
            }
        });


    }

}
