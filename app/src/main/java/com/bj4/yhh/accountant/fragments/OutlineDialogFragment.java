package com.bj4.yhh.accountant.fragments;

import android.view.View;

import com.bj4.yhh.accountant.utils.dialogs.BaseDialogFragment;

/**
 * Created by yenhsunhuang on 15/8/17.
 */
public class OutlineDialogFragment extends BaseDialogFragment {
    @Override
    public int getTitleTextId() {
        return 0;
    }

    @Override
    public View getCustomTitle() {
        return null;
    }

    @Override
    public View getCustomMessage() {
        return null;
    }
}
