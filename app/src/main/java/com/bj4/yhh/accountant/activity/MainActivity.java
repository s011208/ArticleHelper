package com.bj4.yhh.accountant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.fragments.entry.MainEntryFragment;
import com.bj4.yhh.accountant.fragments.searchActs.SearchActFragment;
import com.bj4.yhh.accountant.utils.BaseFragment;
import com.bj4.yhh.accountant.utils.SunProgressBar;


public class MainActivity extends BaseActivity {
    private static final boolean DEBUG = true;
    private static final String TAG = "MainActivity";
    public static final int REQUEST_EDIT_ACT_CONTENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().replace(R.id.container, new MainEntryFragment()).commit();
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
            BaseFragment currentFragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.container);
            if (currentFragment.onBackPress()) {
                return;
            }
            getFragmentManager().popBackStack();
        }
    }
}
