package com.axismerchant.custom;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import com.axismerchant.R;

/**
 * Created by USer on 08-12-2016.
 */

public class ProgressDialogue {

    Dialog alertDialog;


    public void onCreateDialog(Context context) {
        // Inflate the layout for this fragment
        alertDialog = new Dialog(context);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.progress_dialog);
        alertDialog.setCancelable(false);

        ProgressBar progressBar = (ProgressBar) alertDialog.findViewById(R.id.circularProgressBar);

        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 500);
        animation.setDuration(50000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setRepeatCount(5);
        animation.start();

    }


    public void show() {
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }

    }

    public void dismiss() {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

    }
}
