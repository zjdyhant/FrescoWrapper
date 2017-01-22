# FrescoWrapper
Fresco的封装 方便使用
#作用
（1）提供全局配置占位图和失败占位图的功能。通过application中配置FrescoConfig。<br>
（2）对图片加载进行封装，使得加载本地、url、content图片……更加方便，同时提供加载图片监听。<br>
（3）支持 xml中对SimpleDraweeView的宽高wrap_content进行兼容。
#How to use
(1) in gradle<br>
```java
    compile 'com.facebook.fresco:fresco:0.12.0'
    // 支持 GIF 动图，需要添加
    compile 'com.facebook.fresco:animated-gif:0.12.0'
    // 支持 WebP （静态图+动图），需要添加
    compile 'com.facebook.fresco:animated-webp:0.12.0'
    compile 'com.facebook.fresco:webpsupport:0.12.0'
```
(2) in application<br>
  application中进行初始化
```java
 FrescoConfig frescoConfig = FrescoConfigBuilder.newInstance().setDiskDirName("FrescoDefault").setSmallDiskDirName
                ("FrescoSmall").setPlaceholder(ContextCompat.getDrawable(this, R.drawable
                .icon_default)).setFailImage(ContextCompat.getDrawable(this, R.drawable
                .icon_default)).build();
        FrescoLoader.getInstance().init(this, frescoConfig);
```
（3）加载图片
```java
    //按照xml里simpleDraweeView的配置方式从网络加载图片,图片不可信赖，会按照View的宽高进行resize，异步加载
    FrescoLoader.getInstance().loadImageFromWeb(simpleDraweeView,url,listener);
    //按照xml里simpleDraweeView的配置方式从ContentResolver加载图片，资源不可信赖，会按照View的宽高进行resize，异步加载
    FrescoLoader.getInstance().loadImageFromContent(simpleDraweeView,url,listener);
    //按照xml里simpleDraweeView的配置方式从assets加载图片，该资源一般是可信赖的，这里不会进行resize，异步加载
    FrescoLoader.getInstance().loadImageFromAsset(simpleDraweeView,url,listener);
    //加载本地图片   url：无需包含file://前缀，只需要文件的绝对路径,类似/mnt/sdcard/xxx
    FrescoLoader.getInstance().loadImageFromLocalFile(simpleDraweeView,url,listener);
    //加载本地资源图片
    FrescoLoader.getInstance().loadImageFromResource(simpleDraweeView,resId,listener);
    //网络加载图片  并对图片置灰
    FrescoLoader.getInstance().loadGrayImageFromWeb(simpleDraweeView,url,listener);
```
