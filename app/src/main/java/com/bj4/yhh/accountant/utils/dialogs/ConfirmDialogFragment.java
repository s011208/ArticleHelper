package com.bj4.yhh.accountant.utils.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;

/**
 * Created by yenhsunhuang on 15/8/22.
 */
public class ConfirmDialogFragment extends BaseDialogFragment {
    public interface Callback {
        void onConfirm();

        void onCancel();

        String getMessageText();

        String getTitle();
    }

    private TextView mCustomTitle;
    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        }
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    @Override
    public int getTitleTextId() {
        return mCustomTitle.getId();
    }

    @Override
    public View getCustomTitle() {
        mCustomTitle = (TextView) getActivity().getLayoutInflater().inflate(R.layout.base_dialog_fragment_title, null);
        return mCustomTitle;
    }

    @Override
    public View getCustomMessage() {
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mCustomTitle.setText(mCallback.getTitle());
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setMessage(mCallback.getMessageText());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCallback.onConfirm();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCallback.onCancel();
            }
        });
        return builder.create();
    }
}
