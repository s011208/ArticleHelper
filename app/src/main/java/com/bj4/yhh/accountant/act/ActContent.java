package com.bj4.yhh.accountant.act;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.SpannableString;

import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.database.ActProvider;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yenhsunhuang on 15/7/19.
 */
public abstract class ActContent implements Comparable<ActContent> {
    public long mId;
    public String mNumber;
    public int mOrder;
    public String mContent;
    public boolean mHasHighLight;
    public boolean mHasTextNote, mHasImageNote;
    public SpannableString mSpannableContent;

    public ActContent(String number, String content, int order, long id, boolean hasHightLight) {
        mNumber = number;
        mContent = content;
        mOrder = order;
        mId = id;
        mHasHighLight = hasHightLight;
    }

    public ActContent(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            mId = json.getLong(ActDatabase.ID);
            mNumber = json.getString(ActDatabase.NUMBER);
            mContent = json.getString(ActDatabase.CONTENT);
            mOrder = json.getInt(ActDatabase.COLUMN_ORDER);
            mHasHighLight = json.getBoolean(ActDatabase.HIGHLIGHT);
        } catch (JSONException e) {
        }
    }

    public void resetDisplayContent() {
        mSpannableContent = new SpannableString(mContent);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ActDatabase.ID, mId);
            json.put(ActDatabase.NUMBER, mNumber);
            json.put(ActDatabase.CONTENT, mContent);
            json.put(ActDatabase.COLUMN_ORDER, mOrder);
            json.put(ActDatabase.HIGHLIGHT, mHasHighLight);
            json.put(ActDatabase.HAS_TEXT_NOTE, mHasTextNote);
            json.put(ActDatabase.HAS_IMAGE_NOTE, mHasImageNote);
        } catch (JSONException e) {
        }
        return json;
    }

    public void updateNoteStatus(Context context) {
        final int parentType = (this instanceof Chapter) ? ActDatabase.NOTE_PARENT_TYPE_CHAPTER : ActDatabase.NOTE_PARENT_TYPE_ARTICLE;
        Cursor textNote = Note.getNoteData(context, ActDatabase.NOTE_TYPE_TEXT, parentType, mId);
        if (textNote != null) {
            try {
                mHasTextNote = textNote.getCount() > 0;
            } finally {
                textNote.close();
            }
        }
        Cursor imageNote = Note.getNoteData(context, ActDatabase.NOTE_TYPE_IMAGE, parentType, mId);
        if (imageNote != null) {
            try {
                mHasImageNote = imageNote.getCount() > 0;
            } finally {
                imageNote.close();
            }
        }
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public int compareTo(ActContent another) {
        if (mOrder < another.mOrder)
            return -1;
        else if (mOrder == another.mOrder)
            return 0;
        else return 1;
    }


    public boolean updateHighLight(Context context, boolean highLight) {
        mHasHighLight = highLight;
        if (mId == ActDatabase.NO_ID) {
            return false;
        }
        Uri uri = getBaseUriByInstance();
        if (uri == null) {
            return false;
        }
        ContentValues cv = new ContentValues();
        cv.put(ActDatabase.HIGHLIGHT, mHasHighLight);
        int rtn = context.getContentResolver().update(uri, cv, ActDatabase.ID + "=" + mId, null);
        return rtn > 0;
    }

    private Uri getBaseUriByInstance() {
        Uri uri = null;
        if (this instanceof Chapter) {
            uri = Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_CHAPTERS);
        } else if (this instanceof Article) {
            uri = Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ARTICLES);
        }
        return uri;
    }
}
