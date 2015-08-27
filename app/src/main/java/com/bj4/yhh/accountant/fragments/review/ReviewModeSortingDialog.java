package com.bj4.yhh.accountant.fragments.review;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.utils.dialogs.BaseDialogFragment;

/**
 * Created by yenhsunhuang on 15/8/24.
 */
public class ReviewModeSortingDialog extends BaseDialogFragment {
    public interface Callback {
        void onSorting(int sortingType);
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
        return R.string.review_mode_fragment_sorting_dialog_title;
    }

    @Override
    public String getTitleText() {
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setItems(R.array.review_mode_fragment_sorting_dialog_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCallback != null) {
                    mCallback.onSorting(which);
                } else {
                    getActivity().getIntent().putExtra("sorting_type", which);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());

                }
            }
        });
        return builder.create();
    }
}
