package com.bj4.yhh.accountant.fragments.plan;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.database.PlanProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

    public static final int ORDER_BY_ARTICLE = 0;
    public static final int ORDER_BY_RANDOM = 1;

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

    public static void insertOrUpdate(Context context, Plan plan) {
        if (plan.mId == NO_ID) {
            insert(context, plan);
        } else {
            update(context, plan);
        }
    }

    private static void insert(Context context, Plan plan) {
        final long id = ContentUris.parseId(context.getContentResolver().insert(getBaseUri(), plan.toContentValues()));
        plan.mId = id;
    }

    private static void update(Context context, Plan plan) {
        context.getContentResolver().update(getBaseUri(), plan.toContentValues(), ID + "=" + plan.mId, null);
    }

    public void initAct(Context context) {
        if (mActId != ActDatabase.NO_ID) {
            mAct = Act.queryActById(context, mActId);
        }
    }

    public Act getAct() {
        return mAct;
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

    public static void deleteAllPlans(Context context) {
        context.getContentResolver().delete(getBaseUri(), null, null);
    }

    public static void deletePlan(Context context, long planId) {
        context.getContentResolver().delete(getBaseUri(), Plan.ID + "=" + planId, null);
    }


    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        if (mId != NO_ID) {
            cv.put(ID, mId);
        }
        cv.put(ACT_ID, mActId);
        cv.put(PLAN_ORDER, mPlanOrderBy);
        cv.put(TOTAL_PLAN_PROGRESS, mTotalPlanProgress);
        cv.put(CURRENT_PLAN_PROGRESS, mCurrentPlanProgress);
        cv.put(TOTAL_ITEMS, mTotalItems);
        cv.put(FINISHED_ITEM, mFinishedItem);
        return cv;
    }

    public static Uri getBaseUri() {
        return Uri.parse("content://" + PlanProvider.AUTHORITY + "/" + PlanProvider.PATH_PLAN);
    }

    public static ArrayList<Plan> query(Context context) {
        final ArrayList<Plan> rtn = new ArrayList<Plan>();
        Cursor data = context.getContentResolver().query(getBaseUri(), null, null, null, null, null);
        if (data != null) {
            try {
                final int indexOfId = data.getColumnIndex(ID);
                final int indexOfActId = data.getColumnIndex(ACT_ID);
                final int indexOfPlanOrder = data.getColumnIndex(PLAN_ORDER);
                final int indexOfTotalPlanProgress = data.getColumnIndex(TOTAL_PLAN_PROGRESS);
                final int indexOfCurrentPlanProgress = data.getColumnIndex(CURRENT_PLAN_PROGRESS);
                final int indexOfTotalItem = data.getColumnIndex(TOTAL_ITEMS);
                final int indexOfFinishedItem = data.getColumnIndex(FINISHED_ITEM);
                while (data.moveToNext()) {
                    Plan plan =
                            new Plan(data.getLong(indexOfId), data.getLong(indexOfActId), data.getInt(indexOfPlanOrder)
                                    , data.getInt(indexOfTotalPlanProgress), data.getInt(indexOfCurrentPlanProgress)
                                    , data.getInt(indexOfTotalItem), data.getInt(indexOfFinishedItem));
                    plan.initAct(context);
                    rtn.add(plan);
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }
}
