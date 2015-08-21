package com.bj4.yhh.accountant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.fragments.entry.MainEntryFragment;
import com.bj4.yhh.accountant.fragments.init.InitDataFragment;
import com.bj4.yhh.accountant.utils.BaseFragment;


public class MainActivity extends BaseActivity {
    private static final boolean DEBUG = true;
    private static int sContainerId;
    private static final String TAG = "MainActivity";
    public static final int REQUEST_EDIT_ACT_CONTENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sContainerId = R.id.container;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().replace(getMainFragmentContainerId(), new InitDataFragment()).commit();
    }

    public static int getMainFragmentContainerId() {
        return sContainerId;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG)
            Log.v(TAG, "onActivityResult, requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == REQUEST_EDIT_ACT_CONTENT) {
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            BaseFragment currentFragment = (BaseFragment) getFragmentManager().findFragmentById(getMainFragmentContainerId());
            if (currentFragment.onBackPress()) {
                return;
            }
            getFragmentManager().popBackStack();
        }
    }
}
