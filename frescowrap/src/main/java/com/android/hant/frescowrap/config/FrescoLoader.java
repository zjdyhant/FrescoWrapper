package com.android.hant.frescowrap.config;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.hant.frescowrap.constant.FrescoConstant;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.MemoryTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.webpsupport.WebpBitmapFactoryImpl;


public class FrescoLoader {

    private ActivityManager mActivityManager;

    private Context context;

    private static class FrescoLoaderHolder {
        private static final FrescoLoader INSTANCE = new FrescoLoader();
    }

    private FrescoLoader() {
    }

    public static final FrescoLoader getInstance() {
        return FrescoLoaderHolder.INSTANCE;
    }

    /**
     * @param context
     * @param frescoConfig
     * @描述：初始化Fresco
     */
    public void init(Context context, FrescoConfig frescoConfig) {
        if (frescoConfig == null) {
            throw new NullPointerException("config can not be null");
        }
        DraweeConfig.setPlaceholderId(frescoConfig.getPlaceholderDrawable());
        DraweeConfig.setFailDrawable(frescoConfig.getFailureDrawable());
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.context = context;
        boolean isWebpSupportEnabled;
        boolean isDecodeMemoryFileEnabled;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            isWebpSupportEnabled = true;
            isDecodeMemoryFileEnabled = true;
        } else {
            isWebpSupportEnabled = false;
            isDecodeMemoryFileEnabled = false;
        }

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context).experiment().setWebpSupportEnabled(true)
                .setBitmapsConfig(Bitmap.Config.RGB_565).setDecodeMemoryFileEnabled(isDecodeMemoryFileEnabled)  //根据不同android平台解码
                /**
                 *   设置EncodeImage解码时是否解码图片样图，必须和ImageRequest的ResizeOptions一起使用，作用就是在图片解码时根据ResizeOptions
                 *   所设的宽高的像素进行解码，这样解码出来可以得到一个更小的Bitmap。通过在Decode图片时，来改变采样率来实现得，使其采样ResizeOptions大小。
                 *   ResizeOptions和DownsampleEnabled参数都不影响原图片的大小，影响的是EncodeImage的大小，进而影响Decode出来的Bitmap的大小，ResizeOptions
                 *   须和此参数结合使用是因为单独使用ResizeOptions的话只支持JPEG图，所以需支持png、jpg、webp需要先设置此参数
                 */.setDownsampleEnabled(true)
                /**
                 * 最终影响的downsampleEnabledForNetwork参数。
                 这个参数的作用是在downsampleEnabled为true的情况下，设置是否当这次请求是从网络中加载图片时，来对EncodeImage重新改变大小。也就是说设置了这个为true
                 ，可以在从网络中加载图片时候根据Resizing参数Decode出更小的样图，具体是在Decode时通过采样Resizing的像素来实现的
                 */.setResizeAndRotateEnabledForNetwork(true).setCacheKeyFactory(DefaultCacheKeyFactory.getInstance())
                .setBitmapMemoryCacheParamsSupplier(bitmapMemoryCacheParamsSupplier)//内存缓存配置（一级缓存，已解码的图片）
                .setEncodedMemoryCacheParamsSupplier(encodedMemoryCacheParamsSupplier)//未解码的内存缓存的配置（二级缓存）
                .setMainDiskCacheConfig(getDiskCacheConfig(context, frescoConfig.getDiskDirName()))
                .setSmallImageDiskCacheConfig(getSmallDiskCacheConfig(context, frescoConfig.getSmallDiskDirName()))
                .setMemoryTrimmableRegistry(memoryTrimmableRegistry).build();
        Fresco.initialize(context, config);

    }

