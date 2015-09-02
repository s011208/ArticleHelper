package com.bj4.yhh.accountant.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by yen-hsun_huang on 2015/8/11.
 */
public class ScaledImageView extends View implements View.OnTouchListener {
    private static final String TAG = "ScaledImageView";
    private static final boolean DEBUG = true;

    private Bitmap mThumbnail;

    private final GestureDetector mGestureDetector;
    private OnClickListener mOnClickListener;
    // for scale
    private final Matrix mDrawMatrix = new Matrix();
    private float mLastFocusX;
    private float mLastFocusY;
    private final ScaleGestureDetector mScaleGestureDetector;
    // for move
    private float mLastMoveX, mLastMoveY;

    private boolean mIsScaling = false;

    public ScaledImageView(Context context) {
        this(context, null);
    }

    public ScaledImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaledImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
        mGestureDetector = new GestureDetector(context, new SingleTapConfirm());
    }

    private class SingleTapConfirm implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }
    }

    public void setThumbnail(Bitmap b) {
        mDrawMatrix.reset();
        mThumbnail = b;
        final Runnable invalidateTask = new Runnable() {
            @Override
            public void run() {
                float scale = Math.max(getWidth() / (float) mThumbnail.getWidth(), getHeight() / (float) mThumbnail.getHeight());
                mDrawMatrix.postScale(scale, scale);
                invalidate();
            }
        };
        if (getWidth() != 0) {
            invalidateTask.run();
        } else {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final ViewTreeObserver vto = getViewTreeObserver();
                    if (vto.isAlive()) {
                        vto.removeOnGlobalLayoutListener(this);
                    }
                    invalidateTask.run();
                }
            });
        }
    }

    public Bitmap getCropImage(Rect cropRect) {
        if (mThumbnail == null) return null;
        final Bitmap scaledBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(scaledBitmap);
        canvas.drawBitmap(mThumbnail, mDrawMatrix, null);
        canvas.setBitmap(null);
        final Bitmap bitmap = Bitmap.createBitmap(scaledBitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
        scaledBitmap.recycle();
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mThumbnail == null) return;
        canvas.drawBitmap(mThumbnail, mDrawMatrix, null);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
            mIsScaling = false;
        }
        if (motionEvent.getPointerCount() == 1 && !mIsScaling) {
            final boolean isSingleTapUp = mGestureDetector.onTouchEvent(motionEvent);
            if (DEBUG)
                Log.d(TAG, "isSingleTapUp: " + isSingleTapUp + ", (mOnClickListener != null): " + (mOnClickListener != null));
            if (isSingleTapUp) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(this);
                }
                return true;
            }
            moveImage(motionEvent);
            return true;
        } else {
            mIsScaling = true;
            return mScaleGestureDetector.onTouchEvent(motionEvent);
        }
    }

    private void moveImage(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = mLastMoveX - motionEvent.getX();
                float deltaY = mLastMoveY - motionEvent.getY();
                mDrawMatrix.postTranslate(-deltaX, -deltaY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        mLastMoveX = motionEvent.getX();
        mLastMoveY = motionEvent.getY();
    }

    private class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Matrix transformationMatrix = new Matrix();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            transformationMatrix.postTranslate(-focusX, -focusY);
            transformationMatrix.postScale(detector.getScaleFactor(), detector.getScaleFactor());
            float focusShiftX = focusX - mLastFocusX;
            float focusShiftY = focusY - mLastFocusY;
            transformationMatrix.postTranslate(focusX + focusShiftX, focusY + focusShiftY);
            mDrawMatrix.postConcat(transformationMatrix);
            mLastFocusX = focusX;
            mLastFocusY = focusY;
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mLastFocusX = detector.getFocusX();
            mLastFocusY = detector.getFocusY();
            return mThumbnail != null;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }

}