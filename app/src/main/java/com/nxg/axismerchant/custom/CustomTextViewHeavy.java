package com.nxg.axismerchant.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by Dell on 03-02-2016.
 */
public class CustomTextViewHeavy extends AppTextView {
    public CustomTextViewHeavy(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/futura_std_heavy.otf"));
    }
}
