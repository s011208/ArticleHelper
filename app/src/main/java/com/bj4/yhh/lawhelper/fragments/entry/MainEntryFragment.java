package com.bj4.yhh.lawhelper.fragments.entry;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.activity.ReviewModeActivity;
import com.bj4.yhh.lawhelper.activity.TestModeActivity;
import com.bj4.yhh.lawhelper.fragments.plan.PlanModeFragment;
import com.bj4.yhh.lawhelper.fragments.searchActs.SearchActFragment;
import com.bj4.yhh.lawhelper.utils.BaseFragment;

/**
 * Created by yenhsunhuang on 15/8/12.
 */
public class MainEntryFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainEntryFragment";
    private static final boolean DEBUG = true;

    private View mRoot;
    private GridView mGridView;
    private MainEntryGridAdapter mMainEntryGridAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.main_entry_fragment, null);
        mGridView = (GridView) mRoot.findViewById(R.id.main_entry_grid_view);
        mMainEntryGridAdapter = new MainEntryGridAdapter(getActivity());
        mGridView.setAdapter(mMainEntryGridAdapter);
        mGridView.setOnItemClickListener(this);
        return mRoot;
    }

    @Override
    public boolean onBackPress() {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (DEBUG) {
            Log.d(TAG, "onItemClick, position: " + position);
        }
        switch (position) {
            case MainEntryGridAdapter.ADD_NEW_ACT:
                getFragmentManager().beginTransaction().replace(R.id.container, new SearchActFragment()).addToBackStack(null).commitAllowingStateLoss();
                break;
            case MainEntryGridAdapter.PLAN_MODE:
                getFragmentManager().beginTransaction().replace(R.id.container, new PlanModeFragment()).addToBackStack(null).commitAllowingStateLoss();
                break;
            case MainEntryGridAdapter.REVIEW_MODE:
                Intent startReviewModeIntent = new Intent(getActivity(), ReviewModeActivity.class);
                startActivity(startReviewModeIntent);
                break;
            case MainEntryGridAdapter.TEST_MODE:
                Intent startTestModeIntent = new Intent(getActivity(), TestModeActivity.class);
                startActivity(startTestModeIntent);
                break;
            case MainEntryGridAdapter.UPDATE_ACTS:
                break;
        }
    }
}
