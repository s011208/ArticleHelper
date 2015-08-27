package com.bj4.yhh.accountant.fragments.searchActs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.utils.dialogs.BaseDialogFragment;

/**
 * Created by yenhsunhuang on 15/7/31.
 */
public class AddActsFolderDialog extends BaseDialogFragment {

    private EditText mCustomMessage;

    @Override
    public View getCustomMessage() {
        mCustomMessage = (EditText) getActivity().getLayoutInflater().inflate(R.layout.editor_note_edittext, null);
        return mCustomMessage;
    }

    @Override
    public int getTitleTextResources() {
        return R.string.add_acts_folder_title_dialog_title;
    }

    @Override
    public String getTitleText() {
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDialogBuilder().setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String txt = mCustomMessage.getText().toString();
                if (TextUtils.isEmpty(txt) || "".equals(txt)) {
                    // ignore
                } else {
                    Intent intent = getActivity().getIntent();
                    intent.putExtra("title", txt);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                }
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }
}
