package com.bj4.yhh.accountant.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Created by yenhsunhuang on 15/7/20.
 */
public class TranslationHeaderLayout extends FrameLayout {
    private static final boolean DEBUG = false;
    private static final String TAG = "TranslationHeaderLayout";

    public interface Callback {
        void onScroll(int deltaY);

        void onScrollDone();
    }

    private View mHeader;
    private View mFooter;
    private int mHeaderHeight;
    private int mLastTouchY;

    private Callback mCallback;

    public TranslationHeaderLayout(Context context) {
        this(context, null);
    }

    public TranslationHeaderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TranslationHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                final ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.removeOnPreDrawListener(this);
                }
                initPosition();
                return false;
            }
        });
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    private void initPosition() {
        if (mHeader == null || mFooter == null) {
            return;
        }
        mHeaderHeight = mHeader.getHeight();
        mFooter.setTranslationY(mHeaderHeight);
    }

    public void setHeader(View header) {
        mHeader = header;
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(header, fl);
    }

    public void setFooter(View footer) {
        mFooter = footer;
        addView(footer);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        handleTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private boolean handleTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                final int deltaY = (int) (mLastTouchY - ev.getY());
                final int scrollY = getScrollY();
                if (DEBUG) {
                    Log.d(TAG, "deltaY: " + deltaY + ", scrollY: " + scrollY);
                }
                if (deltaY > 0) {
                    if (scrollY + deltaY >= mHeaderHeight) {
                        scrollTo(0, mHeaderHeight);
                    } else {
                        scrollBy(0, deltaY);
                    }
                } else {
                    if (scrollY + deltaY <= 0) {
                        scrollTo(0, 0);
                    } else {
                        scrollBy(0, deltaY);
                    }
                }
                if (DEBUG)
                    Log.d(TAG, "header alpha, getScrollY(): " + getScrollY() + ", mHeaderHeight: " + mHeaderHeight);
                mHeader.setAlpha(1 - (getScrollY() / (float) mHeaderHeight));
                if (mCallback != null) {
                    mCallback.onScroll(deltaY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCallback != null) {
                    mCallback.onScrollDone();
                }
                break;
        }
        mLastTouchY = (int) ev.getY();
        return true;
    }
}
