package com.bj4.yhh.accountant.act;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.database.ActProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/24.
 */
public class Note {
    private static final String TAG = "Note";
    private static final boolean DEBUG = false;

    public long mId = ActDatabase.NO_ID;
    public int mNoteParentType;
    public long mNoteParentId;
    public int mNoteType;
    public String mNoteContent;

    private boolean mIsMore = false;

    public Note(int parentType, long parentId, int noteType, String noteContent) {
        mNoteParentType = parentType;
        mNoteParentId = parentId;
        mNoteType = noteType;
        mNoteContent = noteContent;
    }

    public Note(long id, int parentType, long parentId, int noteType, String noteContent) {
        this(parentType, parentId, noteType, noteContent);
        mId = id;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ActDatabase.ID, mId);
            json.put(ActDatabase.NOTE_PARENT_TYPE, mNoteParentType);
            json.put(ActDatabase.NOTE_PARENT_ID, mNoteParentId);
            json.put(ActDatabase.NOTE_TYPE, mNoteType);
            json.put(ActDatabase.NOTE_CONTENT, mNoteContent);
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
        if (mId != ActDatabase.NO_ID) {
            cv.put(ActDatabase.ID, mId);
        }
        cv.put(ActDatabase.NOTE_PARENT_TYPE, mNoteParentType);
        cv.put(ActDatabase.NOTE_PARENT_ID, mNoteParentId);
        cv.put(ActDatabase.NOTE_TYPE, mNoteType);
        cv.put(ActDatabase.NOTE_CONTENT, mNoteContent);
        return cv;
    }

    public static boolean insertOrUpdate(Context context, Note note) {
        if (DEBUG) {
            Log.d(TAG, "note: " + note.toString());
        }
        if (note.mId == ActDatabase.NO_ID) {
            return insert(context, note);
        } else {
            return update(context, note);
        }
    }

    private static boolean insert(Context context, Note note) {
        long rtnId = ContentUris.parseId(context.getContentResolver().insert(getBaseUri(), note.toContentValues()));
        if (rtnId != -1) {
            note.mId = rtnId;
            return true;
        } else {
            return false;
        }
    }

    private static boolean update(Context context, Note note) {
        int rtn = context.getContentResolver().update(getBaseUri(), note.toContentValues(), ActDatabase.ID + "=" + note.mId, null);
        if (rtn > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void delete(Context context, Note note) {
        context.getContentResolver().delete(getBaseUri(), ActDatabase.ID + "=" + note.mId, null);
    }

    public static Uri getBaseUri() {
        return Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_NOTES);
    }

    public boolean isEmptyContent() {
        return mId == ActDatabase.NO_ID;
    }

    public void setIsMore() {
        mIsMore = true;
    }

    public boolean isMore() {
        return mIsMore;
    }

    public void setImageContent(String imageContent) {
        mNoteType = ActDatabase.NOTE_TYPE_IMAGE;
        mNoteContent = imageContent;
    }

    public static Cursor getNoteData(Context context, int noteType, int parentType, long parentId) {
        return context.getContentResolver().query(Note.getBaseUri(), null, ActDatabase.NOTE_TYPE + "=" + noteType + " and " + ActDatabase.NOTE_PARENT_TYPE + "=" + parentType + " and " + ActDatabase.NOTE_PARENT_ID + "=" + parentId, null, ActDatabase.ID);
    }

    public static void deleteAllNotesByActId(Context context, long actId) {
        Act act = Act.queryActById(context, actId);
        deleteAllNotesByAct(context, act);
    }

    public static ArrayList<Long> queryNoteIdByParentId(Context context, int notParentType, long parentId) {
        ArrayList<Long> rtn = new ArrayList<Long>();
        Cursor data = context.getContentResolver().query(getBaseUri(), new String[]{ActDatabase.ID}, ActDatabase.NOTE_PARENT_TYPE + "=" + notParentType + " and " + ActDatabase.NOTE_PARENT_ID + "=" + parentId, null, null);
        if (data != null) {
            try {
                while (data.moveToNext()) {
                    rtn.add(data.getLong(0));
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }

    public static void deleteAllNotesByAct(final Context context, final Act act) {
        int deleteItems = 0;
        Act.queryAllActContent(context, act);
        for (Chapter chapter : act.getChapters()) {
            for (Article article : chapter.getArticles()) {
                ArrayList<Long> noteIds = queryNoteIdByParentId(context, ActDatabase.NOTE_PARENT_TYPE_ARTICLE, article.mId);
                for (long noteId : noteIds) {
                    deleteItems += context.getContentResolver().delete(getBaseUri(), ActDatabase.ID + "=" + noteId, null);
                }
                if (DEBUG)
                    Log.d(TAG, "article noteIds size: " + noteIds.size());
            }
            ArrayList<Long> noteIds = queryNoteIdByParentId(context, ActDatabase.NOTE_PARENT_TYPE_CHAPTER, chapter.mId);
            for (long noteId : noteIds) {
                deleteItems += context.getContentResolver().delete(getBaseUri(), ActDatabase.ID + "=" + noteId, null);
            }
            if (DEBUG) Log.d(TAG, "chapter noteIds size: " + noteIds.size());
        }
        ArrayList<Long> noteIds = queryNoteIdByParentId(context, ActDatabase.NOTE_PARENT_TYPE_ACT, act.getId());
        for (long noteId : noteIds) {
            deleteItems += context.getContentResolver().delete(getBaseUri(), ActDatabase.ID + "=" + noteId, null);
        }
        if (DEBUG) Log.d(TAG, "act noteIds size: " + noteIds.size());
        if (DEBUG) Log.d(TAG, "deleteItems: " + deleteItems);
    }
}
