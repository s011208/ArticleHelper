package com.bj4.yhh.lawhelper;

import android.app.Application;

import com.bj4.yhh.lawhelper.services.RetrieveActDataService;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.parse.Parse;

/**
 * Created by yenhsunhuang on 15/4/14.
 */
public class AccountApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initParse();
        AccountDataHelper.getInstance(this);
        initActList();
        initImageLoader();
        RetrieveActDataService.requestToCheckUnFinishedTask(this); // to check whether have failed tasks
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void initActList() {
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();
        ImageLoader.getInstance().init(config);
    }

    private void initParse() {
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "iYp5DMTOJHG4PRFqwXDG35dfquBL2nCjq0wTnFhQ", "OzTIYKiuPtsfyQ0Mzphj7mwAVRf44t55P4nwDWug");
    }
}
