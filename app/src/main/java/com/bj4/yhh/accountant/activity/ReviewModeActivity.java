package com.bj4.yhh.accountant.activity;

import android.os.Bundle;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.fragments.SimpleActFragment;
import com.bj4.yhh.accountant.fragments.display.actcontent.DisplayActContentFragment;

/**
 * Created by yenhsunhuang on 15/8/23.
 */
public class ReviewModeActivity extends BaseActivity implements SimpleActFragment.Callback {
    private static int sContainerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sContainerId = R.id.container;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_mode);
        SimpleActFragment frag = new SimpleActFragment();
        Bundle args = new Bundle();
        args.putInt(SimpleActFragment.ARGUS_TITLE_RESOURCE, R.string.review_mode_fragment_title);
        frag.setArguments(args);
        getFragmentManager().beginTransaction().replace(getMainFragmentContainerId(), frag).commit();
    }

    public static int getMainFragmentContainerId() {
        return sContainerId;
    }

    @Override
    public void onActClicked(Act act) {
        DisplayActContentFragment frag = new DisplayActContentFragment();
        Bundle args = new Bundle();
        args.putString(DisplayActContentFragment.ARGUMENT_JSON_ACT, act.toString());
        args.putInt(DisplayActContentFragment.ARGUS_DISPLAY_TYPE, DisplayActContentFragment.ARGUS_DISPLAY_TYPE_REVIEW_MODE);
        frag.setArguments(args);
        getFragmentManager().beginTransaction().replace(getMainFragmentContainerId(), frag).commit();
    }
}
