package com.bj4.yhh.accountant.fragments.display.actcontent.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.utils.dialogs.BaseDialogFragment;

/**
 * Created by yenhsunhuang on 15/7/25.
 */
public class EditorNoteDialogFragment extends BaseDialogFragment {
    public static final String TAG = "EditorNoteDialogFragment";

    private TextView mCustomTitle;

    private EditText mCustomMessage;

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (EditorNoteDialogFragment.Callback) activity;
    }

    @Override
    public int getTitleTextId() {
        return mCustomTitle.getId();
    }

    @Override
    public View getCustomTitle() {
        mCustomTitle = (TextView) getActivity().getLayoutInflater().inflate(R.layout.base_dialog_fragment_title, null);
        mCustomTitle.setText(R.string.activity_act_editor_edit_note_dialog_title);
        return mCustomTitle;
    }

    @Override
    public View getCustomMessage() {
        mCustomMessage = (EditText) getActivity().getLayoutInflater().inflate(R.layout.editor_note_edittext, null);
        return mCustomMessage;
    }

    public interface Callback {
        void onPositiveClick(String newNote);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String txt = getArguments().getString("txt");
        if (TextUtils.isEmpty(txt) || "".equals(txt)) {
            mCustomMessage.setHint(R.string.activity_act_editor_note_default_text);
        } else {
            mCustomMessage.setText(txt);
        }
        AlertDialog.Builder builder = getDialogBuilder().setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCallback != null) {
                    String txt = mCustomMessage.getText().toString();
                    if (TextUtils.isEmpty(txt) || "".equals(txt)) {
                        // ignore
                    } else {
                        mCallback.onPositiveClick(txt);
                    }
                }
            }
        });
        return builder.create();
    }
}
