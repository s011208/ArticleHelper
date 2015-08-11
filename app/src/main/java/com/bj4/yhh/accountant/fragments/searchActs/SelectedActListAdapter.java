package com.bj4.yhh.accountant.fragments.searchActs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.Act;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/9.
 */
public class SelectedActListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private final ArrayList<Act> mData = new ArrayList<Act>();

    public SelectedActListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Act getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.selected_act_list, null);
            holder.mTitle = (TextView) convertView.findViewById(R.id.act_title);
            holder.mStatusSwitcher = (ViewSwitcher) convertView.findViewById(R.id.act_loading_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Act item = getItem(position);
        holder.mTitle.setText(item.getTitle());
        holder.mStatusSwitcher.setDisplayedChild(item.hasLoadedSuccess() ? 1 : 0);
        return convertView;
    }

    private void getData() {
        mData.clear();
        mData.addAll(Act.query(mContext, null, null, null, null));
    }

    @Override
    public void notifyDataSetChanged() {
        getData();
        super.notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView mTitle;

        ViewSwitcher mStatusSwitcher;
    }
}
