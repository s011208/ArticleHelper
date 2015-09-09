package com.bj4.yhh.lawhelper.fragments;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/8/23.
 */
public class SimpleActListAdapter extends BaseAdapter {

    private final ArrayList<Act> mActs = new ArrayList<Act>();
    private final Context mContext;
    private final int mTextSize;
    private final int mTextPadding;

    public SimpleActListAdapter(Context context) {
        mContext = context;
        mTextSize = context.getResources().getInteger(R.integer.review_mode_fragment_text_size);
        mTextPadding = context.getResources().getDimensionPixelSize(R.dimen.review_mode_fragment_padding);
        updateContent();
    }

    public void updateContent() {
        mActs.clear();
        mActs.addAll(Act.query(mContext, null, null, null, null));
    }

    @Override
    public int getCount() {
        return mActs.size();
    }

    @Override
    public Act getItem(int position) {
        return mActs.get(position);
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
        text.setText(getItem(position).getTitle());

        return convertView;
    }
}
