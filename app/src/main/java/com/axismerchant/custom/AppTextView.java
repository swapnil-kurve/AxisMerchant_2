package com.axismerchant.custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.axismerchant.R;
import com.axismerchant.activity.AppActivity;
import com.axismerchant.classes.Constants;

/**
 * Created by venki on 19/8/16.
 */
public class AppTextView extends TextView {

    public AppTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            AppActivity activity = (AppActivity) context;
            SharedPreferences preferences = activity.getSharedPreferences(Constants.LanguageData, Context.MODE_PRIVATE);
            int languageSelected = preferences.getInt("Selected_Language",0);

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppTextView);
            final int N = a.getIndexCount();
            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);
                switch (attr) {

                    case R.styleable.AppTextView_hindi_text:
                        if (languageSelected == 2) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.mangal_font)));
                            setText(text);
                        }
                        break;

                    case R.styleable.AppTextView_tamil_text:
                        if (languageSelected == 3) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.tamil_font)));
                            setText(text);
                        }
                        break;

                    case R.styleable.AppTextView_telugu_text:
                        if (languageSelected == 4) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.telugu_font)));
                            setText(text);
                        }
                        break;

                    case R.styleable.AppTextView_kannada_text:
                        if (languageSelected == 5) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.kannada_font)));
                            setText(text);
                        }
                        break;

                    case R.styleable.AppTextView_bengali_text:
                        if (languageSelected == 6) {
                            String text = a.getString(attr);
                            setTypeface(Typeface.createFromAsset(activity.getAssets(), activity.getString(R.string.bengali_font)));
                            setText(text);
                        }
                        break;


                    case R.styleable.AppTextView_hindi_font_name: {
                        if (languageSelected == 2)  {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;

                    case R.styleable.AppTextView_tamil_font_name: {
                        if (languageSelected == 3)  {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;

                    case R.styleable.AppTextView_telugu_font_name: {
                        if (languageSelected == 4) {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;

                    case R.styleable.AppTextView_kannada_font_name: {
                        if (languageSelected == 5)  {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;

                    case R.styleable.AppTextView_bengali_font_name: {
                        if (languageSelected == 6)  {
                            String typeface = a.getString(attr);
                            this.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
                        }
                    }
                    break;


                }
            }
        } catch (Exception e) {
        }
    }
}
