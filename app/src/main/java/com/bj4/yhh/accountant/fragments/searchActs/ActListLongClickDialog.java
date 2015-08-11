package com.bj4.yhh.accountant.fragments.searchActs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.ActsFolder;
import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.utils.dialogs.BaseDialogFragment;

/**
 * Created by yenhsunhuang on 15/8/4.
 */
public class ActListLongClickDialog extends BaseDialogFragment {
    private static final String TAG = "ActListLongClickDialog";
    private static final boolean DEBUG = false;

    public static final String ITEM_TYPE = "item_type";
    public static final int ITEM_TYPE_FOLDER = 0;
    public static final int ITEM_TYPE_ACTS = 1;

    public static final String FOLDER_ID = "folder_id";
    public static final String ACT_ID = "act_id";

    private int mItemType;
    private ActsFolder mActsFolder;
    private long mActId = ActDatabase.NO_ID;
    private int mItemSelectionsId;

    private TextView mCustomTitle;

    @Override
    public int getTitleTextId() {
        return mCustomTitle.getId();
    }

    @Override
    public View getCustomTitle() {
        mCustomTitle = (TextView) getActivity().getLayoutInflater().inflate(R.layout.base_dialog_fragment_title, null);
        mCustomTitle.setText(R.string.act_list_long_click_dialog);
        return mCustomTitle;
    }

    @Override
    public View getCustomMessage() {
        ListView listview = new ListView(getActivity());
        String[] data = getActivity().getResources().getStringArray(mItemSelectionsId);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, data);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG) Log.d(TAG, "position: " + position);
                if (mItemSelectionsId == R.array.act_list_long_click_action_act) {
                    switch (position) {
                        case 0: // Move to
                            SelectFolderDialog moveToDialog = new SelectFolderDialog();
                            moveToDialog.setTargetFragment(ActListLongClickDialog.this, SearchActFragment.REQUEST_MOVE_TO);
                            moveToDialog.show(getFragmentManager(), moveToDialog.getClass().getName());
                            break;
                        case 1: // Copy to
                            SelectFolderDialog copyToDialog = new SelectFolderDialog();
                            copyToDialog.setTargetFragment(ActListLongClickDialog.this, SearchActFragment.REQUEST_COPY_TO);
                            copyToDialog.show(getFragmentManager(), copyToDialog.getClass().getName());
                            break;
                        case 2: // Remove
                            ActsFolder.removeActsFolderContentById(getActivity(), mActsFolder.mId, mActId);
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                            dismiss();
                            break;
                        default:
                            if (DEBUG) Log.d(TAG, "none?");
                    }
                } else if (mItemSelectionsId == R.array.act_list_long_click_action_folder) {
                    switch (position) {
                        case 0: // Edit folder name
                            AddActsFolderDialog editFolderNameDialog = new AddActsFolderDialog();
                            editFolderNameDialog.setTargetFragment(ActListLongClickDialog.this, SearchActFragment.REQUEST_EDIT_FOLDER_TITLE);
                            editFolderNameDialog.show(getFragmentManager(), editFolderNameDialog.getClass().getName());
                            break;
                        case 1: // Remove folder
                            ActsFolder.delete(getActivity(), mActsFolder);
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                            dismiss();
                            break;
                        default:
                            if (DEBUG) Log.d(TAG, "none?");
                    }
                }
            }
        });
        return listview;
    }

    @Override
    public void onInitComponents() {
        mItemType = getArguments().getInt(ITEM_TYPE, -1);
        if (mItemType == -1) {
            throw new RuntimeException("must have ITEM_TYPE");
        }
        if (mItemType == ITEM_TYPE_ACTS) {
            mActId = getArguments().getLong(ACT_ID);
            mItemSelectionsId = R.array.act_list_long_click_action_act;
        } else {
            mItemSelectionsId = R.array.act_list_long_click_action_folder;
        }
        mActsFolder = ActsFolder.queryActsFolderById(getActivity(), getArguments().getLong(FOLDER_ID));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDialogBuilder();
        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) Log.d(TAG, "requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == SearchActFragment.REQUEST_MOVE_TO) {
            long moveToFolderId = data.getLongExtra("selected_folder_id", -1);
            if (DEBUG) Log.d(TAG, "moveToFolderId: " + moveToFolderId);
            mActsFolder.mActIds.remove(mActId);
            ActsFolder.insertOrUpdate(getActivity(), mActsFolder);
            ActsFolder.updateActsFolderContentById(getActivity(), moveToFolderId, mActId);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
            dismiss();
        } else if (requestCode == SearchActFragment.REQUEST_COPY_TO) {
            long copyToFolderId = data.getLongExtra("selected_folder_id", -1);
            if (DEBUG) Log.d(TAG, "moveToFolderId: " + copyToFolderId);
            ActsFolder.updateActsFolderContentById(getActivity(), copyToFolderId, mActId);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
            dismiss();
        } else if (requestCode == SearchActFragment.REQUEST_EDIT_FOLDER_TITLE) {
            final String newTitle = data.getStringExtra("title");
            if (DEBUG) Log.d(TAG, "edit folder title: " + newTitle);
            mActsFolder.mTitle = newTitle;
            ActsFolder.insertOrUpdate(getActivity(), mActsFolder);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
            dismiss();
        }
    }
}
