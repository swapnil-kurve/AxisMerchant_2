package com.axismerchant.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by Dell on 03-02-2016.
 */
public class CustomTextViewLightBold extends AppTextView {
    public CustomTextViewLightBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Futura_LightBold.ttf"));
    }
}
