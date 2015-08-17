package com.bj4.yhh.accountant.fragments.plan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewSwitcher;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.utils.BaseFragment;

/**
 * Created by yenhsunhuang on 15/8/17.
 */
public class PlanModeFragment extends BaseFragment implements View.OnClickListener {
    private PlanListAdapter mPlanListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.plan_mode_fragment, null);
        Button addNewPlan = (Button) root.findViewById(R.id.add_new_plan);
        addNewPlan.setOnClickListener(this);
        Button removeAllPlans = (Button) root.findViewById(R.id.remove_all_plans);
        removeAllPlans.setOnClickListener(this);

        ViewSwitcher topArea = (ViewSwitcher) root.findViewById(R.id.top_area);
        ListView planList = (ListView) root.findViewById(R.id.plan_list);
        mPlanListAdapter = new PlanListAdapter(getActivity());
        planList.setAdapter(mPlanListAdapter);

        return root;
    }

    @Override
    public boolean onBackPress() {
        return false;
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.add_new_plan) {
        } else if (vId == R.id.remove_all_plans) {
        }
    }
}
