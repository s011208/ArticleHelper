package com.bj4.yhh.lawhelper.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.utils.BaseFragment;

/**
 * Created by yenhsunhuang on 15/8/23.
 */
public class SimpleActFragment extends BaseFragment {
    public interface Callback {
        void onActClicked(Act act);
    }

    public static final String ARGUS_TITLE_RESOURCE = "argus_title_resource";
    public static final String EXTRA_CLICK_ITEM = "extra_click_item";
    private static final boolean DEBUG = true;
    private static final String TAG = "SimpleActFragment";

    private SimpleActListAdapter mSimpleActListAdapter;

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final int titleResource = getArguments().getInt(ARGUS_TITLE_RESOURCE, -1);
        View root = inflater.inflate(R.layout.review_mode_fragment, null);
        ListView list = (ListView) root.findViewById(R.id.all_act_list);
        mSimpleActListAdapter = new SimpleActListAdapter(getActivity());
        list.setAdapter(mSimpleActListAdapter);
        TextView mTitle = (TextView) root.findViewById(R.id.title);
        if (titleResource != -1) {
            mTitle.setText(titleResource);
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Act act = mSimpleActListAdapter.getItem(i);
                if (mCallback != null) {
                    mCallback.onActClicked(act);
                    if (DEBUG) Log.d(TAG, "act: " + act);
                } else {
                    if (getTargetFragment() != null) {
                        Intent intent = getActivity().getIntent();
                        intent.putExtra(EXTRA_CLICK_ITEM, act.toString());
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    }
                }
            }
        });
        return root;
    }

    @Override
    public boolean onBackPress() {
        return false;
    }
}
