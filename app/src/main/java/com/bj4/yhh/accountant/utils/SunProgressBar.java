package com.bj4.yhh.accountant.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.bj4.yhh.accountant.R;

/**
 * Created by yenhsunhuang on 15/8/9.
 */
public class SunProgressBar extends View {
    private static final String TAG = "SunProgressBar";
    private static final boolean DEBUG = true;
    private static final int PROGRESS_ITEM_COUNT = 10;
    private static final int PROGRESS_ANIMATOR_DURATION = 1000;
    private int mStartIndex = 0;

    private static final int TOTAL_UNIT = 10;
    private static final int ITEM_UNIT = 3;
    private static final int CENTER_UNIT = 2;
    private final ValueAnimator mProgressAnimator = ValueAnimator.ofFloat(0, PROGRESS_ITEM_COUNT);
    private final Paint mPaint = new Paint();
    private final Drawable[] mProgressColor;
    private int mRectItemHeight;
    private int mRectItemWidth;
    private int mCenterOffset;

    public SunProgressBar(Context context) {
        this(context, null);
    }

    public SunProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SunProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources res = context.getResources();
        mPaint.setColor(Color.argb(0xff, 0xff, 0x99, 0x00));
        mProgressColor = new Drawable[]{res.getDrawable(R.drawable.progress_color_0), res.getDrawable(R.drawable.progress_color_1),
                res.getDrawable(R.drawable.progress_color_2), res.getDrawable(R.drawable.progress_color_3),
                res.getDrawable(R.drawable.progress_color_4), res.getDrawable(R.drawable.progress_color_5),
                res.getDrawable(R.drawable.progress_color_6), res.getDrawable(R.drawable.progress_color_7),
                res.getDrawable(R.drawable.progress_color_8), res.getDrawable(R.drawable.progress_color_9)};
        mProgressAnimator.setDuration(PROGRESS_ANIMATOR_DURATION);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mStartIndex = (int) Math.ceil((Float) animation.getAnimatedValue());
                invalidate();
            }
        });
        mProgressAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mProgressAnimator.setRepeatMode(ValueAnimator.RESTART);
        mProgressAnimator.setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        final int availableSize = Math.min(parentWidth, parentHeight)
                - Math.max(getPaddingLeft() + getPaddingRight(), getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(availableSize, availableSize);
        final int unit = availableSize / TOTAL_UNIT;
        mRectItemWidth = unit * ITEM_UNIT;
        mRectItemHeight = unit;
        mCenterOffset = unit * CENTER_UNIT;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mProgressAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mProgressAnimator.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0)
            return;
        final int centerX = canvas.getWidth() / 2;
        final int centerY = canvas.getHeight() / 2;
        canvas.drawCircle(centerX, centerY, mCenterOffset / 2, mPaint);
        canvas.save();
        for (int i = 0; i < PROGRESS_ITEM_COUNT; i++) {
            final int drawableIndex = (i + mStartIndex) % PROGRESS_ITEM_COUNT;
            mProgressColor[drawableIndex].setBounds(centerX + mCenterOffset, centerY - mRectItemHeight / 2, centerX + mRectItemWidth + mCenterOffset, centerY - mRectItemHeight / 2 + mRectItemHeight);
            mProgressColor[drawableIndex].draw(canvas);
            canvas.rotate(-360 / PROGRESS_ITEM_COUNT, centerX, centerY);
        }
        canvas.restore();
    }


}
