package com.android.hant.frescowrap.config;

import android.graphics.drawable.Animatable;

import com.facebook.imagepipeline.image.ImageInfo;


public interface ImageLoaderListener {

    public void onSuccess(String id, ImageInfo imageInfo, Animatable animatable);

    public void onFailure(String id, Throwable throwable);
}
