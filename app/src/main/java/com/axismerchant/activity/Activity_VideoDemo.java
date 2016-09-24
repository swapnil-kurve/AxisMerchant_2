package com.axismerchant.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import com.axismerchant.R;

public class Activity_VideoDemo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_demo);

        /*VideoView view = (VideoView)findViewById(R.id.videoView);
        String path = "https://youtu.be/1jVBpT0m8Wg";
        view.setVideoURI(Uri.parse(path));
        MediaController mc= new MediaController(Activity_VideoDemo.this);
        mc.setAnchorView(view);
        view.setMediaController(mc);
        view.start();
*/
     /*   String path = "android.resource://" + getPackageName() + "/" + R.raw.axis;
        view.setVideoURI(Uri.parse(path));
        MediaController mc= new MediaController(Activity_VideoDemo.this);
        view.setMediaController(mc);
        view.start();*/

    }


}
