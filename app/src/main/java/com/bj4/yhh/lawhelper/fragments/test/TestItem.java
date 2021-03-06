package com.bj4.yhh.lawhelper.fragments.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bj4.yhh.lawhelper.database.PlanProvider;
import com.bj4.yhh.lawhelper.fragments.plan.Plan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Yen-Hsun_Huang on 2015/8/20.
 */
public class TestItem {

    public static final String DELETE_ITEM_STRING = "（刪除）";

    public static final String ID = "_id";
    public static final String PLAN_ID = "plan_id";
    public static final String ACT_ID = "act_id";

    // TODO change chapter id to array list
    /**
     * may have more than one chapter
     */
    public static final String CHAPTER_IDS = "chapter_id";
    public static final String ARTICLE_ID = "article_id";
    public static final String FAILED_TIME = "failed_time";
    public static final String IS_READ = "is_read";
    public static final String IS_ANSWER = "is_answer";
    public static final String DISPLAY_DAY = "display_day";

    public static final int TRUE = 0;
    public static final int FALSE = 1;

    public static final long NO_ID = -1;
    public long mId = NO_ID;
    public long mPlanId = NO_ID;
    public long mActId = NO_ID;
    public ArrayList<Long> mChapterIds;
    public long mArticleId = NO_ID;
    public int mFailedTime = 0;
    public boolean mIsRead = false;
    public boolean mIsAnswer = false;
    public int mDisplayDay = -1;

    public static String convertJsonFromChapterIds(ArrayList<Long> ids) {
        JSONArray json = new JSONArray();
        for (Long id : ids) {
            json.put(id);
        }
        return json.toString();
    }

    public static ArrayList<Long> convertChapterIdsFromJson(String jsonArrayString) {
        final ArrayList<Long> rtn = new ArrayList<Long>();
        try {
            JSONArray json = new JSONArray(jsonArrayString);
            for (int i = 0; i < json.length(); i++) {
                rtn.add(json.getLong(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public TestItem(long planId, long actId, ArrayList<Long> chapterIds, long articleId, int displayDay) {
        this(NO_ID, planId, actId, chapterIds, articleId, 0, false, false, displayDay);
    }

    public TestItem(long id, long planId, long actId, ArrayList<Long> chapterIds, long articleId, int failedTime, boolean isAnswer, boolean isRead, int displayDay) {
        mId = id;
        mPlanId = planId;
        mActId = actId;
        mChapterIds = chapterIds;
        mArticleId = articleId;
        mFailedTime = failedTime;
        mIsAnswer = isAnswer;
        mIsRead = isRead;
        mDisplayDay = displayDay;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        if (mId != NO_ID) {
            cv.put(ID, mId);
        }
        cv.put(PLAN_ID, mPlanId);
        cv.put(ACT_ID, mActId);
        cv.put(CHAPTER_IDS, convertJsonFromChapterIds(mChapterIds));
        cv.put(ARTICLE_ID, mArticleId);
        cv.put(FAILED_TIME, mFailedTime);
        cv.put(IS_ANSWER, mIsAnswer ? TRUE : FALSE);
        cv.put(IS_READ, mIsRead ? TRUE : FALSE);
        cv.put(DISPLAY_DAY, mDisplayDay);
        return cv;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ID, mId);
            json.put(PLAN_ID, mPlanId);
            json.put(ACT_ID, mActId);
            json.put(CHAPTER_IDS, convertJsonFromChapterIds(mChapterIds));
            json.put(ARTICLE_ID, mArticleId);
            json.put(FAILED_TIME, mFailedTime);
            json.put(IS_ANSWER, mIsAnswer ? TRUE : FALSE);
            json.put(IS_READ, mIsRead ? TRUE : FALSE);
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

    public static void deleteAllTestItem(Context context) {
        context.getContentResolver().delete(getBaseUri(), null, null);
    }

    public static void deleteTestItemByPlanId(Context context, long planId) {
        context.getContentResolver().delete(getBaseUri(), PLAN_ID + "=" + planId, null);
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

    public static ArrayList<TestItem> queryTestItemByPlan(Context context, Plan plan) {
        return queryTestItem(context, null, PLAN_ID + "=" + plan.mId, null, DISPLAY_DAY);
    }

    public static void update(Context context, TestItem item) {
        context.getContentResolver().update(getBaseUri(), item.toContentValues(), ID + "=" + item.mId, null);
    }

    public static ArrayList<TestItem> queryTestItem(Context context, String[] projection, String selection, String[] selectionArgus, String sortOrder) {
        final ArrayList<TestItem> rtn = new ArrayList<TestItem>();
        Cursor data = context.getContentResolver().query(getBaseUri(), projection, selection, selectionArgus, sortOrder);
        if (data != null) {
            try {
                final int indexOfId = data.getColumnIndex(ID);
                final int indexOfPlanId = data.getColumnIndex(PLAN_ID);
                final int indexOfActId = data.getColumnIndex(ACT_ID);
                final int indexOfChapterId = data.getColumnIndex(CHAPTER_IDS);
                final int indexOfArticleId = data.getColumnIndex(ARTICLE_ID);
                final int indexOfHasFailed = data.getColumnIndex(FAILED_TIME);
                final int indexOfIsAnswer = data.getColumnIndex(IS_ANSWER);
                final int indexOfIsRead = data.getColumnIndex(IS_READ);
                final int indexOfDisplayDay = data.getColumnIndex(DISPLAY_DAY);
                while (data.moveToNext()) {
                    rtn.add(new TestItem(data.getLong(indexOfId)
                            , data.getLong(indexOfPlanId)
                            , data.getLong(indexOfActId)
                            , convertChapterIdsFromJson(data.getString(indexOfChapterId))
                            , data.getLong(indexOfArticleId)
                            , data.getInt(indexOfHasFailed)
                            , data.getInt(indexOfIsAnswer) == TRUE
                            , data.getInt(indexOfIsRead) == TRUE
                            , data.getInt(indexOfDisplayDay)));
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }
}
