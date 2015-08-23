package com.bj4.yhh.accountant.activity;

import android.os.Bundle;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.fragments.review.ReviewModeFragment;

/**
 * Created by yenhsunhuang on 15/8/23.
 */
public class ReviewModeActivity extends BaseActivity {
    private static int sContainerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sContainerId = R.id.container;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_mode);
        getFragmentManager().beginTransaction().replace(getMainFragmentContainerId(), new ReviewModeFragment()).commit();

    }

    public static int getMainFragmentContainerId() {
        return sContainerId;
    }
}
