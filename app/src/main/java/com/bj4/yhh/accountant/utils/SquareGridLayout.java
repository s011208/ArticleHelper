package com.bj4.yhh.accountant.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yenhsunhuang on 15/7/25.
 */
public class SquareGridLayout extends ViewGroup {
    private static final String TAG = "SquareGridLayout";
    private static final boolean DEBUG = false;

    private int mScreenWidth;
    private int mColumnCount = 5;
    private int mChildSize;

    public SquareGridLayout(Context context) {
        this(context, null);
    }

    public SquareGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
    }

    public void setColumnCount(int columnCount) {
        if (DEBUG) {
            Log.v(TAG, "setColumn, columnCount: " + columnCount);
        }
        mColumnCount = columnCount;
        super.requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        if (DEBUG)
            Log.d(TAG, "onLayout, child count: " + count + ", l: " + l + ", t: " + t + ", r: " + r + ", b: " + b);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(mChildSize * i, t, mChildSize * (i + 1), b);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (DEBUG)
            Log.d(TAG, "onMeasure, getMeasuredWidth: " + getMeasuredWidth() + ", getMeasuredHeight: " + getMeasuredHeight()
                    + ", getWidth(): " + getWidth() + ", getHeight(): " + getHeight()
                    + ", parentWidth: " + parentWidth + ", parentHeight: " + parentHeight);
        final int viewWidth = Math.min(mScreenWidth, parentWidth);
        mChildSize = viewWidth / mColumnCount;
        setMeasuredDimension(viewWidth, mChildSize);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int wSpec = MeasureSpec.makeMeasureSpec(
                    mChildSize, MeasureSpec.EXACTLY);
            int hSpec = MeasureSpec.makeMeasureSpec(
                    mChildSize, MeasureSpec.EXACTLY);
            child.measure(wSpec, hSpec);

        }
    }
}
