# FrescoWrapper
Fresco的封装 方便使用
#How to use
(1) in application
```java
 FrescoConfig frescoConfig = FrescoConfigBuilder.newInstance().setDiskDirName("FrescoDefault").setSmallDiskDirName
                ("FrescoSmall").setPlaceholder(ContextCompat.getDrawable(this, R.drawable
                .icon_default)).setFailImage(ContextCompat.getDrawable(this, R.drawable
                .icon_default)).build();
        FrescoLoader.getInstance().init(this, frescoConfig);
```
