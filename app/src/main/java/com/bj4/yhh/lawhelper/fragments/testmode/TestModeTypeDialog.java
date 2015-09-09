package com.bj4.yhh.lawhelper.fragments.testmode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.utils.dialogs.BaseDialogFragment;

/**
 * Created by yenhsunhuang on 15/8/26.
 */
public class TestModeTypeDialog extends BaseDialogFragment {

    @Override
    public View getCustomMessage() {
        return null;
    }

    @Override
    public int getTitleTextResources() {
        return R.string.test_mode_type_dialog_title;
    }

    @Override
    public String getTitleText() {
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
