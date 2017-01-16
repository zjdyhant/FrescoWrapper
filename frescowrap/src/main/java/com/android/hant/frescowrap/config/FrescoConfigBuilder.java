package com.android.hant.frescowrap.config;

import android.graphics.drawable.Drawable;


public class FrescoConfigBuilder {

    private String diskDirName;

    private String smallDiskDirName;

    private Drawable placeholderDrawable;

    private Drawable failDrawable;


    public static FrescoConfigBuilder newInstance() {
        return new FrescoConfigBuilder();
    }

    private FrescoConfigBuilder() {
        diskDirName = "FrescoDefaultDisk";
        smallDiskDirName = "FrescoSmallDisk";
        placeholderDrawable = null;
        failDrawable = null;
    }

    public String getDiskDirName() {
        return diskDirName;
    }

    public FrescoConfigBuilder setDiskDirName(String diskDirName) {
        this.diskDirName = diskDirName;
        return this;
    }

    public String getSmallDiskDirName() {
        return smallDiskDirName;
    }

    public FrescoConfigBuilder setSmallDiskDirName(String smallDiskDirName) {
        this.smallDiskDirName = smallDiskDirName;
        return this;
    }

    public FrescoConfigBuilder setPlaceholder(Drawable placeholderDrawable) {
        this.placeholderDrawable = placeholderDrawable;
        return this;
    }

    public Drawable getPlaceholderDrawable() {
        return placeholderDrawable;
    }


    public FrescoConfigBuilder setFailImage(Drawable failDrawable) {
        this.failDrawable = failDrawable;
        return this;
    }

    public Drawable getFailureDrawable() {
        return failDrawable;
    }


    public FrescoConfig build() {
        return new FrescoConfig(this);
    }

}
