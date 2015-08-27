package com.bj4.yhh.accountant.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bj4.yhh.accountant.R;

/**
 * Created by Yen-Hsun_Huang on 2015/8/27.
 */
public class BaseToast {
    public static void showToast(Context context, String text) {
        showToast(context, text, 0);
    }

    public static void showToast(Context context, String text, int drawableResource) {
        Toast toast = new Toast(context);
        final LinearLayout root = (LinearLayout) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.base_toast, null);
        final TextView toastText = (TextView) root.findViewById(R.id.toast_text);
        toastText.setCompoundDrawablePadding(context.getResources().getDimensionPixelSize(R.dimen.base_toast_drawable_padding));
        if (drawableResource > 0) {
            toastText.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(drawableResource), null, null, null);
        }
        toastText.setText(text);
        toast.setView(root);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, context.getResources().getDimensionPixelSize(R.dimen.base_toast_y_offset));
        toast.show();
    }
}
