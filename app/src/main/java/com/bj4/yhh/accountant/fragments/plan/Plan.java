package com.bj4.yhh.accountant.fragments.plan;

import android.content.ContentValues;
import android.content.Context;

import com.bj4.yhh.accountant.act.Act;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yenhsunhuang on 15/8/18.
 */
public class Plan {
    public static final String ID = "_id";
    public static final String ACT_ID = "act_id";
    public static final String PLAN_ORDER = "plan_order";
    public static final String TOTAL_PLAN_PROGRESS = "total_plan_progress";
    public static final String CURRENT_PLAN_PROGRESS = "current_plan_progress";
    public static final String TOTAL_ITEMS = "total_items";
    public static final String FINISHED_ITEM = "finished_item";

    public static final long NO_ID = -1;
    public long mId;
    public long mActId;
    public int mPlanOrderBy;
    public int mTotalPlanProgress;
    public int mCurrentPlanProgress;
    public int mTotalItems;
    public int mFinishedItem;

    private Act mAct;

    public Plan(long actId, int orderBy, int totalProgress, int currentProgress, int totalItem, int finishedItem) {
        this(NO_ID, actId, orderBy, totalProgress, currentProgress, totalItem, finishedItem);
    }

    public Plan(long id, long actId, int orderBy, int totalProgress, int currentProgress, int totalItem, int finishedItem) {
        mId = id;
        mActId = actId;
        mPlanOrderBy = orderBy;
        mTotalPlanProgress = totalProgress;
        mCurrentPlanProgress = currentProgress;
        mTotalItems = totalItem;
        mFinishedItem = finishedItem;

    }

    public void initAct(Context context) {
        if (mId != NO_ID) {
            mAct = Act.queryActById(context, mId);
        }
    }

    public String getActTitle() {
        if (mAct == null) return null;
        return mAct.getTitle();
    }

    public Plan(JSONObject json) throws JSONException {
        this(json.getLong(ID), json.getLong(ACT_ID), json.getInt(PLAN_ORDER),
                json.getInt(TOTAL_PLAN_PROGRESS), json.getInt(CURRENT_PLAN_PROGRESS),
                json.getInt(TOTAL_ITEMS), json.getInt(FINISHED_ITEM));
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ID, mId);
            json.put(ACT_ID, mActId);
            json.put(PLAN_ORDER, mPlanOrderBy);
            json.put(TOTAL_PLAN_PROGRESS, mTotalPlanProgress);
            json.put(CURRENT_PLAN_PROGRESS, mCurrentPlanProgress);
            json.put(TOTAL_ITEMS, mTotalItems);
            json.put(FINISHED_ITEM, mFinishedItem);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ID, mId);
        cv.put(ACT_ID, mActId);
        cv.put(PLAN_ORDER, mPlanOrderBy);
        cv.put(TOTAL_PLAN_PROGRESS, mTotalPlanProgress);
        cv.put(CURRENT_PLAN_PROGRESS, mCurrentPlanProgress);
        cv.put(TOTAL_ITEMS, mTotalItems);
        cv.put(FINISHED_ITEM, mFinishedItem);
        return cv;
    }
}
