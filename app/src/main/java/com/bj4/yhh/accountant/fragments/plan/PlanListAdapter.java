package com.bj4.yhh.accountant.fragments.plan;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/8/18.
 */
public class PlanListAdapter extends BaseAdapter {
    private final ArrayList<Plan> mData = new ArrayList<>();
    private final Context mContext;

    public PlanListAdapter(Context context) {
        mContext = context;
        updateContent();
    }

    private void updateContent() {
        mData.clear();
        mData.addAll(Plan.query(mContext));
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Plan getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return new View(mContext);
    }
}
