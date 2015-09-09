package com.bj4.yhh.lawhelper.activity.image;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.ActContent;
import com.bj4.yhh.lawhelper.act.Note;
import com.bj4.yhh.lawhelper.utils.ImageNoteHelper;
import com.bj4.yhh.lawhelper.utils.SquareGridLayout;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/25.
 */
public class ImagePreviewPagerAdapter extends PagerAdapter {
    public interface Callback {
        void onItemClick(Note note);

        void onItemRemoved();
    }

    private static final boolean DEBUG = false;
    private static final String TAG = "ImagePreviewAdapter";

    private final ImageNoteHelper mImageNoteHelper;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private int mItemPerPage = 4;
    private final Callback mCallback;
    private final Drawable mOnSelectedDrawable;

    private final ArrayList<Note> mData = new ArrayList<Note>();
    private final SparseArray<View> mViewContainer = new SparseArray<View>();

    private View mCurrentView;

    public ImagePreviewPagerAdapter(ActContent content, Context context, Callback cb) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCallback = cb;
        mImageNoteHelper = new ImageNoteHelper(content, context);
        mImageNoteHelper.init();
        mItemPerPage = mContext.getResources().getInteger(R.integer.activity_image_wallpaper_preview_count);
        mOnSelectedDrawable = mContext.getResources().getDrawable(R.drawable.rectangle_main_title_color);
        mData.addAll(mImageNoteHelper.getAllImageNotes());

