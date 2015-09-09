package com.bj4.yhh.lawhelper.activity;

import android.content.Intent;
import android.os.Bundle;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.act.ActContent;
import com.bj4.yhh.lawhelper.fragments.SimpleActFragment;
import com.bj4.yhh.lawhelper.fragments.display.actcontent.DisplayActContentFragment;

/**
 * Created by yenhsunhuang on 15/8/23.
 */
public class ReviewModeActivity extends BaseActivity implements SimpleActFragment.Callback, DisplayActContentFragment.Callback {
    private static int sContainerId;

    public static final String RESULT_SELECTED_ARTICLE = "result_selected_article";

    public static final String EXTRA_KEY_MODE = "extra_key_mode";
    public static final int EXTRA_MODE_SELECT_ARTICLE = 1000;
    public static final int EXTRA_MODE_NORMAL = 0;
    private int mMode = EXTRA_MODE_NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sContainerId = R.id.container;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_mode);
        mMode = getIntent().getIntExtra(EXTRA_KEY_MODE, EXTRA_MODE_NORMAL);
        SimpleActFragment frag = new SimpleActFragment();
        Bundle args = new Bundle();
        args.putInt(SimpleActFragment.ARGUS_TITLE_RESOURCE, R.string.review_mode_fragment_title);
        frag.setArguments(args);
        getFragmentManager().beginTransaction().replace(getMainFragmentContainerId(), frag).commitAllowingStateLoss();
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
        args.putInt(DisplayActContentFragment.ARGUS_CLICK_MODE, (mMode == EXTRA_MODE_NORMAL ? DisplayActContentFragment.ARGUS_CLICK_MODE_NORMAL : DisplayActContentFragment.ARGUS_CLICK_MODE_GET_LINKS));
        frag.setArguments(args);
        getFragmentManager().beginTransaction().replace(getMainFragmentContainerId(), frag).commitAllowingStateLoss();
    }

    @Override
    public void onActContentSelected(ActContent actContent) {
        Intent rtn = getIntent();
        if (rtn == null) {
            rtn = new Intent();
        }
        rtn.putExtra(RESULT_SELECTED_ARTICLE, actContent.toString());
        setResult(RESULT_OK, rtn);
        finish();
    }
}
