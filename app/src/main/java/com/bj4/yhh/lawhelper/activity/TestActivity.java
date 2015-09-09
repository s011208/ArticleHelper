package com.bj4.yhh.lawhelper.activity;

import android.os.Bundle;
import android.util.Log;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.fragments.plan.Plan;
import com.bj4.yhh.lawhelper.fragments.test.TestItem;
import com.bj4.yhh.lawhelper.fragments.test.TestItemFragment;
import com.bj4.yhh.lawhelper.fragments.testmode.TestModeItemFragment;
import com.bj4.yhh.lawhelper.utils.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Yen-Hsun_Huang on 2015/8/20.
 */
public class TestActivity extends BaseActivity implements TestItemFragment.Callback {
    private static final boolean DEBUG = true;
    private static final String TAG = "TestActivity";

    public static final String EXTRA_PLAN = "extra_plan";

    private int mTestType = TestModeItemFragment.TEST_TYPE_BY_DEFAULT;

    private static int sContainerId;
    private Plan mPlan;
    private ArrayList<TestItem> mTestItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sContainerId = R.id.container;
        super.onCreate(savedInstanceState);
        try {
            mPlan = new Plan(new JSONObject(getIntent().getStringExtra(EXTRA_PLAN)));
            mPlan.initAct(this);
            mTestItems = TestItem.queryTestItemByPlan(this, mPlan);
            mTestType = getIntent().getIntExtra(TestModeItemFragment.EXTRA_TEST_TYPE, TestModeItemFragment.TEST_TYPE_BY_DEFAULT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (DEBUG) {
            Log.d(TAG, "Plan: " + mPlan);
        }
        setContentView(R.layout.activity_test);
        getFragmentManager().beginTransaction().replace(getMainFragmentContainerId(), new TestItemFragment()).commitAllowingStateLoss();
    }

    @Override
    public Plan getPlan() {
        return mPlan;
    }

    @Override
    public ArrayList<TestItem> getTestItems() {
        return mTestItems;
    }

    @Override
    public int getTestType() {
        return mTestType;
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

    public static int getMainFragmentContainerId() {
        return sContainerId;
    }
}
