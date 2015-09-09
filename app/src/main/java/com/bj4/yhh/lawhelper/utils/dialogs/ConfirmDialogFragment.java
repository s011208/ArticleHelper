package com.bj4.yhh.lawhelper.utils.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

/**
 * Created by yenhsunhuang on 15/8/22.
 */
public class ConfirmDialogFragment extends BaseDialogFragment {
    public static final int REQUEST_CONFIRM = 0;

    public static final String ARGUS_TITLE = "title";

    public static final String ARGUS_MESSAGE = "message";

    public interface Callback {
        void onConfirm();

        void onCancel();
    }

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        }
    }


    @Override
    public View getCustomMessage() {
        return null;
    }

    @Override
    public int getTitleTextResources() {
        return 0;
    }

    @Override
    public String getTitleText() {
        return getArguments().getString(ARGUS_TITLE, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setMessage(getArguments().getString(ARGUS_MESSAGE, null));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCallback != null) {
                    mCallback.onConfirm();
                } else {
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCallback != null) {
                    mCallback.onCancel();
                } else {
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                }
            }
        });
        return builder.create();
    }
}
