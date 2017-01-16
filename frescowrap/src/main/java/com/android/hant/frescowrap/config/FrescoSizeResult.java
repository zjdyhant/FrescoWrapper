package com.android.hant.frescowrap.config;

import com.facebook.imagepipeline.common.ResizeOptions;

import java.io.Serializable;


public class FrescoSizeResult implements Serializable {


    private static final long serialVersionUID = 3762728633603473721L;

    private int sizeCode;

   private ResizeOptions resizeOptions;

    public int getSizeCode() {
        return sizeCode;
    }

    public void setSizeCode(int sizeCode) {
        this.sizeCode = sizeCode;
    }


    public ResizeOptions getResizeOptions() {
        return resizeOptions;
    }

    public void setResizeOptions(ResizeOptions resizeOptions) {
        this.resizeOptions = resizeOptions;
    }
}