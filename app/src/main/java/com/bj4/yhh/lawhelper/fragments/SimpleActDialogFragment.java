package com.bj4.yhh.lawhelper.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.utils.dialogs.BaseDialogFragment;

/**
 * Created by yenhsunhuang on 15/8/22.
 */
public class SimpleActDialogFragment extends BaseDialogFragment {
    public static final String EXTRA_CLICK_ITEM = "extra_click_item";

    public interface Callback {
        void onActClicked(Act act);
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
        return R.string.menu_list_items_text_update_acts;
    }

    @Override
    public String getTitleText() {
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDialogBuilder();
        final SimpleActListAdapter adapter = new SimpleActListAdapter(getActivity());
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Act act = adapter.getItem(i);
                if (mCallback != null) {
                    mCallback.onActClicked(act);
                } else {
                    if (getTargetFragment() != null) {
                        Intent intent = getActivity().getIntent();
                        intent.putExtra(EXTRA_CLICK_ITEM, act.toString());
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    }
                }
            }
        });
        return builder.create();
    }
}
