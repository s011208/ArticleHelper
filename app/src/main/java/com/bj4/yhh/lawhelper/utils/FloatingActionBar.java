package com.bj4.yhh.lawhelper.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.bj4.yhh.lawhelper.R;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/29.
 */
public class FloatingActionBar extends RelativeLayout {
    private static final boolean DEBUG = false;
    private static final String TAG = "FloatingActionBar";
    private static final int ANIMATION_DURATION = 200;
    private FloatingActionButton mFloatingActionButton;
    private View mBackgroundView;

    private Context mContext;

    private AnimatorSet mAnimator;
    private int mCurrentBackgroundColor = Color.TRANSPARENT;

    private View mCustomView;
    private int mCustomViewMargin;

    private boolean mIsShow = false;

    public FloatingActionBar(Context context) {
        this(context, null);
    }

    public FloatingActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mBackgroundView = new View(mContext);
        mCustomViewMargin = context.getResources().getDimensionPixelSize(R.dimen.floating_action_bar_custom_view_margin);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (DEBUG) Log.d(TAG, "FloatingActionBar");
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof FloatingActionButton) {
                setFloatingActionButton((FloatingActionButton) child);
                getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        ViewTreeObserver vto = getViewTreeObserver();
                        if (vto.isAlive()) {
                            vto.removeOnPreDrawListener(this);
                        }
                        Rect buttonRect = new Rect();
                        mFloatingActionButton.getGlobalVisibleRect(buttonRect);
                        if (DEBUG)
                            Log.d(TAG, "buttonRect: " + buttonRect + ", buttonRect.width(): " + buttonRect.width());
                        mBackgroundView.setId(Utils.generateViewId());
                        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        rl.setMargins(buttonRect.width() / 2, mFloatingActionButton.getShadowPadding(), buttonRect.width() / 2, mFloatingActionButton.getShadowPadding());
                        addView(mBackgroundView, 0, rl);
                        if (mCustomView != null) {
                            mCustomView.setBackgroundResource(R.drawable.edittext_with_orange_border);
                            RelativeLayout.LayoutParams crl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                            crl.addRule(RelativeLayout.ALIGN_LEFT, mBackgroundView.getId());
                            crl.addRule(RelativeLayout.ALIGN_RIGHT, mBackgroundView.getId());
                            crl.addRule(RelativeLayout.ALIGN_TOP, mBackgroundView.getId());
                            crl.addRule(RelativeLayout.ALIGN_BOTTOM, mBackgroundView.getId());
                            crl.setMargins(mCustomViewMargin, mCustomViewMargin, buttonRect.width() / 2, mCustomViewMargin);
                            addView(mCustomView, crl);
                            mCustomView.setVisibility(View.INVISIBLE);
                            mCustomView.setScaleX(0);
                        }
                        return false;
                    }
                });
            }
        }
    }

    public void setCustomView(View v) {
        if (v == null) return;
        mCustomView = v;
    }

    public void setFloatingActionButton(FloatingActionButton button) {
        if (DEBUG) Log.d(TAG, "setFloatingActionButton");
        mFloatingActionButton = button;
    }

    public FloatingActionButton getFloatingActionButton() {
        return mFloatingActionButton;
    }

    public void showBar() {
        if (mFloatingActionButton == null) {
            return;
        }
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimator = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<Animator>();
        final int fromColor = mCurrentBackgroundColor;
        final int toColor = mFloatingActionButton.getTintColor();
        ValueAnimator bgVa = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        bgVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                Integer value = (Integer) animator.getAnimatedValue();
                mCurrentBackgroundColor = value;
                mBackgroundView.setBackgroundColor(value);
            }

        });
        animators.add(bgVa);
        if (mCustomView != null) {
            ValueAnimator scVa = ValueAnimator.ofFloat(mCustomView.getScaleX(), 1);
            scVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCustomView.setScaleX((Float) animation.getAnimatedValue());
                }
            });
            scVa.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mCustomView.setPivotX(mCustomView.getWidth());
                    mCustomView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animators.add(scVa);
        }
        mAnimator.playTogether(animators);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.start();
        mIsShow = true;
    }

    public void hideBar() {
        if (mFloatingActionButton == null) {
            return;
        }
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimator = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<Animator>();
        final int fromColor = mCurrentBackgroundColor;
        final int toColor = Color.TRANSPARENT;
        ValueAnimator bgVa = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        bgVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                Integer value = (Integer) animator.getAnimatedValue();
                mCurrentBackgroundColor = value;
                mBackgroundView.setBackgroundColor(value);
            }

        });
        animators.add(bgVa);
        if (mCustomView != null) {
            ValueAnimator scVa = ValueAnimator.ofFloat(mCustomView.getScaleX(), 0);
            scVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCustomView.setScaleX((Float) animation.getAnimatedValue());
                }
            });
            scVa.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mCustomView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animators.add(scVa);
        }
        mAnimator.playTogether(animators);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.start();
        mIsShow = false;
    }

    public boolean isShow() {
        return mIsShow;
    }
}
