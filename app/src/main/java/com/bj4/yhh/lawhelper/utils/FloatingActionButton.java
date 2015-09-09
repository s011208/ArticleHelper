package com.bj4.yhh.lawhelper.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.bj4.yhh.lawhelper.R;

/**
 * Created by yenhsunhuang on 15/7/28.
 */
public class FloatingActionButton extends View {
    private Drawable mDrawable;
    private int mTintColor = Color.WHITE;
    private int mPressTintColor = Color.GRAY;
    private Context mContext;

    private int mDrawablePadding;
    private int mShadowPadding;

    private StateListDrawable mStateListDrawable;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mDrawablePadding = context.getResources().getDimensionPixelSize(R.dimen.floating_action_button_drawable_padding);
        mShadowPadding = context.getResources().getDimensionPixelSize(R.dimen.floating_action_button_shadow_layer_size);
        mStateListDrawable = new StateListDrawable();
    }

    public FloatingActionButton setIconDrawable(Drawable drawable) {
        mDrawable = drawable;
        return this;
    }

    public FloatingActionButton setTinitColor(int color) {
        mTintColor = color;
        return this;
    }

    public int getTintColor() {
        return mTintColor;
    }

    public int getPressTintColor() {
        return mPressTintColor;
    }

    public FloatingActionButton setPressTintColor(int color) {
        mPressTintColor = color;
        return this;
    }

    private static Drawable buildCircleWithShadow(int color, int width, int height, int shadowPadding, int drawablePadding, Context context, Drawable icon, boolean withShadow) {
        final int size = Math.min(width, height) - shadowPadding;
        Paint paint = new Paint();
        if (withShadow) {
            paint.setShadowLayer(shadowPadding, 0, 0, Color.BLACK);
        }
        paint.setAntiAlias(true);
        paint.setColor(color);
        Bitmap background = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        canvas.drawCircle(size / 2, size / 2, (size - shadowPadding * 2) / 2, paint);
        if (icon != null) {
            icon.setBounds(drawablePadding, drawablePadding, size - drawablePadding, size - drawablePadding);
            icon.draw(canvas);
        }
        canvas.setBitmap(null);
        return new BitmapDrawable(context.getResources(), background);
    }

    public int getShadowPadding() {
        return mShadowPadding;
    }


    public void build() {
        final Runnable buildTask = new Runnable() {
            @Override
            public void run() {
                Drawable tintColor = buildCircleWithShadow(mTintColor, getWidth(), getHeight(), mShadowPadding, mDrawablePadding, mContext, mDrawable, true);
                Drawable pressTintColor = buildCircleWithShadow(mPressTintColor, getWidth(), getHeight(), mShadowPadding, mDrawablePadding, mContext, mDrawable, false);
                mStateListDrawable.addState(new int[]{android.R.attr.state_pressed},
                        pressTintColor);
                mStateListDrawable.addState(new int[]{android.R.attr.state_focused},
                        pressTintColor);
                mStateListDrawable.addState(new int[]{},
                        tintColor);
                setBackground(mStateListDrawable);
            }
        };
        if (getWidth() == 0 || getHeight() == 0) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver vto = getViewTreeObserver();
                    if (vto.isAlive()) {
                        vto.removeOnGlobalLayoutListener(this);
                    }
                    buildTask.run();
                }
            });
        } else {
            buildTask.run();
        }
    }
}
