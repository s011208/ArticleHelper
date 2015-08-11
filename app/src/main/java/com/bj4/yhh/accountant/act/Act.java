package com.bj4.yhh.accountant.act;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.database.ActProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by yenhsunhuang on 15/4/13.
 */
public class Act implements Comparable<Act> {
    private long mId = ActDatabase.NO_ID;
    private String mTitle;
    private String mAmendedDate;
    private String mCategory;
    private boolean mHasLoadSuccess = false;
    private final ArrayList<Chapter> mChapters = new ArrayList<Chapter>();

    public Act(String title) {
        this(title, null, null);
    }

    public Act(String title, String amendedDate, String category) {
        mTitle = title;
        mAmendedDate = amendedDate;
        mCategory = category;
    }

    public Act(String title, String amendedDate, String category, long id, int hasLoadSuccess) {
        mTitle = title;
        mAmendedDate = amendedDate;
        mCategory = category;
        mId = id;
        mHasLoadSuccess = (hasLoadSuccess == ActDatabase.TRUE);
    }

    public Act(ContentValues cv) {
        this(cv.getAsString(ActDatabase.TITLE), cv.getAsString(ActDatabase.AMENDED_DATE), cv.getAsString(ActDatabase.CATEGORY));
        if (cv.containsKey(ActDatabase.ID)) {
            mId = cv.getAsLong(ActDatabase.ID);
        }
    }

    public Act(JSONObject json) {
        try {
            mTitle = json.getString(ActDatabase.TITLE);
            mAmendedDate = json.getString(ActDatabase.AMENDED_DATE);
            mCategory = json.getString(ActDatabase.CATEGORY);
            mId = json.getLong(ActDatabase.ID);
            mHasLoadSuccess = json.getInt(ActDatabase.HAS_LOAD_SUCCESS) == ActDatabase.TRUE;
        } catch (JSONException e) {
        }
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAmendedDate() {
        return mAmendedDate;
    }

    public String getCategory() {
        return mCategory;
    }

    public ArrayList<Chapter> getChapters() {
        return mChapters;
    }

    public boolean hasLoadedSuccess() {
        return mHasLoadSuccess;
    }

    public void addChapter(Chapter chapter) {
        mChapters.add(chapter);
        Collections.sort(mChapters);
    }

    public void setChapters(ArrayList<Chapter> chapters) {
        mChapters.clear();
        mChapters.addAll(chapters);
    }

    public ContentValues toContentValues() {
        ContentValues rtn = new ContentValues();
        rtn.put(ActDatabase.AMENDED_DATE, mAmendedDate);
        rtn.put(ActDatabase.TITLE, mTitle);
        rtn.put(ActDatabase.CATEGORY, mCategory);
        rtn.put(ActDatabase.HAS_LOAD_SUCCESS, mHasLoadSuccess ? ActDatabase.TRUE : ActDatabase.FALSE);
        if (mId != ActDatabase.NO_ID)
            rtn.put(ActDatabase.ID, mId);
        return rtn;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ActDatabase.AMENDED_DATE, mAmendedDate);
            json.put(ActDatabase.TITLE, mTitle);
            json.put(ActDatabase.CATEGORY, mCategory);
            json.put(ActDatabase.HAS_LOAD_SUCCESS, mHasLoadSuccess ? ActDatabase.TRUE : ActDatabase.FALSE);
            json.put(ActDatabase.ID, mId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    @Override
    public int compareTo(Act another) {
        return getTitle().compareTo(another.getTitle());
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public static ArrayList<Act> query(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final ArrayList<Act> rtn = new ArrayList<Act>();
        Cursor allActList = context.getContentResolver().query(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ACTS), projection, selection, selectionArgs, sortOrder);
        if (allActList != null) {
            try {
                final int indexOfId = allActList.getColumnIndex(ActDatabase.ID);
                final int indexOfTitle = allActList.getColumnIndex(ActDatabase.TITLE);
                final int indexOfAmendedDate = allActList.getColumnIndex(ActDatabase.AMENDED_DATE);
                final int indexOfCategory = allActList.getColumnIndex(ActDatabase.CATEGORY);
                final int indexOfLoaded = allActList.getColumnIndex(ActDatabase.HAS_LOAD_SUCCESS);
                while (allActList.moveToNext()) {
                    rtn.add(new Act(allActList.getString(indexOfTitle), allActList.getString(indexOfAmendedDate), allActList.getString(indexOfCategory), allActList.getLong(indexOfId), allActList.getInt(indexOfLoaded)));
                }
            } finally {
                allActList.close();
            }
        }
        return rtn;
    }

    public static int update(Context context, ContentValues values, String selection, String[] selectionArgs) {
        int rtn = context.getContentResolver().update(getBaseUri(), values, selection, selectionArgs);
        return rtn;
    }

    public static long getActId(Context context, String title) {
        long rtn = -1;
        Cursor data = context.getContentResolver().query(getBaseUri(), null, ActDatabase.TITLE + "='" + title + "'", null, null);
        if (data != null) {
            try {
                if (data.moveToFirst()) {
                    rtn = data.getLong(data.getColumnIndex(ActDatabase.ID));
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }

    private static Uri getBaseUri() {
        return Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ACTS);
    }

    public static void queryAllActContent(Context context, Act act) {
        ArrayList<Chapter> chapters = act.getChapters();
        chapters.clear();
        chapters.addAll(Chapter.queryChapterByActId(context, act.mId));
        for (Chapter chapter : chapters) {
            Chapter.queryAllChapterContent(context, chapter);
        }
    }

    public static void deleteAct(Context context, Act act) {
        if (act == null)
            return;
        // delete act
        int result = context.getContentResolver().delete(getBaseUri(), ActDatabase.ID + "=" + act.mId, null);
        if (result <= 0) {
            return;
        }
        // delete chapters
        queryAllActContent(context, act);
        for (Chapter chapter : act.getChapters()) {
            Chapter.delete(context, chapter);
            // delete articles
            Article.deleteByChapterId(context, chapter.mId);
        }
    }
}
