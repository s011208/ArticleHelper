package com.bj4.yhh.accountant.utils.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;

/**
 * Created by yenhsunhuang on 15/7/30.
 */
public abstract class BaseDialogFragment extends DialogFragment {

    private View mTitle, mMessage;

    public abstract int getTitleTextId();

    public abstract View getCustomTitle();

    public abstract View getCustomMessage();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initComponents();
    }

    public void setCustomTitle(View title) {
        if (title == null) return;
        mTitle = title;
        mTitle.setBackgroundColor(getActivity().getResources().getColor(R.color.main_title_color));
        if (getTitleTextId() > 0) {
            View titleText = mTitle.findViewById(getTitleTextId());
            if (titleText != null && titleText instanceof TextView) {
                ((TextView) titleText).setTextColor(getActivity().getResources().getColor(R.color.white));
            }
        }
    }

    public void setCustomMessage(View msg) {
        if (msg == null) return;
        mMessage = msg;
    }

    private void initComponents() {
        onInitComponents();
        setCustomTitle(getCustomTitle());
        setCustomMessage(getCustomMessage());
    }

    public void onInitComponents() {
    }

    public AlertDialog.Builder getDialogBuilder() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        if (mTitle != null) {
            dialogBuilder.setCustomTitle(mTitle);
        }
        if (mMessage != null) {
            dialogBuilder.setView(mMessage);
        }
        return dialogBuilder;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return getDialogBuilder().setTitle("test").setMessage("test").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create();
    }

    @Override
    public void onResume() {
        super.onResume();
        setButtonsBackground(DialogInterface.BUTTON_POSITIVE);
        setButtonsBackground(DialogInterface.BUTTON_NEGATIVE);
        setButtonsBackground(DialogInterface.BUTTON_NEUTRAL);
    }

    private void setButtonsBackground(int buttonId) {
        if (getDialog() instanceof AlertDialog == false) {
            return;
        }
        Button button = ((AlertDialog) getDialog()).getButton(buttonId);
        if (button != null) {
            button.setBackgroundResource(R.drawable.on_click_background);
        }
    }
}
