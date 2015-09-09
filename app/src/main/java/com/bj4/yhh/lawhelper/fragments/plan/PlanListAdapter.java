package com.bj4.yhh.lawhelper.fragments.plan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/8/18.
 */
public class PlanListAdapter extends BaseAdapter {
    public interface Callback {
        void onOutlineButtonClick(Act act);
    }

    private Callback mCallback;

    private final ArrayList<Plan> mData = new ArrayList<>();
    private final Context mContext;
    private final LayoutInflater mInflater;

    public PlanListAdapter(Context context, Callback cb) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCallback = cb;
        updateContent();
    }

    public void updateContent() {
        mData.clear();
        mData.addAll(Plan.query(mContext));
        for (Plan plan : mData) {
            plan.initAct(mContext);
        }
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
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.plan_mode_fragment_list_item, null);
            holder.mActTitle = (TextView) convertView.findViewById(R.id.act_title);
            holder.mOrderText = (TextView) convertView.findViewById(R.id.plan_order);
            holder.mOutlineButton = (ImageView) convertView.findViewById(R.id.outline_btn);
            holder.mActItem = (TextView) convertView.findViewById(R.id.act_item);
            holder.mPlanDay = (TextView) convertView.findViewById(R.id.plan_day);
            holder.mPlanProgress = (ProgressBar) convertView.findViewById(R.id.plan_progress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Plan item = getItem(position);

        holder.mActTitle.setText(item.getActTitle());
        int orderTextRes = 0;
        switch (item.mPlanOrderBy) {
            case Plan.ORDER_BY_ARTICLE:
                orderTextRes = R.string.add_plan_fragment_order_by_article;
                break;
            case Plan.ORDER_BY_RANDOM:
                orderTextRes = R.string.add_plan_fragment_order_by_random;
                break;
            default:
                orderTextRes = R.string.add_plan_fragment_order_by_article;
        }
        holder.mOrderText.setText(orderTextRes);

        holder.mActItem.setText(item.mFinishedItem + " / " + item.mTotalItems);
        holder.mPlanDay.setText(item.mCurrentPlanProgress + " / " + item.mTotalPlanProgress);
        holder.mPlanProgress.setMax(item.mTotalItems);
        holder.mPlanProgress.setProgress(item.mFinishedItem);
        holder.mOutlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Act planAct = item.getAct();
                if (mCallback != null) {
                    mCallback.onOutlineButtonClick(planAct);
                }
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        TextView mActTitle;
        TextView mOrderText;
        ImageView mOutlineButton;
        TextView mActItem;
        TextView mPlanDay;
        ProgressBar mPlanProgress;
    }
}
