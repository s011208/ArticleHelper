package com.bj4.yhh.accountant.utils.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;

/**
 * Created by yenhsunhuang on 15/7/30.
 */
public abstract class BaseDialogFragment extends DialogFragment {

    private View mTitle, mMessage;
    private TextView mTitleText;

    public abstract View getCustomMessage();

    public abstract int getTitleTextResources();

    public abstract String getTitleText();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initComponents();
    }

    private void setCustomTitle() {
        mTitle = getActivity().getLayoutInflater().inflate(R.layout.base_dialog_fragment_title, null);
        mTitle.setBackgroundColor(getActivity().getResources().getColor(R.color.main_title_color));
        mTitleText = (TextView) mTitle.findViewById(R.id.base_fragment_title);
        if (mTitleText != null) {
            mTitleText.setTextColor(getActivity().getResources().getColor(R.color.white));
            if (getTitleTextResources() > 0) {
                mTitleText.setText(getTitleTextResources());
            } else if (getTitleText() != null) {
                mTitleText.setText(getTitleText());
            }
        }
    }

    private void setCustomMessage(View msg) {
        if (msg == null) return;
        mMessage = msg;
    }

    private void initComponents() {
        onInitComponents();
        setCustomMessage(getCustomMessage());
    }

    public void onInitComponents() {
    }

    public AlertDialog.Builder getDialogBuilder() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        setCustomTitle();
        dialogBuilder.setCustomTitle(mTitle);
        if (mMessage != null) {
            LinearLayout msgRoot = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.base_dialog_fragment_message, null);
            LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            final int margin = getActivity().getResources().getDimensionPixelSize(R.dimen.base_dialog_fragment_message_left_margin);
            ll.setMargins(margin, 0, margin, 0);
            msgRoot.addView(mMessage, ll);
            dialogBuilder.setView(msgRoot);
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
        setMessagePadding();
    }

    private void setMessagePadding() {
        if (mMessage != null) return;
        final TextView messageView = (TextView) getDialog().findViewById(android.R.id.message);
        if (messageView == null) return;
        FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams) messageView.getLayoutParams();
        final int margin = getActivity().getResources().getDimensionPixelSize(R.dimen.base_dialog_fragment_message_left_margin);
        fl.setMargins(margin, 0, margin, 0);
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
