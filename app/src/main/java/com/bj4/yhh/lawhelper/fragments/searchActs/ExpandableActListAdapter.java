package com.bj4.yhh.lawhelper.fragments.searchActs;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.act.ActsFolder;
import com.bj4.yhh.lawhelper.database.ActDatabase;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/31.
 */
public class ExpandableActListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "ExpandableActList";
    private static final boolean DEBUG = false;
    private final Context mContext;
    private final LayoutInflater mInflater;
    /**
     * title
     */
    private final ArrayList<ActsFolder> mActsFolders = new ArrayList<ActsFolder>();
    private final ArrayList<Act> mAllActs = new ArrayList<Act>();
    /**
     * content
     */
    private final ArrayList<ArrayList<Act>> mActsGroupList = new ArrayList<ArrayList<Act>>();

    public ExpandableActListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        init();
    }

    public void updateContent() {
        init();
    }

    private void init() {
        mActsFolders.clear();
        mActsFolders.addAll(ActsFolder.query(mContext, null, null, null, ActDatabase.ACT_FOLDER_TITLE));
        mAllActs.clear();
        mAllActs.addAll(Act.query(mContext, null, null, null, ActDatabase.TITLE));
        mActsGroupList.clear();
        for (ActsFolder folder : mActsFolders) {
            ArrayList<Act> actList = new ArrayList<Act>();
            for (long actId : folder.mActIds) {
                Act act = getActById(actId);
                if (act != null) {
                    actList.add(act);
                }
            }
            mActsGroupList.add(actList);
        }
        if (DEBUG) {
            Log.d(TAG, "getGroupCount(): " + getGroupCount());
            for (int i = 0; i < getGroupCount(); i++) {
                Log.d(TAG, "group: " + i + ", getChildrenCount(): " + getChildrenCount(i));
            }
        }
    }

    private Act getActById(long id) {
        for (Act act : mAllActs) {
            if (act.getId() == id) {
                return act;
            }
        }
        return null;
    }

    @Override
    public int getGroupCount() {
        return mActsFolders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mActsGroupList.get(groupPosition).size();
    }

    @Override
    public ActsFolder getGroup(int groupPosition) {
        return mActsFolders.get(groupPosition);
    }

    @Override
    public Act getChild(int groupPosition, int childPosition) {
        return mActsGroupList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return Integer.valueOf(String.valueOf(groupPosition) + String.valueOf(childPosition));
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        if (convertView == null) {
            holder = new GroupViewHolder();
            convertView = mInflater.inflate(R.layout.expandable_folder_group_view, null);
            holder.mTitle = (TextView) convertView.findViewById(R.id.group_txt);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }
        ActsFolder folder = getGroup(groupPosition);
        holder.mTitle.setText(folder.mTitle);
        return convertView;
    }

    private static class GroupViewHolder {
        TextView mTitle;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder = null;
        if (convertView == null) {
            holder = new ChildViewHolder();
            convertView = mInflater.inflate(R.layout.expandable_folder_child_view, null);
            holder.mTitle = (TextView) convertView.findViewById(R.id.child_txt);
            holder.mStatusSwitcher = (ViewSwitcher) convertView.findViewById(R.id.act_loading_status);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }
        Act child = getChild(groupPosition, childPosition);
        holder.mTitle.setText(child.getTitle());
        holder.mStatusSwitcher.setDisplayedChild(child.hasLoadedSuccess() ? 1 : 0);
        return convertView;
    }

    private static class ChildViewHolder {
        TextView mTitle;
        ViewSwitcher mStatusSwitcher;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
