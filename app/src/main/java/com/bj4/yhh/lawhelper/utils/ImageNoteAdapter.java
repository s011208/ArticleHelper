package com.bj4.yhh.lawhelper.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.bj4.yhh.lawhelper.act.ActContent;
import com.bj4.yhh.lawhelper.act.Note;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/25.
 */
public abstract class ImageNoteAdapter extends BaseAdapter {
    private static final boolean DEBUG = false;
    private static final String TAG = "ImageNoteAdapter";

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ImageNoteHelper mImageNoteHelper;


    public Context getContext() {
        return mContext;
    }

    public LayoutInflater getLayoutInflater() {
        return mInflater;
    }

    public ActContent getActContent() {
        return mImageNoteHelper.getActContent();
    }

    public int getParentType() {
        return mImageNoteHelper.getParentType();
    }

    public ImageNoteHelper getImageNoteHelper() {
        return mImageNoteHelper;
    }

    public ImageLoader getImageLoader() {
        return mImageNoteHelper.getImageLoader();
    }

    public DisplayImageOptions getDisplayImageOptions() {
        return mImageNoteHelper.getDisplayImageOptions();
    }

    public ArrayList<Note> getAllImageNotes() {
        return mImageNoteHelper.getAllImageNotes();
    }

    public ImageNoteAdapter(ActContent content, Context context) {
        mImageNoteHelper = new ImageNoteHelper(content, context);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void init() {
        mImageNoteHelper.init();
    }
}
