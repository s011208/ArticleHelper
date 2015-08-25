package com.bj4.yhh.accountant.fragments;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.fragments.plan.Plan;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/8/23.
 */
public class SimplePlanListAdapter extends BaseAdapter {

    private final ArrayList<Plan> mPlans = new ArrayList<Plan>();
    private final Context mContext;
    private final int mTextSize;
    private final int mTextPadding;

    public SimplePlanListAdapter(Context context) {
        mContext = context;
        mTextSize = context.getResources().getInteger(R.integer.review_mode_fragment_text_size);
        mTextPadding = context.getResources().getDimensionPixelSize(R.dimen.review_mode_fragment_padding);
        updateContent();
    }

    public void updateContent() {
        mPlans.clear();
        mPlans.addAll(Plan.query(mContext));
    }

    @Override
    public int getCount() {
        return mPlans.size();
    }

    @Override
    public Plan getItem(int position) {
        return mPlans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextView(mContext);
            final TextView text = (TextView) convertView;
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
            text.setPadding(mTextPadding, mTextPadding, mTextPadding, mTextPadding);
        }

        final TextView text = (TextView) convertView;
        text.setText(getItem(position).getActTitle());

        return convertView;
    }
}
