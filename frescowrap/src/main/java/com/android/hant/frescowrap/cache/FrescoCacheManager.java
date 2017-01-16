package com.android.hant.frescowrap.cache;

import android.net.Uri;

import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;

import java.util.concurrent.Executor;


public class FrescoCacheManager {

    private ImagePipeline imagePipeline = Fresco.getImagePipeline();

    private static class FrescoCacheManagerHolder {
        private static final FrescoCacheManager INSTANCE = new FrescoCacheManager();
    }

    private FrescoCacheManager() {

    }

    public static final FrescoCacheManager getInstance() {
        return FrescoCacheManagerHolder.INSTANCE;
    }

    /**
     * 判定某个Uri对应的数据是否在内存缓存中
     * @param uri
     * @return
     */
    public boolean isInMemoryCache(Uri uri){
        return imagePipeline.isInBitmapMemoryCache(uri);
    }

    /**
     * 描述：判定某个Uri对应的数据是否在硬盘缓存中
     * @param uri
     * @param subscriber
     * @param executor
     */
    public void isInDiskCache(Uri uri,DataSubscriber<Boolean> subscriber,Executor executor){
        DataSource<Boolean> inDiskCacheSource = imagePipeline.isInDiskCache(uri);
        inDiskCacheSource.subscribe(subscriber,executor);
    }

    /**
     * 描述：从内存缓存中清除uri对应的数据
     * @param uri
     */
    public void evictFromMemoryCache(Uri uri){
        imagePipeline.evictFromMemoryCache(uri);
    }

    /**
     * 描述：从硬盘缓存中清除uri对应的数据
     * @param uri
     */
    public void evictFromDiskCache(Uri uri){
        imagePipeline.evictFromDiskCache(uri);
    }

    /**
     * 描述：清除内存缓存
     */
    public void clearMemoryCache(){
        imagePipeline.clearMemoryCaches();
    }

    /**
     * 描述：清除硬盘缓存
     */
    public void clearDiskCache(){
        imagePipeline.clearDiskCaches();
    }

    /**
     * 描述：清除所有缓存
     */
    public void clearCache(){
        imagePipeline.clearCaches();
    }
}
