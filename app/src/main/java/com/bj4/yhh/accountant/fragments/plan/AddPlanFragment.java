package com.bj4.yhh.accountant.fragments.plan;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.utils.BaseFragment;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/8/18.
 */
public class AddPlanFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "AddPlanFragment";
    private static final boolean DEBUG = true;

    private Act mSelectedAct;
    private int mTotalPlanDay = 7;
    private int mOrderBy = Plan.ORDER_BY_ARTICLE;
    private int mTotalArticleCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.add_plan_fragment, null);
        Button btnOk = (Button) root.findViewById(R.id.ok);
        btnOk.setOnClickListener(this);
        Button btnCancel = (Button) root.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(this);

        final TextView articleCount = (TextView) root.findViewById(R.id.total_article_counr);
        Spinner actSpinner = (Spinner) root.findViewById(R.id.act_spinner);
        final ArrayList<Act> acts = Act.query(getActivity(), null, null, null, null);
        final ArrayList<String> actsName = new ArrayList<String>();
        for (Act act : acts) {
            actsName.add(act.getTitle());
        }
        if (!acts.isEmpty())
            mSelectedAct = acts.get(0);
        ArrayAdapter<String> actSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, actsName);
        actSpinner.setAdapter(actSpinnerAdapter);
        actSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedAct = acts.get(position);
                if (DEBUG) {
                    Log.d(TAG, "onItemSelected, act: " + mSelectedAct.getTitle());
                }
                mTotalArticleCount = Act.getArticleCount(getActivity(), mSelectedAct);
                articleCount.setText(String.valueOf(mTotalArticleCount));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        EditText totalPlanDays = (EditText) root.findViewById(R.id.total_day);
        totalPlanDays.setText(String.valueOf(mTotalPlanDay));
        totalPlanDays.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    mTotalPlanDay = Integer.valueOf(s.toString());
                } catch (Exception e) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Spinner orderSpinner = (Spinner) root.findViewById(R.id.act_order_spinner);
        final String[] orderSpinnerArray = getActivity().getResources().getStringArray(R.array.add_plan_fragment_order);
        ArrayAdapter<String> orderSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, orderSpinnerArray);
        orderSpinner.setAdapter(orderSpinnerAdapter);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG) {
                    Log.d(TAG, "order by: " + orderSpinnerArray[position]);
                }
                mOrderBy = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return root;
    }

    @Override
    public boolean onBackPress() {
        return false;
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.ok) {
            if (DEBUG) {
                Log.d(TAG, "ok");
            }
            final int totalProgress = mTotalArticleCount / mTotalPlanDay + 1;
            Plan plan = new Plan(mSelectedAct.getId(), mOrderBy, totalProgress, 0, mTotalArticleCount, 0);
            if (DEBUG) Log.v(TAG, "plan: " + plan.toString());
            Plan.insertOrUpdate(getActivity(), plan);
            getActivity().onBackPressed();
        } else if (vId == R.id.cancel) {
            if (DEBUG) {
                Log.d(TAG, "cancel");
            }
            getActivity().onBackPressed();
        }
    }
}
