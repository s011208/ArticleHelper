package com.bj4.yhh.lawhelper.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.bj4.yhh.lawhelper.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by yenhsunhuang on 15/7/25.
 */
public class BaseActivity extends Activity {
    // act content
    public static final String EXTRA_ACT_CONTENT = "extra_act_content";
    public static final String EXTRA_ACT_CONTENT_TYPE = "extra_act_content_type";

    // animation
    public static final String EXTRA_TOUCH_X = "extra_touch_x";

    private SystemBarTintManager mTintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemUiTint();
    }

    public SystemBarTintManager getTintManager() {
        return mTintManager;
    }

    /**
     * https://github.com/jgilfelt/SystemBarTint
     */
    private void initSystemUiTint() {
        mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setNavigationBarTintEnabled(false);
        mTintManager.setStatusBarTintResource(R.color.main_title_color_dark);
        mTintManager.setNavigationBarTintColor(Color.TRANSPARENT);
    }

    public boolean isStatusBarTintEnabled() {
        return mTintManager.isStatusBarTintEnabled();
    }

    public boolean isNavBarTintEnabled() {
        return mTintManager.isNavBarTintEnabled();
    }
}
