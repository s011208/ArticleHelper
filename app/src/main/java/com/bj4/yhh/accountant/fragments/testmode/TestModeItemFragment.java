package com.bj4.yhh.accountant.fragments.testmode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.fragments.SimplePlanListAdapter;
import com.bj4.yhh.accountant.utils.BaseFragment;

/**
 * Created by yenhsunhuang on 15/8/25.
 */
public class TestModeItemFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.test_mode_item_fragment, null);
        TextView text = (TextView) root.findViewById(R.id.title);
        text.setText(R.string.test_mode_fragment_title);
        ListView list = (ListView) root.findViewById(R.id.test_act_list);
        final SimplePlanListAdapter adapter = new SimplePlanListAdapter(getActivity());
        list.setAdapter(adapter);
        return root;
    }

    @Override
    public boolean onBackPress() {
        return false;
    }
}
