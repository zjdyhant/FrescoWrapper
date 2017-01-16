package com.android.hant.frescowrap.config;

import android.graphics.drawable.Drawable;


public class FrescoConfig {
    private String diskDirName;

    private String smallDiskDirName;

    private Drawable placeholderDrawable;

    private Drawable failDrawable;


    protected FrescoConfig(FrescoConfigBuilder frescoConfigBuilder) {
        this.diskDirName = frescoConfigBuilder.getDiskDirName();
        this.smallDiskDirName = frescoConfigBuilder.getSmallDiskDirName();
        this.placeholderDrawable = frescoConfigBuilder.getPlaceholderDrawable();
        this.failDrawable = frescoConfigBuilder.getFailureDrawable();
    }

    public String getDiskDirName() {
        return diskDirName;
    }

    public String getSmallDiskDirName() {
        return smallDiskDirName;
    }

    public Drawable getPlaceholderDrawable() {
        return placeholderDrawable;
    }


    public Drawable getFailureDrawable() {
        return failDrawable;
    }

}
