package com.bj4.yhh.lawhelper.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

/**
 * Created by yenhsunhuang on 15/8/12.
 */
public class BlurBuilder {
    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 7.5f;

    private BlurBuilder() {
    }

    public static Bitmap blur(View v) {
        Bitmap viewCache = getScreenshot(v);
        Bitmap rtn = blur(v.getContext(), viewCache);
        viewCache.recycle();
        return rtn;
    }

    public static Bitmap blur(Context context, Drawable d) {
        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        final int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        return blur(context, d, screenWidth, screenHeight);
    }

    public static Bitmap blur(Context context, Drawable d, int desiredWidth, int desiredHeight) {
        Bitmap image = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(image);
        d.setBounds(0, 0, image.getWidth(), image.getHeight());
        d.draw(canvas);
        canvas.setBitmap(null);
        Bitmap rtn = blur(context, image);
        image.recycle();
        return rtn;
    }

    @TargetApi(17)
    public static Bitmap blur(Context ctx, Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(ctx);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    private static Bitmap getScreenshot(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }
}