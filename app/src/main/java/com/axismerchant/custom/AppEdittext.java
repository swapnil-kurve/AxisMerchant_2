package com.axismerchant.custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.axismerchant.R;
import com.axismerchant.activity.AppActivity;
import com.axismerchant.classes.Constants;

import java.util.Locale;

/**
 * Created by user on 9/9/16.
 */
public class AppEdittext extends EditText {
    Configuration config;
    Locale locale;
    String currentLanguage;

    public AppEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);

        try {
            AppActivity activity = (AppActivity) context;
            SharedPreferences preferences = activity.getSharedPreferences(Constants.LanguageData, Context.MODE_PRIVATE);
            int languageSelected = preferences.getInt("Selected_Language",0);

            currentLanguage = preferences.getString("CurrentLanguage","en");

            locale = new Locale(currentLanguage);
            Locale.setDefault(locale);
            config = new Configuration();
            config.locale = locale;
            activity.getBaseContext().getResources().updateConfiguration(config,
                    activity.getBaseContext().getResources().getDisplayMetrics());

            switch (languageSelected)
            {
                case 2:
                    this.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/mangal.ttf"));
                    break;

                case 3:
                    this.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/tamil.TTF"));
                    break;

                case 4:
                    this.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/telugu.TTF"));
                    break;

                case 5:
                    this.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/kannada.TTF"));
                    break;

                case 6:
                    this.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/bengali.TTF"));
                    break;
            }

           /* TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppEdittext);
            final int N = a.getIndexCount();
            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);
                switch (attr) {

                    case R.styleable.AppEdittext_hindi_hint_text:
                        if (languageSelected == 2) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.mangal_font)));
                            setHint(text);
                        }
                        break;

                    case R.styleable.AppEdittext_tamil_hint_text:
                        if (languageSelected == 3) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.tamil_font)));
                            setHint(text);
                        }
                        break;

                    case R.styleable.AppEdittext_telugu_hint_text:
                        if (languageSelected == 4) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.telugu_font)));
                            setHint(text);
                        }
                        break;

                    case R.styleable.AppEdittext_kannada_hint_text:
                        if (languageSelected == 5) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.kannada_font)));
                            setHint(text);
                        }
                        break;

                    case R.styleable.AppEdittext_bengali_hint_text:
                        if (languageSelected == 6) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.bengali_font)));
                            setHint(text);
                        }
                        break;


                    case R.styleable.AppEdittext_hindi_hint_font_name: {
                        if (languageSelected == 2)  {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;

                    case R.styleable.AppEdittext_tamil_hint_font_name: {
                        if (languageSelected == 3)  {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;

                    case R.styleable.AppEdittext_telugu_hint_font_name: {
                        if (languageSelected == 4) {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;

                    case R.styleable.AppEdittext_kannada_hint_font_name: {
                        if (languageSelected == 5)  {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;

                    case R.styleable.AppEdittext_bengali_hint_font_name: {
                        if (languageSelected == 6)  {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;


                }
            }*/
        } catch (Exception e) {
        }

    }
}
