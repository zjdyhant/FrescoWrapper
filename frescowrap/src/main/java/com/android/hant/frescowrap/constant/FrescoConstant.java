package com.android.hant.frescowrap.constant;

import android.content.Context;
import android.util.TypedValue;

import com.facebook.common.util.ByteConstants;


public class FrescoConstant {
    public final static int MAX_MAIN_MEMORY_NUM = 256; //默认已解码图片最大内存缓存项数目
    public static final int MAX_DISK_CACHE_VERY_LOW_SIZE = 2 * ByteConstants.MB;//默认图极低磁盘空间缓存的最大值(大图)
    public static final int MAX_DISK_CACHE_LOW_SIZE = 10 * ByteConstants.MB;//默认图低磁盘空间缓存的最大值(大图)
    public static final int MAX_DISK_CACHE_SIZE = 40 * ByteConstants.MB;//默认图磁盘缓存的最大值(大图)
    public static final int MAX_SMALL_DISK_CACHE_VERY_LOW_SIZE = 2 * ByteConstants.MB;//默认图极低磁盘空间缓存的最大值(小图)
    public static final int MAX_SMALL_DISK_CACHE_LOW_SIZE = 10 * ByteConstants.MB;//默认图低磁盘空间缓存的最大值(小图)
    public static final int MAX_SMALL_DISK_CACHE_SIZE = 20 * ByteConstants.MB;//默认图磁盘缓存的最大值(小图)

    public static final String SCHEMA_PREFIX_RES = "res:///";
    public static final String SCHEMA_PREFIX_FILE = "file://";
    public static final String SCHEMA_PREFIX_ASSET = "asset:///";

    public static final int SIZE_BOTH_WRAP = 0;
    public static final int SIZE_WIDTH_WRAP = 1;
    public static final int SIZE_HEIGHT_WRAP = 2;
    public static final int SIZE_SUCCESS = 3;

    public static final String DEFAULT_EMPTY_WEB_URL = "http://www.no.exist";

    public static int getDefaultHolderImageSize(Context context) {

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
    }

}
