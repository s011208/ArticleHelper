package com.bj4.yhh.accountant.fragments.testmode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.utils.dialogs.BaseDialogFragment;

/**
 * Created by yenhsunhuang on 15/8/26.
 */
public class TestModeTypeDialog extends BaseDialogFragment {
    private TextView mCustomTitle;

    @Override
    public int getTitleTextId() {
        return mCustomTitle.getId();
    }

    @Override
    public View getCustomTitle() {
        mCustomTitle = (TextView) getActivity().getLayoutInflater().inflate(R.layout.base_dialog_fragment_title, null);
        mCustomTitle.setText(R.string.test_mode_type_dialog_title);
        return mCustomTitle;
    }

    @Override
    public View getCustomMessage() {
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setItems(R.array.test_mode_type_dialog_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getActivity().getIntent();
                intent.putExtra(TestModeItemFragment.EXTRA_TEST_TYPE, which);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            }
        });
        return builder.create();
    }
}
