package com.bj4.yhh.lawhelper.fragments.entry;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bj4.yhh.lawhelper.R;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/8/15.
 */
public class MainEntryGridAdapter extends BaseAdapter {
    private static final String TAG = "MainEntryGridAdapter";
    private static final boolean DEBUG = true;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ArrayList<GridMenuItem> mData = new ArrayList<GridMenuItem>();

    public static final int ADD_NEW_ACT = 0;
    public static final int PLAN_MODE = 1;
    public static final int REVIEW_MODE = 2;
    public static final int TEST_MODE = 3;
    public static final int UPDATE_ACTS = 4;

    private static final int[] MENU_TEXT_RESOURCES = new int[]{
            R.string.menu_list_items_text_manage_act,
            R.string.menu_list_items_text_plan_mode,
            R.string.menu_list_items_text_review_mode,
            R.string.menu_list_items_text_test_mode,
            R.string.menu_list_items_text_update_acts,
    };
    private static final int[] MENU_ICON_RESOURCES = new int[]{
            R.drawable.add_new_act,
            R.drawable.plan_mode,
            R.drawable.review_mode,
            R.drawable.test_mode,
            R.drawable.update_acts,
    };

    public MainEntryGridAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Resources res = mContext.getResources();
        for (int i = 0; i < MENU_TEXT_RESOURCES.length; i++) {
            mData.add(new GridMenuItem(res.getString(MENU_TEXT_RESOURCES[i % MENU_TEXT_RESOURCES.length]), res.getDrawable(MENU_ICON_RESOURCES[i % MENU_ICON_RESOURCES.length])));
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public GridMenuItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_entry_fragment_item, null);
            holder = new ViewHolder();
            holder.mMenuText = (TextView) convertView.findViewById(R.id.menu_text);
            holder.mMenuIcon = (ImageView) convertView.findViewById(R.id.menu_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final GridMenuItem item = getItem(position);
        holder.mMenuText.setText(item.getMenuTitle());
        holder.mMenuIcon.setImageDrawable(item.getMenuIcon());
        return convertView;
    }

    private static class ViewHolder {
        TextView mMenuText;
        ImageView mMenuIcon;
    }
}
