package com.bj4.yhh.lawhelper.fragments.plan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewSwitcher;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.activity.MainActivity;
import com.bj4.yhh.lawhelper.activity.TestActivity;
import com.bj4.yhh.lawhelper.dialogs.OutlineDialogFragment;
import com.bj4.yhh.lawhelper.utils.BaseFragment;
import com.bj4.yhh.lawhelper.utils.BaseToast;
import com.bj4.yhh.lawhelper.utils.dialogs.ConfirmDialogFragment;

/**
 * Created by yenhsunhuang on 15/8/17.
 */
public class PlanModeFragment extends BaseFragment implements View.OnClickListener, PlanListAdapter.Callback {
    private static final String TAG = "PlanModeFragment";
    private static final boolean DEBUG = true;
    private PlanListAdapter mPlanListAdapter;
    private int mDeleteItem = -1;
    private Plan mOnLongClickPlan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.plan_mode_fragment, null);
        Button addNewPlan = (Button) root.findViewById(R.id.add_new_plan);
        addNewPlan.setOnClickListener(this);
        Button removeAllPlans = (Button) root.findViewById(R.id.remove_all_plans);
        removeAllPlans.setOnClickListener(this);

        ViewSwitcher topArea = (ViewSwitcher) root.findViewById(R.id.top_area);
        ListView planList = (ListView) root.findViewById(R.id.plan_list);
        mPlanListAdapter = new PlanListAdapter(getActivity(), this);
        planList.setAdapter(mPlanListAdapter);
        planList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Plan plan = mPlanListAdapter.getItem(i);
                if (plan.mTotalPlanProgress == plan.mCurrentPlanProgress) {
                    BaseToast.showToast(getActivity(), getActivity().getResources().getString(R.string.plan_mode_finish_plan_toast));
                    return;
                }
                Intent intent = new Intent(getActivity(), TestActivity.class);
                intent.putExtra(TestActivity.EXTRA_PLAN, plan.toString());
                getActivity().startActivity(intent);
            }
        });
        planList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mDeleteItem = i;
                mOnLongClickPlan = mPlanListAdapter.getItem(i);
                ConfirmDialogFragment dialog = new ConfirmDialogFragment();
                Bundle args = new Bundle();
                args.putString(ConfirmDialogFragment.ARGUS_TITLE, getTitle());
                args.putString(ConfirmDialogFragment.ARGUS_MESSAGE, getMessageText());
                dialog.setArguments(args);
                dialog.setTargetFragment(PlanModeFragment.this, ConfirmDialogFragment.REQUEST_CONFIRM);
                dialog.show(getFragmentManager(), dialog.getClass().getName());
                return true;
            }
        });

        return root;
    }

    @Override
    public boolean onBackPress() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlanListAdapter != null) {
            mPlanListAdapter.updateContent();
            mPlanListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.add_new_plan) {
            if (DEBUG) {
                Log.d(TAG, "add_new_plan");
            }
            getFragmentManager().beginTransaction().replace(MainActivity.getMainFragmentContainerId(), new AddPlanFragment()).addToBackStack(null).commitAllowingStateLoss();
        } else if (vId == R.id.remove_all_plans) {
            if (DEBUG) {
                Log.d(TAG, "remove_all_plans");
            }
            mDeleteItem = -1;
            ConfirmDialogFragment dialog = new ConfirmDialogFragment();
            Bundle args = new Bundle();
            args.putString(ConfirmDialogFragment.ARGUS_TITLE, getTitle());
            args.putString(ConfirmDialogFragment.ARGUS_MESSAGE, getMessageText());
            dialog.setArguments(args);
            dialog.setTargetFragment(PlanModeFragment.this, ConfirmDialogFragment.REQUEST_CONFIRM);
            dialog.show(getFragmentManager(), dialog.getClass().getName());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConfirmDialogFragment.REQUEST_CONFIRM) {
            if (resultCode == Activity.RESULT_OK) {
                onConfirm();
            } else {
                onCancel();
            }
        }
    }

    @Override
    public void onOutlineButtonClick(Act act) {
        OutlineDialogFragment.showDialog(act, getFragmentManager());
    }

    private void onConfirm() {
        if (mDeleteItem == -1) {
            Plan.deleteAllPlans(getActivity());
            if (mPlanListAdapter != null) {
                mPlanListAdapter.updateContent();
                mPlanListAdapter.notifyDataSetChanged();
            }
        } else if (mDeleteItem >= 0) {
            Plan.deletePlan(getActivity(), mOnLongClickPlan.mId);
            if (mPlanListAdapter != null) {
                mPlanListAdapter.updateContent();
                mPlanListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void onCancel() {

    }

    private String getMessageText() {
        if (mDeleteItem == -1) {
            return getActivity().getResources().getString(R.string.confirm_dialog_fragment_delete_all_message);
        } else if (mDeleteItem >= 0) {
            return getActivity().getResources().getString(R.string.confirm_dialog_fragment_delete_plan_message, mOnLongClickPlan.getActTitle());
        }
        return null;
    }

    private String getTitle() {
        if (mDeleteItem == -1) {
            return getActivity().getResources().getString(R.string.confirm_dialog_fragment_delete_all_title);
        } else if (mDeleteItem >= 0) {
            return getActivity().getResources().getString(R.string.confirm_dialog_fragment_delete_plan_title);
        }
        return null;
    }
}
