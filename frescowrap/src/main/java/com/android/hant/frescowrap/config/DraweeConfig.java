package com.android.hant.frescowrap.config;

import android.graphics.drawable.Drawable;

/**
 * Created by hantao on 17/1/12.
 */

public class DraweeConfig {

    private static Drawable placeholderDrawable;
    private static Drawable failDrawable;

    public DraweeConfig() {
    }

    public static Drawable getPlaceholderDrawable() {
        return placeholderDrawable;
    }

    public static void setPlaceholderId(Drawable placeholderDrawable) {
        placeholderDrawable = placeholderDrawable;
    }

    public static Drawable getFailDrawable() {
        return failDrawable;
    }

    public static void setFailDrawable(Drawable failDrawable) {
        failDrawable = failDrawable;
    }

}
