package com.bj4.yhh.accountant.fragments.display.actcontent.editor;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.utils.dialogs.BaseDialogFragment;

/**
 * Created by yenhsunhuang on 15/7/30.
 */
public class ImageResourceChooser extends BaseDialogFragment {
    public interface Callback {
        void onSelectCamera();

        void onSelectGallery();
    }

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (Callback) activity;
    }

    @Override
    public int getTitleTextId() {
        return R.id.base_fragment_title;
    }

    @Override
    public View getCustomTitle() {
        TextView title = (TextView) getActivity().getLayoutInflater().inflate(R.layout.base_dialog_fragment_title, null);
        title.setText(R.string.image_resource_chooser_dialog_title);
        return title;
    }

    @Override
    public View getCustomMessage() {
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return getDialogBuilder().setItems(R.array.image_resource_chooser_dialog_resource, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mCallback.onSelectCamera();
                        break;
                    case 1:
                        mCallback.onSelectGallery();
                        break;
                }
            }
        }).create();
    }
}
