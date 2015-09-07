package com.bj4.yhh.accountant.act;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;

import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.database.ActProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/19.
 */
public abstract class ActContent implements Comparable<ActContent> {
    public long mId;
    public String mNumber;
    public int mOrder;
    public String mContent;
    public boolean mHasStar;
    public boolean mHasTextNote, mHasImageNote;
    public final ArrayList<String> mLinks = new ArrayList<String>();
    public SpannableString mSpannableContent;
    public int mFailedTime = -1;
    private int mDrawLineStart, mDrawLineEnd;

    public ActContent(String number, String content, int order, long id, boolean hasStar, ArrayList<String> links, int drawLineStart, int drawLineEnd) {
        mNumber = number;
        mContent = content;
        mOrder = order;
        mId = id;
        mHasStar = hasStar;
        mDrawLineStart = drawLineStart;
        mDrawLineEnd = drawLineEnd;
        if (links != null) {
            mLinks.addAll(links);
        }
        resetDisplayContent();
    }

    public static String convertLinksFromArray(ArrayList<String> links) {
        JSONArray array = new JSONArray();
        if (links == null) return array.toString();
        for (String link : links) {
            array.put(link);
        }
        return array.toString();
    }

    public static ArrayList<String> convertLinksFromJSON(String jsonArrayString) {
        final ArrayList<String> rtn = new ArrayList<String>();
        if (jsonArrayString == null) return rtn;
        try {
            JSONArray array = new JSONArray(jsonArrayString);
            for (int i = 0; i < array.length(); i++) {
                rtn.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public ActContent(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            mId = json.getLong(ActDatabase.ID);
            mNumber = json.getString(ActDatabase.NUMBER);
            mContent = json.getString(ActDatabase.CONTENT);
            mOrder = json.getInt(ActDatabase.COLUMN_ORDER);
            mHasStar = json.getBoolean(ActDatabase.HAS_STAR);
            mLinks.addAll(convertLinksFromJSON(json.getString(ActDatabase.LINKS)));
            mDrawLineStart = json.getInt(ActDatabase.DRAW_LINE_START);
            mDrawLineEnd = json.getInt(ActDatabase.DRAW_LINE_END);
        } catch (JSONException e) {
        }
    }

    public void resetDisplayContent() {
        mSpannableContent = new SpannableString(mContent);
        if (mDrawLineStart <= -1 || mDrawLineEnd <= 0) return;
        mSpannableContent.setSpan(new BackgroundColorSpan(Color.rgb(0xff, 0x99, 0x00)), mDrawLineStart, mDrawLineEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(ActDatabase.ID, mId);
            json.put(ActDatabase.NUMBER, mNumber);
            json.put(ActDatabase.CONTENT, mContent);
            json.put(ActDatabase.COLUMN_ORDER, mOrder);
            json.put(ActDatabase.HAS_STAR, mHasStar);
            json.put(ActDatabase.HAS_TEXT_NOTE, mHasTextNote);
            json.put(ActDatabase.HAS_IMAGE_NOTE, mHasImageNote);
            json.put(ActDatabase.LINKS, convertLinksFromArray(mLinks));
            json.put(ActDatabase.DRAW_LINE_START, mDrawLineStart);
            json.put(ActDatabase.DRAW_LINE_END, mDrawLineEnd);
            json.put("mFailedTime", mFailedTime);
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

    public void updateDrawLine(Context context, int drawLineStart, int drawLineEnd) {
        if (mId == ActDatabase.NO_ID) {
            return;
        }
        mDrawLineStart = drawLineStart;
        mDrawLineEnd = drawLineEnd;
        Uri uri = getBaseUriByInstance();
        if (uri == null) {
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put(ActDatabase.DRAW_LINE_START, mDrawLineStart);
        cv.put(ActDatabase.DRAW_LINE_END, mDrawLineEnd);
        context.getContentResolver().update(uri, cv, ActDatabase.ID + "=" + mId, null);
        resetDisplayContent();
    }

    public boolean updateHighLight(Context context, boolean highLight) {
        mHasStar = highLight;
        if (mId == ActDatabase.NO_ID) {
            return false;
        }
        Uri uri = getBaseUriByInstance();
        if (uri == null) {
            return false;
        }
        ContentValues cv = new ContentValues();
        cv.put(ActDatabase.HAS_STAR, mHasStar);
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
