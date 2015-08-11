package com.bj4.yhh.accountant.fragments.cards;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.bj4.yhh.accountant.R;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/12.
 */
public class CardListView extends ListView {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SWIPE_GESTURE = false;
    private static final boolean DEBUG_TOUCH_EVENT = false;
    private static final String TAG = "CardListView";

    public interface Callback {
        public void onFling(View onFlingView);

        public void onFinishAllAnimator();
    }

    private Callback mCallback;

    private final float mSwipeThreshold;
    private final float mScrollThreshold;
    private float mTouchDownX, mTouchDownY;
    private float mFinalX = 0;
    private boolean mHasOnFling = false;

    private View mTouchedView;

    private static final int TOUCH_STATE_NONE = 0;
    private static final int TOUCH_STATE_SCROLL_X = 1;
    private static final int TOUCH_STATE_SCROLL_Y = 2;
    private int mTouchState = TOUCH_STATE_NONE;

    private static final int VELOCITY_X_TRRESHOLD = 1250;
    private static final int FLING_TO_DELETE_DURATION = 200;

    private final GestureDetector mGestureDetector;
    private final GestureDetector.SimpleOnGestureListener mSwipeGestureListener = new SwipeGestureListener();

    private boolean mIsInAnimation = false;
    private ValueAnimator mFlingToDeleteAnimator;

    private AnimatorSet mFillUpAnimatorSet;
    private final ArrayList<Animator> mFillUpAnimatorList = new ArrayList<Animator>();

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mFinalX -= distanceX;
            if (DEBUG && DEBUG_SWIPE_GESTURE)
                Log.d(TAG, "onScroll, distanceX: " + distanceX + ", mFinalX: " + mFinalX);
            calculateTranlationXValue();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) <= VELOCITY_X_TRRESHOLD) {
                return false;
            }
            mHasOnFling = true;
            flingToDelete();
            if (DEBUG && DEBUG_SWIPE_GESTURE) {
                Log.v(TAG, "onFling");
            }
            return true;
        }
    }

    private void calculateTranlationXValue() {
        if (mTouchedView == null) {
            return;
        }
        mTouchedView.setTranslationX(mFinalX);
    }

    private void flingToDelete() {
        if (mTouchedView == null) {
            return;
        }
        if (mFlingToDeleteAnimator != null) {
            mFlingToDeleteAnimator.cancel();
        }
        mIsInAnimation = true;
        final boolean flingToLeft = mTouchedView.getTranslationX() <= 0;
        final int finalX = flingToLeft ? -getWidth() : getWidth();
        mFlingToDeleteAnimator = ValueAnimator.ofFloat(mTouchedView.getTranslationX(), finalX);
        mFlingToDeleteAnimator.setDuration(FLING_TO_DELETE_DURATION);
        mFlingToDeleteAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTouchedView.setTranslationX((Float) animation.getAnimatedValue());
            }
        });
        mFlingToDeleteAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mCallback != null) {
                    mCallback.onFling(mTouchedView);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFillUpAnimatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mFlingToDeleteAnimator.start();
    }

    public CardListView(Context context) {
        this(context, null);
    }

    public CardListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSwipeThreshold = context.getResources().getDimension(R.dimen.main_card_fragment_card_container_swipe_threshold);
        mScrollThreshold = context.getResources().getDimension(R.dimen.main_card_fragment_card_container_scroll_threshold);
        mGestureDetector = new GestureDetector(context, mSwipeGestureListener);
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    private void checkScrollThreshold() {
        if (Math.abs(mFinalX) >= mScrollThreshold) {
            flingToDelete();
        } else {
            resetViewPosition();
        }
    }

    private void resetViewPosition() {
        if (mTouchedView == null)
            return;
        if (mFlingToDeleteAnimator != null) {
            mFlingToDeleteAnimator.cancel();
        }
        final int finalX = 0;
        mFlingToDeleteAnimator = ValueAnimator.ofFloat(mTouchedView.getTranslationX(), finalX);
        mFlingToDeleteAnimator.setDuration(FLING_TO_DELETE_DURATION);
        mFlingToDeleteAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTouchedView.setTranslationX((Float) animation.getAnimatedValue());
            }
        });
        mFlingToDeleteAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (DEBUG && DEBUG_TOUCH_EVENT) {
            Log.v(TAG, "onTouchEvent, action: " + action + ", mTouchState: " + mTouchState);
        }
        if (mIsInAnimation) {
            if (DEBUG && DEBUG_TOUCH_EVENT) {
                Log.v(TAG, "mIsInAnimation: " + mIsInAnimation);
            }
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                super.onInterceptTouchEvent(ev);
                mTouchDownX = ev.getX();
                mTouchDownY = ev.getY();
                mGestureDetector.onTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_UP:
                try {
                    if (mTouchState == TOUCH_STATE_SCROLL_X) {
                        mGestureDetector.onTouchEvent(ev);
                        if (DEBUG && DEBUG_TOUCH_EVENT) {
                            Log.v(TAG, "mHasOnFling: " + mHasOnFling);
                        }
                        if (!mHasOnFling) {
                            checkScrollThreshold();
                        }
                        return true;
                    }
                } finally {
                    // reset variables no matter what
                    mTouchDownX = -1;
                    mTouchDownY = -1;
                    mFinalX = 0;
                    mTouchState = TOUCH_STATE_NONE;
                    mHasOnFling = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchState == TOUCH_STATE_SCROLL_Y) {
                    return super.onTouchEvent(ev);
                }
                isHandleTouchEvent(ev);
                if (mTouchState == TOUCH_STATE_SCROLL_X) {
                    mGestureDetector.onTouchEvent(ev);
                    return true;
                }
        }
        return super.onTouchEvent(ev);
    }

    private void isHandleTouchEvent(MotionEvent ev) {
        if (mTouchState == TOUCH_STATE_SCROLL_X) {
            return;
        }
        final float deltaY = Math.abs(mTouchDownY - ev.getY());
        final float deltaX = Math.abs(mTouchDownX - ev.getX());
        if (deltaY >= mSwipeThreshold) {
            mTouchState = TOUCH_STATE_SCROLL_Y;
        }
        if (deltaX >= mSwipeThreshold) {
            mTouchState = TOUCH_STATE_SCROLL_X;
            mTouchedView = getViewByPosition(ev);
        }
    }

    private View getViewByPosition(MotionEvent downEvent) {
        mFillUpAnimatorList.clear();
        mFillUpAnimatorSet = new AnimatorSet();
        View rtn = null;
        final int positionY = (int) downEvent.getRawY();
        int previousYPosition = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            Rect visibleRect = new Rect();
            v.getGlobalVisibleRect(visibleRect);
            if (visibleRect.top <= positionY && visibleRect.bottom >= positionY) {
                rtn = v;
            } else if (rtn != null && visibleRect.height() > 0) {
                addFillUpAnimator(v, previousYPosition - visibleRect.top);
            }
            previousYPosition = visibleRect.top;
        }
        mFillUpAnimatorSet.setStartDelay(100);
        mFillUpAnimatorSet.playTogether(mFillUpAnimatorList);
        mFillUpAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCallback != null) {
                    mCallback.onFinishAllAnimator();
                }
                mTouchedView = null;
                mIsInAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return rtn;
    }

    @Override
    public int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    private void addFillUpAnimator(final View view, float translationY) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, translationY);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationY((Float) animation.getAnimatedValue());
            }
        });
        animator.setDuration(FLING_TO_DELETE_DURATION);
        animator.setStartDelay(mFillUpAnimatorList.size() * 50);
        mFillUpAnimatorList.add(animator);
    }
}
