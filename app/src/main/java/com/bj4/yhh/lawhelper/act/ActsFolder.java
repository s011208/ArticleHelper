package com.bj4.yhh.lawhelper.act;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bj4.yhh.lawhelper.database.ActDatabase;
import com.bj4.yhh.lawhelper.database.ActProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/31.
 */
public class ActsFolder {
    private static final String JSON_ACTS_ID_KEY = "act_id";
    public long mId = ActDatabase.NO_ID;
    public String mTitle;
    public boolean mIsDefault = false;
    public ArrayList<Long> mActIds;

    public ActsFolder(String title) {
        mTitle = title;
    }

    public ActsFolder(long id, String title, ArrayList<Long> actIds, boolean isDefault) {
        mId = id;
        mTitle = title;
        mActIds = actIds;
        mIsDefault = isDefault;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ActDatabase.ID, mId);
            json.put(ActDatabase.ACT_FOLDER_TITLE, mTitle);
            json.put(ActDatabase.ACTS_ID, encodeActIds(mActIds));
            json.put(ActDatabase.IS_DEFAULT_ITEM, mIsDefault);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        if (mId != ActDatabase.NO_ID) {
            cv.put(ActDatabase.ID, mId);
        }
        cv.put(ActDatabase.ACT_FOLDER_TITLE, mTitle);
        cv.put(ActDatabase.ACTS_ID, encodeActIds(mActIds));
        cv.put(ActDatabase.IS_DEFAULT_ITEM, (mIsDefault ? ActDatabase.TRUE : ActDatabase.FALSE));
        return cv;
    }

    private static Uri getBaseUri() {
        return Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ACTS_FOLDER);
    }

    public static void delete(Context context, ActsFolder folder) {
        context.getContentResolver().delete(getBaseUri(), ActDatabase.ID + "=" + folder.mId, null);
    }

    public static boolean insertOrUpdate(Context context, ActsFolder folder) {
        if (folder.mId == ActDatabase.NO_ID) {
            return insert(context, folder);
        } else {
            return update(context, folder);
        }
    }

    private static boolean insert(Context context, ActsFolder folder) {
        long rtnId = ContentUris.parseId(context.getContentResolver().insert(getBaseUri(), folder.toContentValues()));
        if (rtnId != -1) {
            folder.mId = rtnId;
            return true;
        } else {
            return false;
        }
    }

    public static void removeActsFolderContentById(Context context, long actsFolderId, long actId) {
        ArrayList<ActsFolder> items = query(context, null, ActDatabase.ID + "=" + actsFolderId, null, null);
        if (items.size() != 1) {
            throw new RuntimeException("updateActsFolderContentById with wrong item size: " + items.size());
        }
        ActsFolder folder = items.get(0);
        folder.mActIds.remove(actId);
        update(context, folder);
    }

    public static void updateActsFolderContentById(Context context, long actsFolderId, long actId) {
        ArrayList<ActsFolder> items = query(context, null, ActDatabase.ID + "=" + actsFolderId, null, null);
        if (items.size() != 1) {
            throw new RuntimeException("updateActsFolderContentById with wrong item size: " + items.size());
        }
        ActsFolder folder = items.get(0);
        folder.mActIds.add(actId);
        update(context, folder);
    }

    private static boolean update(Context context, ActsFolder folder) {
        int rtn = context.getContentResolver().update(getBaseUri(), folder.toContentValues(), ActDatabase.ID + "=" + folder.mId, null);
        if (rtn > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static ActsFolder queryActsFolderById(Context context, long folderId) {
        ArrayList<ActsFolder> folders = query(context, null, ActDatabase.ID + "=" + folderId, null, null);
        if (folders.size() > 1)
            throw new RuntimeException("folderId: " + folderId + " more than one");
        if (folders.isEmpty())
            return null;
        return folders.get(0);
    }

    public static ArrayList<ActsFolder> query(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final ArrayList<ActsFolder> rtn = new ArrayList<ActsFolder>();
        Cursor data = context.getContentResolver().query(getBaseUri(), projection, selection, selectionArgs, sortOrder);
        if (data != null) {
            try {
                final int indexOfId = data.getColumnIndex(ActDatabase.ID);
                final int indexOfFolderTitle = data.getColumnIndex(ActDatabase.ACT_FOLDER_TITLE);
                final int indexOfActs = data.getColumnIndex(ActDatabase.ACTS_ID);
                final int indexOfDefaultItem = data.getColumnIndex(ActDatabase.IS_DEFAULT_ITEM);
                while (data.moveToNext()) {
                    ActsFolder folder = new ActsFolder(data.getLong(indexOfId), data.getString(indexOfFolderTitle), decodeActIds(data.getString(indexOfActs)), data.getInt(indexOfDefaultItem) == ActDatabase.TRUE);
                    rtn.add(folder);
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }

    public static final ArrayList<Long> decodeActIds(String actIdsString) {
        final ArrayList<Long> rtn = new ArrayList<Long>();
        try {
            JSONArray jArray = new JSONArray(actIdsString);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                rtn.add(jObject.getLong(JSON_ACTS_ID_KEY));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public static final String encodeActIds(ArrayList<Long> actIds) {
        JSONArray jArray = new JSONArray();
        if (actIds == null || actIds.isEmpty()) {
            return jArray.toString();
        }
        for (Long actId : actIds) {
            JSONObject jObject = new JSONObject();
            try {
                jObject.put(JSON_ACTS_ID_KEY, actId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jArray.put(jObject);
        }
        return jArray.toString();
    }
}