//    private DiskTrimmableRegistry diskTrimmableRegistry = new DiskTrimmableRegistry() {
//
//        @Override
//        public void registerDiskTrimmable(DiskTrimmable trimmable) {
//            if(null!=trimmable){
//                trimmable.trimToMinimum();
//            }
//
//        }
//
//        @Override
//        public void unregisterDiskTrimmable(DiskTrimmable trimmable) {
//
//        }
//    };

    private MemoryTrimmableRegistry memoryTrimmableRegistry = new MemoryTrimmableRegistry() {
        @Override
        public void registerMemoryTrimmable(MemoryTrimmable trimmable) {
            if (null != trimmable) {
                trimmable.trim(MemoryTrimType.OnSystemLowMemoryWhileAppInBackground);
            }

        }

        @Override
        public void unregisterMemoryTrimmable(MemoryTrimmable trimmable) {

        }
    };

    private Supplier<MemoryCacheParams> bitmapMemoryCacheParamsSupplier = new Supplier<MemoryCacheParams>() {
        @Override
        public MemoryCacheParams get() {
            final int maxCacheSize = getMaxCacheSize();
            return new MemoryCacheParams(maxCacheSize, // 内存缓存中总图片的最大大小,以字节为单位。
                    FrescoConstant.MAX_MAIN_MEMORY_NUM,                     // 内存缓存中图片的最大数量。
                    maxCacheSize, // 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                    FrescoConstant.MAX_MAIN_MEMORY_NUM,                    // 内存缓存中准备清除的总图片的最大数量。
                    maxCacheSize);         // 内存缓存中单个图片的最大大小,以字节为单位。
        }
    };

    private Supplier<MemoryCacheParams> encodedMemoryCacheParamsSupplier = new Supplier<MemoryCacheParams>() {
        @Override
        public MemoryCacheParams get() {
            final int maxEncodedCacheSize = getMaxEncodedCacheSize();
            return new MemoryCacheParams(maxEncodedCacheSize, // 内存缓存中总图片的最大大小,以字节为单位。
                    maxEncodedCacheSize / 8,                     // 内存缓存中图片的最大数量。
                    maxEncodedCacheSize, // 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                    maxEncodedCacheSize / 8,                    // 内存缓存中准备清除的总图片的最大数量。
                    maxEncodedCacheSize);                    // 内存缓存中单个图片的最大大小。
        }
    };

    private DiskCacheConfig getDiskCacheConfig(Context context, String dirName) {
        return DiskCacheConfig.newBuilder(context).setBaseDirectoryPath(context.getCacheDir())//硬盘缓存图片基路径
                .setBaseDirectoryName(dirName)//文件夹名
                .setMaxCacheSize(FrescoConstant.MAX_DISK_CACHE_SIZE)//最大硬盘缓存大小
                .setMaxCacheSizeOnLowDiskSpace(FrescoConstant.MAX_DISK_CACHE_LOW_SIZE)//设备磁盘空间低时候的最大硬盘缓存大小。
                .setMaxCacheSizeOnVeryLowDiskSpace(FrescoConstant.MAX_DISK_CACHE_VERY_LOW_SIZE)//设备磁盘空间极低时候的最大硬盘缓存大小
//                .setDiskTrimmableRegistry(diskTrimmableRegistry)
                .build();
    }

    private DiskCacheConfig getSmallDiskCacheConfig(Context context, String dirName) {
        return DiskCacheConfig.newBuilder(context).setBaseDirectoryPath(context.getCacheDir())//硬盘缓存图片基路径
                .setBaseDirectoryName(dirName)//文件夹名
                .setMaxCacheSize(FrescoConstant.MAX_SMALL_DISK_CACHE_SIZE)//最大硬盘缓存大小
                .setMaxCacheSizeOnLowDiskSpace(FrescoConstant.MAX_SMALL_DISK_CACHE_LOW_SIZE)//设备磁盘空间低时候的最大硬盘缓存大小。
                .setMaxCacheSizeOnVeryLowDiskSpace(FrescoConstant.MAX_SMALL_DISK_CACHE_VERY_LOW_SIZE)//设备磁盘空间极低时候的最大硬盘缓存大小
//                .setDiskTrimmableRegistry(diskTrimmableRegistry)
                .build();
    }


    private int getMaxCacheSize() {
        final int maxMemory = Math.min(mActivityManager.getMemoryClass() * ByteConstants.MB, Integer.MAX_VALUE);
        if (maxMemory < 32 * ByteConstants.MB) {
            return 4 * ByteConstants.MB;
        } else if (maxMemory < 64 * ByteConstants.MB) {
            return 6 * ByteConstants.MB;
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                return 8 * ByteConstants.MB;
            } else {
                return maxMemory / 4;
            }
        }
    }

    private int getMaxEncodedCacheSize() {
        final int maxMemory = (int) Math.min(Runtime.getRuntime().maxMemory(), Integer.MAX_VALUE);
        if (maxMemory < 16 * ByteConstants.MB) {
            return 1 * ByteConstants.MB;
        } else if (maxMemory < 32 * ByteConstants.MB) {
            return 2 * ByteConstants.MB;
        } else {
            return 4 * ByteConstants.MB;
        }
    }

    /**
     * 描述：按照xml里simpleDraweeView的配置方式加载图片,该资源一般是可信赖的，这里不会进行resize，异步加载
     *
     * @param simpleDraweeView
     * @param resId               这里的资源必须是真正的图片而不是类似shapeDrawable，可以传R.mipmap.xxx,R.raw.xxx
     * @param imageLoaderListener 下载监听
     */
    public void loadImageFromResource(final SimpleDraweeView simpleDraweeView, final int resId, final ImageLoaderListener
            imageLoaderListener) {
        if (null == simpleDraweeView) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                Uri uri = Uri.parse(FrescoConstant.SCHEMA_PREFIX_RES + resId);
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(
                        ImageRequest.CacheChoice.DEFAULT).setAutoRotateEnabled(true).setLowestPermittedRequestLevel(ImageRequest.RequestLevel
                        .FULL_FETCH).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setTapToRetryEnabled(false)
                        .setAutoPlayAnimations(true).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }

    /**
     * 描述：按照xml里simpleDraweeView的配置方式从网络加载图片,图片不可信赖，会按照View的宽高进行resize，异步加载
     *
     * @param simpleDraweeView
     * @param url                 图片的远程url,需要完整的url，包括schema协议头,
     *                            比如http://xxx,https://xxx
     * @param imageLoaderListener 下载监听
     */
    public void loadImageFromWeb(final SimpleDraweeView simpleDraweeView, final String url, final ImageLoaderListener
            imageLoaderListener) {
        if (null == simpleDraweeView) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                Uri uri = Uri.parse(url == null ? FrescoConstant.DEFAULT_EMPTY_WEB_URL : url);
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(ImageRequest.CacheChoice
                        .DEFAULT).setResizeOptions(frescoSizeResult.getResizeOptions()).setAutoRotateEnabled(true)
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setTapToRetryEnabled(false)
                        .setAutoPlayAnimations(true).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }

    /**
     * 描述：按照xml里simpleDraweeView的配置方式从网络加载图片,图片不可信赖，会按照View的宽高进行resize,并且置灰图片，异步加载
     *
     * @param simpleDraweeView
     * @param url                 图片的远程url,需要完整的url，包括schema协议头,
     *                            比如http://xxx,https://xxx
     * @param imageLoaderListener 下载监听
     */
    public void loadGrayImageFromWeb(final SimpleDraweeView simpleDraweeView, final String url, final ImageLoaderListener
            imageLoaderListener) {
        if (null == simpleDraweeView) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                Uri uri = Uri.parse(url == null ? FrescoConstant.DEFAULT_EMPTY_WEB_URL : url);
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(ImageRequest.CacheChoice
                        .DEFAULT).setResizeOptions(frescoSizeResult.getResizeOptions()).setAutoRotateEnabled(true)
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH).setPostprocessor(new BasePostprocessor() {
                            @Override
                            public String getName() {
                                return "grayPostprocessor";
                            }

                            @Override
                            public void process(Bitmap destBitmap, Bitmap sourceBitmap) {
                                Canvas c = new Canvas(destBitmap);
                                Paint paint = new Paint();
                                ColorMatrix cm = new ColorMatrix();
                                cm.setSaturation(0);
                                ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
                                paint.setColorFilter(f);
                                c.drawBitmap(sourceBitmap, 0, 0, paint);
                                process(destBitmap);
                            }
                        }).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setTapToRetryEnabled(false)
                        .setAutoPlayAnimations(true).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }

    /**
     * 描述：按照xml里simpleDraweeView的配置方式从设备本地存储加载图片，图片不可信赖，会按照View的宽高进行resize，异步加载
     *
     * @param simpleDraweeView
     * @param filePath            无需包含file://前缀，只需要文件的绝对路径,类似/mnt/sdcard/xxx
     * @param imageLoaderListener 下载监听
     */
    public void loadImageFromLocalFile(final SimpleDraweeView simpleDraweeView, final String filePath, final
    ImageLoaderListener imageLoaderListener) {
        if (null == simpleDraweeView || null == filePath) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                Uri uri = Uri.parse(FrescoConstant.SCHEMA_PREFIX_FILE + filePath);
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(ImageRequest.CacheChoice
                        .DEFAULT).setResizeOptions(frescoSizeResult.getResizeOptions()).setAutoRotateEnabled(true)
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setTapToRetryEnabled(false)
                        .setAutoPlayAnimations(true).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }

    /**
     * 描述：按照xml里simpleDraweeView的配置方式从assets加载图片，该资源一般是可信赖的，这里不会进行resize，异步加载
     *
     * @param simpleDraweeView
     * @param asset               无需包含asset:///前缀，只需要assets下面的相对路径，例如xxx.png,dir/xxx.png
     * @param imageLoaderListener 下载监听
     */
    public void loadImageFromAsset(final SimpleDraweeView simpleDraweeView, final String asset, final ImageLoaderListener
            imageLoaderListener) {
        if (null == simpleDraweeView || null == asset) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                Uri uri = Uri.parse(FrescoConstant.SCHEMA_PREFIX_ASSET + asset);
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(ImageRequest.CacheChoice
                        .DEFAULT).setAutoRotateEnabled(true).setLowestPermittedRequestLevel(ImageRequest.RequestLevel
                        .FULL_FETCH).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setTapToRetryEnabled(false)
                        .setAutoPlayAnimations(true).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }

    /**
     * 描述：按照xml里simpleDraweeView的配置方式从ContentResolver加载图片，资源不可信赖，会按照View的宽高进行resize，异步加载
     *
     * @param simpleDraweeView
     * @param contentUrl          例如 content://media/external/images/8
     * @param imageLoaderListener 下载监听
     */
    public void loadImageFromContent(final SimpleDraweeView simpleDraweeView, final String contentUrl, final
    ImageLoaderListener imageLoaderListener) {
        if (null == simpleDraweeView || null == contentUrl) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                Uri uri = Uri.parse(contentUrl);
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(ImageRequest.CacheChoice
                        .DEFAULT).setResizeOptions(frescoSizeResult.getResizeOptions()).setAutoRotateEnabled(true)
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setTapToRetryEnabled(false)
                        .setAutoPlayAnimations(true).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }

    /**
     * 描述：加载图片通用方法  会按照view默认宽高进行resize，异步加载
     *
     * @param simpleDraweeView
     * @param uri                 加载图片uri
     * @param imageType           图片大小类型
     * @param isTapToRetryEnabled 是否允许点击重新加载
     * @param imageLoaderListener 下载监听
     */
    public void loadImage(final SimpleDraweeView simpleDraweeView, final Uri uri, final ImageRequest.CacheChoice imageType, final
    boolean isTapToRetryEnabled, final ImageLoaderListener imageLoaderListener) {
        if (null == simpleDraweeView) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(imageType)
                        .setResizeOptions(frescoSizeResult.getResizeOptions()).setAutoRotateEnabled(true)
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setTapToRetryEnabled(isTapToRetryEnabled)
                        .setAutoPlayAnimations(true).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }

    /**
     * 描述：加载图片通用方法 根据传入resize参数进行裁剪，异步加载
     *
     * @param simpleDraweeView
     * @param uri                 加载图片uri
     * @param imageType           图片大小类型
     * @param isTapToRetryEnabled 是否允许点击重新加载
     * @param resizeOptions       裁剪图片参数
     * @param imageLoaderListener 下载监听
     */
    public void loadImage(final SimpleDraweeView simpleDraweeView, final Uri uri, final ImageRequest.CacheChoice imageType, final
    boolean isTapToRetryEnabled, final ResizeOptions resizeOptions, final ImageLoaderListener imageLoaderListener) {
        if (null == simpleDraweeView) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                if (null != resizeOptions) {
                    if (resizeOptions.width <= 0 || resizeOptions.height <= 0) {
                        return;
                    }
                }
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(imageType)
                        .setAutoRotateEnabled(true).setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                        .setResizeOptions(resizeOptions).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setTapToRetryEnabled(isTapToRetryEnabled)
                        .setAutoPlayAnimations(true).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }

    /**
     * 描述：加载图片通用方法 会按照view默认宽高进行resize，异步加载
     *
     * @param simpleDraweeView
     * @param uri                 加载图片uri
     * @param imageType           图片大小类型
     * @param isTapToRetryEnabled 是否允许点击重新加载
     * @param isAutoAnim          是否自动开启动画
     * @param imageLoaderListener 下载监听
     */
    public void loadImage(final SimpleDraweeView simpleDraweeView, final Uri uri, final ImageRequest.CacheChoice imageType, final
    boolean isTapToRetryEnabled, final boolean isAutoAnim, final ImageLoaderListener imageLoaderListener) {
        if (null == simpleDraweeView) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(imageType)
                        .setAutoRotateEnabled(true).setResizeOptions(frescoSizeResult.getResizeOptions())
                        .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setTapToRetryEnabled(isTapToRetryEnabled)
                        .setAutoPlayAnimations(isAutoAnim).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }

    /**
     * 描述：加载图片通用方法 根据传入resize参数进行裁剪，异步加载
     *
     * @param simpleDraweeView
     * @param uri                 加载图片uri
     * @param imageType           图片大小类型
     * @param isTapToRetryEnabled 是否允许点击重新加载
     * @param resizeOptions       裁剪图片参数
     * @param isAutoAnim          是否自动开启动画
     * @param imageLoaderListener 下载监听
     */
    public void loadImage(final SimpleDraweeView simpleDraweeView, final Uri uri, final ImageRequest.CacheChoice imageType, final
    boolean isTapToRetryEnabled, final ResizeOptions resizeOptions, final boolean isAutoAnim, final ImageLoaderListener
                                  imageLoaderListener) {
        if (null == simpleDraweeView) {
            return;
        }
        simpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
                final FrescoSizeResult frescoSizeResult = checkSize(simpleDraweeView);
                if (null == frescoSizeResult) {
                    return;
                }
                if (null != resizeOptions) {
                    if (resizeOptions.width <= 0 || resizeOptions.height <= 0) {
                        return;
                    }
                }
                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).setCacheChoice(imageType)
                        .setAutoRotateEnabled(true).setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                        .setResizeOptions(resizeOptions).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder().setAutoPlayAnimations(isAutoAnim)
                        .setTapToRetryEnabled(isTapToRetryEnabled).setImageRequest(imageRequest).setControllerListener
                                (createControllerListener(simpleDraweeView, frescoSizeResult, imageLoaderListener))
                        .setOldController(simpleDraweeView.getController()).build();
                simpleDraweeView.setController(controller);
            }
        });
    }


    private FrescoSizeResult checkSize(SimpleDraweeView simpleDraweeView) {
        ViewGroup.LayoutParams layoutParams = simpleDraweeView.getLayoutParams();
        FrescoSizeResult frescoSizeResult = new FrescoSizeResult();
        if ((layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) && (layoutParams.height == ViewGroup.LayoutParams
                .WRAP_CONTENT)) {
            frescoSizeResult.setSizeCode(FrescoConstant.SIZE_BOTH_WRAP);
            frescoSizeResult.setResizeOptions(null);
            return frescoSizeResult;
        }
        int destWidth = 0, destHeight = 0;
        float ratio = simpleDraweeView.getAspectRatio();
        if ((layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) && (layoutParams.height != ViewGroup.LayoutParams
                .WRAP_CONTENT)) {
            if (layoutParams.height <= 0 && layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                return null;
            }
            if (ratio <= 0.0F) {
                frescoSizeResult.setSizeCode(FrescoConstant.SIZE_WIDTH_WRAP);
                frescoSizeResult.setResizeOptions(null);
                return frescoSizeResult;
            }
            if (layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                destHeight = simpleDraweeView.getHeight();
            } else {
                destHeight = layoutParams.height;
            }
            destWidth = (int) (destHeight * ratio);
        } else if ((layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) && (layoutParams.height == ViewGroup
                .LayoutParams.WRAP_CONTENT)) {
            if (layoutParams.width <= 0 && layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT) {
                return null;
            }
            if (ratio <= 0.0F) {
                frescoSizeResult.setSizeCode(FrescoConstant.SIZE_HEIGHT_WRAP);
                frescoSizeResult.setResizeOptions(null);
                return frescoSizeResult;
            }
            if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                destWidth = simpleDraweeView.getWidth();
            } else {
                destWidth = layoutParams.width;
            }
            destHeight = (int) (destWidth / ratio);
        } else if ((layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) && (layoutParams.height != ViewGroup
                .LayoutParams.WRAP_CONTENT)) {
            if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                destWidth = simpleDraweeView.getWidth();
            } else {
                destWidth = layoutParams.width;
            }
            if (layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                destHeight = simpleDraweeView.getHeight();
            } else {
                destHeight = layoutParams.height;
            }
        }
        if (destWidth <= 0 || destHeight <= 0) {
            return null;
        } else {
            frescoSizeResult.setSizeCode(FrescoConstant.SIZE_SUCCESS);
            frescoSizeResult.setResizeOptions(new ResizeOptions(destWidth, destHeight));
            return frescoSizeResult;
        }
    }

    private ControllerListener<ImageInfo> createControllerListener(final SimpleDraweeView simpleDraweeView, final
    FrescoSizeResult frescoSizeResult, final ImageLoaderListener imageLoaderListener) {
        final int tempWidth = simpleDraweeView.getLayoutParams().width;
        final int tempHeight = simpleDraweeView.getLayoutParams().height;
        ControllerListener<ImageInfo> customControllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (frescoSizeResult.getSizeCode() == FrescoConstant.SIZE_SUCCESS) {
                    if (null != imageLoaderListener) {
                        imageLoaderListener.onSuccess(id, imageInfo, animatable);
                    }
                    return;
                }
                if (null != imageInfo) {
                    if (frescoSizeResult.getSizeCode() == FrescoConstant.SIZE_BOTH_WRAP) {
                        simpleDraweeView.getLayoutParams().width = imageInfo.getWidth();
                        simpleDraweeView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    } else if (frescoSizeResult.getSizeCode() == FrescoConstant.SIZE_WIDTH_WRAP) {
                        if (tempHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
                            simpleDraweeView.getLayoutParams().height = simpleDraweeView.getHeight();
                        }
                        simpleDraweeView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    } else if (frescoSizeResult.getSizeCode() == FrescoConstant.SIZE_HEIGHT_WRAP) {
                        if (tempWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
                            simpleDraweeView.getLayoutParams().width = simpleDraweeView.getWidth();
                        }
                        simpleDraweeView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    }
                    simpleDraweeView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
                }
                if (null != imageLoaderListener) {
                    imageLoaderListener.onSuccess(id, imageInfo, animatable);
                }
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                super.onFailure(id, throwable);
                if (null != imageLoaderListener) {
                    if (frescoSizeResult.getSizeCode() != FrescoConstant.SIZE_SUCCESS) {
                        simpleDraweeView.getLayoutParams().width = FrescoConstant.getDefaultHolderImageSize(context);
                        simpleDraweeView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        simpleDraweeView.setAspectRatio(1.0F);
                    }
                    imageLoaderListener.onFailure(id, throwable);
                }
            }
        };
        return customControllerListener;
    }

    /**
     * 描述：兼容png,jpg,webp,需要真正的图片资源，而不是类似ColorDrawable ShapeDrawable,同步方法
     *
     * @param view
     * @param resId 图片资源ID
     */
    public void setBackground(View view, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(new BitmapDrawable(context.getResources(), WebpBitmapFactoryImpl.hookDecodeResource(context
                    .getResources(), resId)));
        } else {
            view.setBackgroundDrawable(new BitmapDrawable(context.getResources(), WebpBitmapFactoryImpl.hookDecodeResource
                    (context.getResources(), resId)));
        }
    }

    /**
     * 描述：兼容png,jpg,webp,而不是类似ColorDrawable ShapeDrawable，同步方法
     *
     * @param imageView
     * @param resId     图片资源ID
     */
    public void setImageResource(ImageView imageView, int resId) {
        imageView.setImageBitmap(WebpBitmapFactoryImpl.hookDecodeResource(context.getResources(), resId));
    }

    /**
     * 描述：兼容png,jpg,webp,需要真正的图片资源，而不是类似ColorDrawable ShapeDrawable
     *
     * @param resId 图片资源ID
     */
    public Drawable getDrawable(int resId) {
        return new BitmapDrawable(context.getResources(), WebpBitmapFactoryImpl.hookDecodeResource(context.getResources(),
                resId));
    }
}
