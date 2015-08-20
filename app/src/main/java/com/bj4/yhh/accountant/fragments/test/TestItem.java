package com.bj4.yhh.accountant.fragments.test;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.bj4.yhh.accountant.database.PlanProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Yen-Hsun_Huang on 2015/8/20.
 */
public class TestItem {
    public static final String ID = "_id";
    public static final String PLAN_ID = "plan_id";
    public static final String ACT_ID = "act_id";
    public static final String CHAPTER_ID = "chapter_id";
    public static final String ARTICLE_ID = "article_id";
    public static final String HAS_FAILED = "has_failed";
    public static final String IS_ANSWER = "is_answer";
    public static final String DISPLAY_DAY = "display_day";

    public static final int TRUE = 0;
    public static final int FALSE = 1;

    public static final long NO_ID = -1;
    public long mId = NO_ID;
    public long mPlanId = NO_ID;
    public long mActId = NO_ID;
    public long mChapterId = NO_ID;
    public long mArticleId = NO_ID;
    public boolean mHasFailed = false;
    public boolean mIsAnswer = false;
    public int mDisplayDay = -1;

    public TestItem(long planId, long actId, long chapterId, long articleId, int displayDay) {
        this(NO_ID, planId, actId, chapterId, articleId, false, false, displayDay);
    }

    public TestItem(long id, long planId, long actId, long chapterId, long articleId, boolean hasFailed, boolean isAnswer, int displayDay) {
        mId = id;
        mPlanId = planId;
        mActId = actId;
        mChapterId = chapterId;
        mArticleId = articleId;
        mHasFailed = hasFailed;
        mIsAnswer = isAnswer;
        mDisplayDay = displayDay;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        if (mId != NO_ID) {
            cv.put(ID, mId);
        }
        cv.put(PLAN_ID, mPlanId);
        cv.put(ACT_ID, mActId);
        cv.put(CHAPTER_ID, mChapterId);
        cv.put(ARTICLE_ID, mArticleId);
        cv.put(HAS_FAILED, mHasFailed ? TRUE : FALSE);
        cv.put(IS_ANSWER, mIsAnswer ? TRUE : FALSE);
        cv.put(DISPLAY_DAY, mDisplayDay);
        return cv;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ID, mId);
            json.put(PLAN_ID, mPlanId);
            json.put(ACT_ID, mActId);
            json.put(CHAPTER_ID, mChapterId);
            json.put(ARTICLE_ID, mArticleId);
            json.put(HAS_FAILED, mHasFailed ? TRUE : FALSE);
            json.put(IS_ANSWER, mIsAnswer ? TRUE : FALSE);
            json.put(DISPLAY_DAY, mDisplayDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public static Uri getBaseUri() {
        return Uri.parse("content://" + PlanProvider.AUTHORITY + "/" + PlanProvider.PATH_TEST_ITEM);
    }

    public static int bulkInsert(Context context, ArrayList<TestItem> items) {
        ContentValues[] cvs = new ContentValues[items.size()];
        for (int i = 0; i < items.size(); i++) {
            final TestItem item = items.get(i);
            cvs[i] = item.toContentValues();
        }
        return bulkInsert(context, cvs);
    }

    public static int bulkInsert(Context context, ContentValues[] items) {
        return context.getContentResolver().bulkInsert(getBaseUri(), items);
    }
}
