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
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
