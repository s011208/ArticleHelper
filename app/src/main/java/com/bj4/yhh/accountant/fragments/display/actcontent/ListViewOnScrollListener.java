package com.bj4.yhh.accountant.fragments.display.actcontent;

import android.view.View;
import android.widget.AbsListView;

/**
 * Created by yenhsunhuang on 15/7/20.
 */
public class ListViewOnScrollListener implements AbsListView.OnScrollListener {
    private ScrollDistanceListener mScrollDistanceListener;

    private boolean mListScrollStarted;
    private int mFirstVisibleItem;
    private int mFirstVisibleHeight;
    private int mFirstVisibleTop, mFirstVisibleBottom;
    private int mScrollDistance;

    public ListViewOnScrollListener(ScrollDistanceListener listener) {
        setScrollDistanceListener(listener);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (view.getCount() == 0) return;
        switch (scrollState) {
            case SCROLL_STATE_IDLE: {
                mListScrollStarted = false;
                break;
            }
            case SCROLL_STATE_TOUCH_SCROLL: {
                final View firstChild = view.getChildAt(0);
                mFirstVisibleItem = view.getFirstVisiblePosition();
                mFirstVisibleTop = firstChild.getTop();
                mFirstVisibleBottom = firstChild.getBottom();
                mFirstVisibleHeight = firstChild.getHeight();
                mListScrollStarted = true;
                break;
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount == 0 || !mListScrollStarted) return;
        final View firstChild = view.getChildAt(0);
        final int firstVisibleTop = firstChild.getTop(), firstVisibleBottom = firstChild.getBottom();
        final int firstVisibleHeight = firstChild.getHeight();
        final int delta;
        if (firstVisibleItem > mFirstVisibleItem) {
            mFirstVisibleTop += mFirstVisibleHeight;
            delta = firstVisibleTop - mFirstVisibleTop;
        } else if (firstVisibleItem < mFirstVisibleItem) {
            mFirstVisibleBottom -= mFirstVisibleHeight;
            delta = firstVisibleBottom - mFirstVisibleBottom;
        } else {
            delta = firstVisibleBottom - mFirstVisibleBottom;
        }
        mScrollDistance += delta;
        if (mScrollDistanceListener != null) {
            mScrollDistanceListener.onScrollDistanceChanged(delta, mScrollDistance);
        }
        mFirstVisibleTop = firstVisibleTop;
        mFirstVisibleBottom = firstVisibleBottom;
        mFirstVisibleHeight = firstVisibleHeight;
        mFirstVisibleItem = firstVisibleItem;
    }

    public void setScrollDistanceListener(ScrollDistanceListener listener) {
        mScrollDistanceListener = listener;
    }

    public static interface ScrollDistanceListener {
        void onScrollDistanceChanged(int delta, int scrollDistance);
    }
}
