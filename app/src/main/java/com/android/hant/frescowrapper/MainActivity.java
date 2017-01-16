package com.android.hant.frescowrapper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.hant.frescowrap.config.FrescoLoader;
import com.facebook.drawee.view.SimpleDraweeView;

public class MainActivity extends AppCompatActivity {

    private String url = "http://img4.imgtn.bdimg.com/it/u=1387176538,2453453076&fm=23&gp=0.jpg";

    private SimpleDraweeView draweeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        draweeView = (SimpleDraweeView) findViewById(R.id.drawee_view);
        FrescoLoader.getInstance().loadImageFromWeb(draweeView,url,null);
    }
}
