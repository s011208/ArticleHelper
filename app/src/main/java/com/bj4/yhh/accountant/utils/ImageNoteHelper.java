package com.bj4.yhh.accountant.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.ActContent;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;
import com.bj4.yhh.accountant.act.Note;
import com.bj4.yhh.accountant.database.ActDatabase;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/25.
 */
public class ImageNoteHelper {

    private final Context mContext;
    private final ActContent mActContent;
    private final int mParentType;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private final ArrayList<Note> mAllData = new ArrayList<Note>();

    public ActContent getActContent() {
        return mActContent;
    }

    public int getParentType() {
        return mParentType;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public DisplayImageOptions getDisplayImageOptions() {
        return mOptions;
    }

    public ArrayList<Note> getAllImageNotes() {
        return mAllData;
    }

    public ImageNoteHelper(ActContent content, Context context) {
        mActContent = content;
        mContext = context;
        if (mActContent instanceof Chapter) {
            mParentType = ActDatabase.NOTE_PARENT_TYPE_CHAPTER;
        } else if (mActContent instanceof Article) {
            mParentType = ActDatabase.NOTE_PARENT_TYPE_ARTICLE;
        } else {
            mParentType = -1;
        }
        mImageLoader = ImageLoader.getInstance();
        mOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnFail(R.drawable.emoticon_sad)
                .cacheOnDisc(true).resetViewBeforeLoading(true).build();
    }

    public void setMaximumImageSize(final int w, final int h) {
        mOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnFail(R.drawable.emoticon_sad)
                .cacheOnDisc(true).resetViewBeforeLoading(true).postProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bmp) {
                        if (bmp.getHeight() <= h && bmp.getWidth() <= w)
                            return bmp;
                        return Bitmap.createScaledBitmap(bmp, w, h, false);
                    }
                }).build();
    }

    public void setImageSize(final int w, final int h) {
        mOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnFail(R.drawable.emoticon_sad)
                .cacheOnDisc(true).resetViewBeforeLoading(true).postProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bmp) {
                        return Bitmap.createScaledBitmap(bmp, w, h, false);
                    }
                }).build();
    }

    public void init() {
        mAllData.clear();
        Cursor data = Note.getNoteData(mContext, ActDatabase.NOTE_TYPE_IMAGE, mParentType, getActContent().mId);
        if (data != null) {
            try {
                final int idIndex = data.getColumnIndex(ActDatabase.ID);
                final int noteParentTypeIndex = data.getColumnIndex(ActDatabase.NOTE_PARENT_TYPE);
                final int noteParentTypeIdIndex = data.getColumnIndex(ActDatabase.NOTE_PARENT_ID);
                final int noteTypeIndex = data.getColumnIndex(ActDatabase.NOTE_TYPE);
                final int noteContentIndex = data.getColumnIndex(ActDatabase.NOTE_CONTENT);
                while (data.moveToNext()) {
                    mAllData.add(new Note(data.getLong(idIndex), data.getInt(noteParentTypeIndex), data.getLong(noteParentTypeIdIndex), data.getInt(noteTypeIndex), data.getString(noteContentIndex)));
                }
            } finally {
                data.close();
            }
        }
    }
}