        if (DEBUG)
            Log.v(TAG, "ImagePreviewPagerAdapter getCount: " + getCount() + ", mData size: " + mData.size());
    }

    public ImageNoteHelper getImageNoteHelper() {
        return mImageNoteHelper;
    }

    public void deletePage(final Note note) {
        for (int i = 0; i < mViewContainer.size(); i++) {
            final int page = mViewContainer.keyAt(i);
            ViewGroup squareLayout = (ViewGroup) mViewContainer.get(page);
            for (int j = 0; j < squareLayout.getChildCount(); j++) {
                Note imageNote = (Note) squareLayout.getChildAt(j).getTag();
                if (imageNote != note) continue;
                mData.remove(note);
                shiftItemsToLeft(i, j);
                notifyDataSetChanged();
                break;
            }
        }
    }

    private void shiftItemsToLeft(final int startPage, final int startIndex) {
        final ArrayList<View> removedViews = new ArrayList<View>();
        final ArrayList<View> translationViews = new ArrayList<View>();
        final ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
        for (int i = 0; i < mViewContainer.size(); i++) {
            final int page = mViewContainer.keyAt(i);
            if (page < startPage) {
                continue;
            } else if (page == startPage) {
                ViewGroup squareLayout = (ViewGroup) mViewContainer.get(page);
                final int squareLayoutChildCount = squareLayout.getChildCount();
                for (int j = startIndex; j < squareLayoutChildCount; j++) {
                    final View imageContainer = squareLayout.getChildAt(j);
                    if (j == startIndex) {
                        // remove after animation
                        removedViews.add(imageContainer);
                    } else {
                        // move to left
                        translationViews.add(squareLayout.getChildAt(j));
                    }
                }
                // add new imageContainer(from next page) into squareLayout
                View newImageContainer = null;
                if (page < mViewContainer.size() - 1 && ((ViewGroup) mViewContainer.get(page + 1)).getChildCount() > 0) {
                    newImageContainer = ((ViewGroup) mViewContainer.get(page + 1)).getChildAt(0);
                    ((ViewGroup) mViewContainer.get(page + 1)).removeView(newImageContainer);
                    squareLayout.addView(newImageContainer);
                    translationViews.add(newImageContainer);
                }
            } else {
                // refresh content directly
                ViewGroup squareLayout = (ViewGroup) mViewContainer.get(page);
                View newImageContainer;
                if (DEBUG) {
                    Log.v(TAG, "page: " + page + ", mViewContainer.size(): " + mViewContainer.size());
                }
                if (page < mViewContainer.size() - 1) {
                    if (DEBUG) {
                        Log.v(TAG, "((ViewGroup) mViewContainer.get(page + 1)).getChildCount(): " + ((ViewGroup) mViewContainer.get(page + 1)).getChildCount());
                    }
                    if (((ViewGroup) mViewContainer.get(page + 1)).getChildCount() > 0) {
                        newImageContainer = ((ViewGroup) mViewContainer.get(page + 1)).getChildAt(0);
                        ((ViewGroup) mViewContainer.get(page + 1)).removeView(newImageContainer);
                        squareLayout.addView(newImageContainer);
                    }
                }
            }
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();
                for (View removedView : removedViews) {
                    removedView.setAlpha(value);
                }
                for (View translationView : translationViews) {
                    translationView.setTranslationX(translationView.getWidth() * (value - 1));
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                for (View removedView : removedViews) {
                    ViewGroup container = (ViewGroup) removedView.getParent();
                    container.removeView(removedView);
                    Note.delete(mContext, (Note) removedView.getTag());
                }
                for (View translationView : translationViews) {
                    translationView.setTranslationX(0);
                }
                if (mCallback != null) {
                    mCallback.onItemRemoved();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(250);
        animator.start();
    }

    private View inflatePage(final int pagePosition) {
        final SquareGridLayout container = (SquareGridLayout) mInflater.inflate(R.layout.image_preview_grid_layout, null);
        container.setColumnCount(mItemPerPage);
        if (DEBUG) {
            Log.d(TAG, "pagePosition: " + pagePosition + ", mItemPerPage: " + mItemPerPage
                    + ",  mData.size(): " + mData.size());
        }
        for (int i = pagePosition * mItemPerPage; i < mItemPerPage * (pagePosition + 1) && i < mData.size(); i++) {
            final Note note = mData.get(i);
            final FrameLayout imageContainer = (FrameLayout) mInflater.inflate(R.layout.image_preview_grid, null);
            imageContainer.setTag(note);
            final ImageView image = (ImageView) imageContainer.findViewById(R.id.cell_image);
            image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver vto = image.getViewTreeObserver();
                    if (vto.isAlive()) {
                        vto.removeOnGlobalLayoutListener(this);
                    }
                    mImageNoteHelper.setImageSize(image.getWidth(), image.getHeight());
                    mImageNoteHelper.getImageLoader().displayImage(note.mNoteContent, image, mImageNoteHelper.getDisplayImageOptions());
                }
            });
            imageContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCurrentView == v) {
                        return;
                    }
                    mCurrentView = v;
                    if (mCallback != null) {
                        mCallback.onItemClick(note);
                    }
                }
            });
            container.addView(imageContainer);
        }
        return container;
    }

    public Note setFocusDrawable(String noteUri) {
        Note rtn = null;
        for (int i = 0; i < mViewContainer.size(); i++) {
            final int key = mViewContainer.keyAt(i);
            ViewGroup squareLayout = (ViewGroup) mViewContainer.get(key);
            if (DEBUG) Log.w(TAG, "noteUri: " + noteUri);
            for (int j = 0; j < squareLayout.getChildCount(); j++) {
                FrameLayout imageContainer = (FrameLayout) squareLayout.getChildAt(j);
                Note imageTag = (Note) imageContainer.getTag();
                if (DEBUG) Log.d(TAG, "imageTag: " + System.identityHashCode(imageTag));
                if (imageTag.mNoteContent.equals(noteUri)) {
                    imageContainer.setForeground(mOnSelectedDrawable);
                    rtn = imageTag;
                } else {
                    imageContainer.setForeground(null);
                }
            }
        }
        return rtn;
    }

    public void setFocusDrawable(Note note) {
        for (int i = 0; i < mViewContainer.size(); i++) {
            final int key = mViewContainer.keyAt(i);
            ViewGroup squareLayout = (ViewGroup) mViewContainer.get(key);
            if (DEBUG) Log.w(TAG, "note: " + System.identityHashCode(note));
            for (int j = 0; j < squareLayout.getChildCount(); j++) {
                FrameLayout imageContainer = (FrameLayout) squareLayout.getChildAt(j);
                Note imageTag = (Note) imageContainer.getTag();
                if (DEBUG) Log.d(TAG, "imageTag: " + System.identityHashCode(imageTag));
                if (imageTag == note) {
                    imageContainer.setForeground(mOnSelectedDrawable);
                } else {
                    imageContainer.setForeground(null);
                }
            }
        }
    }

    public void clearAllFocusDrawable() {
        for (int i = 0; i < mViewContainer.size(); i++) {
            final int key = mViewContainer.keyAt(i);
            ViewGroup squareLayout = (ViewGroup) mViewContainer.get(key);
            for (int j = 0; j < squareLayout.getChildCount(); j++) {
                FrameLayout imageContainer = (FrameLayout) squareLayout.getChildAt(j);
                imageContainer.setForeground(null);
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewContainer.get(position));
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mViewContainer.get(position);
        if (view == null) {
            view = inflatePage(position);
            mViewContainer.put(position, view);
        }
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return (int) Math.ceil(mData.size() / (float) mItemPerPage);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
}
