package com.bj4.yhh.accountant.fragments.display.actcontent.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.ActContent;
import com.bj4.yhh.accountant.act.Note;
import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.utils.ImageNoteAdapter;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/24.
 */
public class EditorImageNoteGridAdapter extends ImageNoteAdapter {
    private static final String TAG = "NoteGridAdapter";
    private static final boolean DEBUG = false;

    private final int mMaximumColumnCount;
    private final int mIconResourcePadding;

    private final ArrayList<Note> mAllData = new ArrayList<Note>();
    private final ArrayList<Note> mDisplayData = new ArrayList<Note>();

    public EditorImageNoteGridAdapter(ActContent content, Context context) {
        super(content, context);
        mIconResourcePadding = getContext().getResources().getDimensionPixelSize(R.dimen.activity_act_editor_grid_resource_icon_padding);
        mMaximumColumnCount = getContext().getResources().getInteger(R.integer.activity_act_editor_grid_column_count);
        init();
    }

    public void updateContent() {
        init();
    }

    public void init() {
        mDisplayData.clear();
        mAllData.clear();
        super.init();
        mAllData.addAll(getAllImageNotes());
        if (DEBUG) Log.d(TAG, "mAllData size: " + mAllData.size());
        if (mAllData.size() >= mMaximumColumnCount) {
            for (int i = 0; i < mMaximumColumnCount - 2; i++) {
                mDisplayData.add(mAllData.get(i));
            }
            mDisplayData.add(getAddNewNote());
            mDisplayData.add(getMoreNote());
        } else {
            mDisplayData.addAll(mAllData);
            mDisplayData.add(getAddNewNote());
        }
    }

    private Note getAddNewNote() {
        Note note = new Note(getParentType(), getActContent().mId, ActDatabase.NOTE_TYPE_IMAGE, "");
        return note;
    }

    private Note getMoreNote() {
        Note note = getAddNewNote();
        note.setIsMore();
        return note;
    }

    @Override
    public int getCount() {
        return mDisplayData.size();
    }

    @Override
    public Note getItem(int position) {
        return mDisplayData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.edit_act_note_grid_image_view, null);
            holder = new ViewHolder();
            holder.mImageNote = (ImageView) convertView.findViewById(R.id.note_image);
            holder.mImageNote.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.mSwitcher = (ViewSwitcher) convertView.findViewById(R.id.note_switcher);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Note item = getItem(position);
        if (DEBUG)
            Log.d(TAG, "position: " + position + ", item.mNoteContent: " + item.mNoteContent
                    + ", item.isMore(): " + item.isMore()
                    + ", item.isEmptyContent(): " + item.isEmptyContent() + "\nholder: " + holder);
        getImageLoader().cancelDisplayTask(holder.mImageNote);
        if (item.isMore()) {
            ((FrameLayout) convertView).setForeground(null);
            holder.mSwitcher.setDisplayedChild(1);
            holder.mImageNote.setImageResource(R.drawable.ic_grid);
            holder.mImageNote.setPadding(mIconResourcePadding, mIconResourcePadding, mIconResourcePadding, mIconResourcePadding);
        } else if (item.isEmptyContent()) {
            ((FrameLayout) convertView).setForeground(null);
            holder.mSwitcher.setDisplayedChild(1);
            holder.mImageNote.setImageResource(R.drawable.ic_add_more);
            holder.mImageNote.setPadding(mIconResourcePadding, mIconResourcePadding, mIconResourcePadding, mIconResourcePadding);
        } else {
            ((FrameLayout) convertView).setForeground(getContext().getResources().getDrawable(R.drawable.editor_image_note_grid_foreground));
            holder.mImageNote.setPadding(0, 0, 0, 0);
            final ViewHolder finalHolder = holder;
            final Runnable imageTask = new Runnable() {
                @Override
                public void run() {
                    getImageLoader().displayImage(item.mNoteContent, finalHolder.mImageNote, getDisplayImageOptions(), new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {
                            finalHolder.mSwitcher.setDisplayedChild(0);
                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {
                            finalHolder.mSwitcher.setDisplayedChild(1);
                            finalHolder.mImageNote.setPadding(mIconResourcePadding, mIconResourcePadding, mIconResourcePadding, mIconResourcePadding);
                        }

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            finalHolder.mSwitcher.setDisplayedChild(1);
                            if (DEBUG) Log.v(TAG, "finalHolder: " + finalHolder);
                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {

                        }
                    });
                }
            };
            imageTask.run();
        }
        return convertView;
    }

    private static class ViewHolder {
        ViewSwitcher mSwitcher;
        ImageView mImageNote;
    }
}
