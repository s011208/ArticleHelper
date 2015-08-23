package com.bj4.yhh.accountant.fragments.review;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.fragments.SimpleActListAdapter;
import com.bj4.yhh.accountant.utils.BaseFragment;

/**
 * Created by yenhsunhuang on 15/8/23.
 */
public class ReviewModeFragment extends BaseFragment {
    private static final boolean DEBUG = true;
    private static final String TAG = "ReviewModeFragment";

    private SimpleActListAdapter mSimpleActListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.review_mode_fragment, null);
        ListView list = (ListView) root.findViewById(R.id.all_act_list);
        mSimpleActListAdapter = new SimpleActListAdapter(getActivity());
        list.setAdapter(mSimpleActListAdapter);
        TextView mTitle = (TextView) root.findViewById(R.id.title);
        return root;
    }

    @Override
    public boolean onBackPress() {
        return false;
    }
}
