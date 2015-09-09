package com.bj4.yhh.lawhelper.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yenhsunhuang on 15/7/27.
 */
public class Utils {
    private static final String TAG = "Utils";

    public static int getStatusBarHeight(Context context) {
        int status_bar_height = 0;
        int statusBarHeightResourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarHeightResourceId > 0) {
            status_bar_height = context.getResources().getDimensionPixelSize(statusBarHeightResourceId);
        }
        return status_bar_height;
    }

    public static int getNavigationBarHeight(Context context, int orientation) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier(
                orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                "dimen", "android");
        if (id > 0) {
            return resources.getDimensionPixelSize(id);
        }
        return 0;
    }

    public static boolean hasNavigationBar() {
        try {
            Class<?> c = Class.forName("android.view.WindowManagerGlobal");
            Method m = c.getDeclaredMethod("getWindowManagerService", new Class<?>[]{});
            Object windowManagerService = m.invoke(null, new Object[]{});
            c = windowManagerService.getClass();
            m = c.getDeclaredMethod("hasNavigationBar", new Class<?>[]{});
            return (Boolean) m.invoke(windowManagerService, new Object[]{});
        } catch (Exception e) {
            Log.w(TAG, "failed to get windowManagerService", e);
        }
        return false;
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        }
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static boolean haveInternet(Context ctx) {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            return false;
        }
        return true;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ((netInfo != null) && netInfo.isConnected());
    }

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return ((netInfo != null) && netInfo.isConnected());
    }
}
