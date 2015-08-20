package com.bj4.yhh.accountant.activity;

import android.os.Bundle;
import android.util.Log;

import com.bj4.yhh.accountant.fragments.plan.Plan;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yen-Hsun_Huang on 2015/8/20.
 */
public class TestActivity extends BaseActivity {
    private static final boolean DEBUG = true;
    private static final String TAG = "TestActivity";

    public static final String EXTRA_PLAN = "extra_plan";

    private Plan mPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mPlan = new Plan(new JSONObject(getIntent().getStringExtra(EXTRA_PLAN)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (DEBUG) {
            Log.d(TAG, "Plan: " + mPlan);
        }
    }
}
