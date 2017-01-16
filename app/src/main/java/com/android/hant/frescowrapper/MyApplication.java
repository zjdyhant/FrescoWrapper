package com.android.hant.frescowrapper;

import android.app.Application;
import android.support.v4.content.ContextCompat;

import com.android.hant.frescowrap.config.FrescoConfig;
import com.android.hant.frescowrap.config.FrescoConfigBuilder;
import com.android.hant.frescowrap.config.FrescoLoader;


/**
 * Created by hantao on 17/1/12.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FrescoConfig frescoConfig = FrescoConfigBuilder.newInstance().setDiskDirName("FrescoDefault").setSmallDiskDirName
                ("FrescoSmall").setPlaceholder(ContextCompat.getDrawable(this, R.drawable
                .icon_default)).setFailImage(ContextCompat.getDrawable(this, R.drawable
                .icon_default)).build();
        FrescoLoader.getInstance().init(this, frescoConfig);
    }
}
