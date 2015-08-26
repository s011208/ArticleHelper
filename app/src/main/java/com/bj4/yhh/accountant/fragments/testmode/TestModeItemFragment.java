package com.bj4.yhh.accountant.fragments.testmode;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.activity.TestActivity;
import com.bj4.yhh.accountant.fragments.SimplePlanListAdapter;
import com.bj4.yhh.accountant.fragments.plan.Plan;
import com.bj4.yhh.accountant.utils.BaseFragment;

/**
 * Created by yenhsunhuang on 15/8/25.
 */
public class TestModeItemFragment extends BaseFragment {
    public static final int TEST_TYPE_BY_PLAN = 0;
    public static final int TEST_TYPE_BY_ALL = 1;
    public static final int TEST_TYPE_BY_ALL_RANDOMLY = 2;

    private static final int REQUEST_SELECT_TEST_TYPE = 1000;
    public static final String EXTRA_TEST_TYPE = "extra_test_type";

    private SimplePlanListAdapter mSimplePlanListAdapter;
    private int mSelectPlanIndex = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.test_mode_item_fragment, null);
        TextView text = (TextView) root.findViewById(R.id.title);
        text.setText(R.string.test_mode_fragment_title);
        ListView list = (ListView) root.findViewById(R.id.test_act_list);
        mSimplePlanListAdapter = new SimplePlanListAdapter(getActivity());
        list.setAdapter(mSimplePlanListAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectPlanIndex = position;
                DialogFragment dialog = new TestModeTypeDialog();
                dialog.setTargetFragment(TestModeItemFragment.this, REQUEST_SELECT_TEST_TYPE);
                dialog.show(getFragmentManager(), TestModeTypeDialog.class.getName());
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_TEST_TYPE) {
            if (resultCode == Activity.RESULT_OK) {
                final int testType = data.getIntExtra(EXTRA_TEST_TYPE, TEST_TYPE_BY_ALL);
                Plan plan = mSimplePlanListAdapter.getItem(mSelectPlanIndex);

                Intent startTestIntent = new Intent(getActivity(), TestActivity.class);
                startTestIntent.putExtra(TestActivity.EXTRA_PLAN, plan.toString());
                startActivity(startTestIntent);
            }
        }
    }

    @Override
    public boolean onBackPress() {
        return false;
    }
}
