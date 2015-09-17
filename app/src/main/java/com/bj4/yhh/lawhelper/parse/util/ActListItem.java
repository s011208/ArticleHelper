package com.bj4.yhh.lawhelper.parse.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bj4.yhh.lawhelper.database.ActDatabase;
import com.bj4.yhh.lawhelper.database.ActProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/6.
 */
public class ActListItem {
    public String mUrl, mTitle, mAmendedDate, mCategory;

    public long mId = -1;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ActListItem)) return false;
        ActListItem other = (ActListItem) o;
        return other.mUrl.equals(mUrl) && other.mTitle.equals(mTitle) && other.mCategory.equals(mCategory);
    }

    public ActListItem(String url, String title, String amendedDate, String category) {
        this(-1, url, title, amendedDate, category);
    }

    public ActListItem(long id, String url, String title, String amendedDate, String category) {
        mId = id;
        mUrl = url;
        mTitle = title;
        mAmendedDate = amendedDate;
        mCategory = category;
    }

    public ActListItem(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            if (json.has(ActDatabase.ID)) {
                mId = json.getLong(ActDatabase.ID);
            }
            mUrl = json.getString(ActDatabase.URL);
            mTitle = json.getString(ActDatabase.TITLE);
            mAmendedDate = json.getString(ActDatabase.AMENDED_DATE);
            mCategory = json.getString(ActDatabase.CATEGORY);
        } catch (JSONException e) {
        }
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ActDatabase.ID, mId);
            json.put(ActDatabase.URL, mUrl);
            json.put(ActDatabase.TITLE, mTitle);
            json.put(ActDatabase.AMENDED_DATE, mAmendedDate);
            json.put(ActDatabase.CATEGORY, mCategory);
        } catch (JSONException e) {
        }
        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public ContentValues getContentValues() {
        final ContentValues cv = new ContentValues();
        cv.put(ActDatabase.URL, mUrl);
        cv.put(ActDatabase.TITLE, mTitle);
        cv.put(ActDatabase.AMENDED_DATE, mAmendedDate);
        cv.put(ActDatabase.CATEGORY, mCategory);
        return cv;
    }

    public static ArrayList<ActListItem> queryFromProvider(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final ArrayList<ActListItem> rtn = new ArrayList<ActListItem>();
        Cursor allActList = context.getContentResolver().query(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST), projection, selection, selectionArgs, sortOrder);
        if (allActList != null) {
            try {
                final int indexOfId = allActList.getColumnIndex(ActDatabase.ID);
                final int indexOfUrl = allActList.getColumnIndex(ActDatabase.URL);
                final int indexOfTitle = allActList.getColumnIndex(ActDatabase.TITLE);
                final int indexOfAmendedDate = allActList.getColumnIndex(ActDatabase.AMENDED_DATE);
                final int indexOfCategory = allActList.getColumnIndex(ActDatabase.CATEGORY);
                while (allActList.moveToNext()) {
                    rtn.add(new ActListItem(allActList.getLong(indexOfId), allActList.getString(indexOfUrl), allActList.getString(indexOfTitle), allActList.getString(indexOfAmendedDate), allActList.getString(indexOfCategory)));
                }
            } finally {
                allActList.close();
            }
        }
        return rtn;
    }
}
