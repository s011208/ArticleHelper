package com.bj4.yhh.lawhelper.fragments.searchActs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.act.ActsFolder;
import com.bj4.yhh.lawhelper.act.Note;
import com.bj4.yhh.lawhelper.database.ActDatabase;
import com.bj4.yhh.lawhelper.fragments.plan.Plan;
import com.bj4.yhh.lawhelper.utils.dialogs.BaseDialogFragment;
import com.bj4.yhh.lawhelper.utils.dialogs.ConfirmDialogFragment;

import java.util.ArrayList;

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
                            boolean findActsInOtherFolders = findActsInOtherFolders();
                            if (DEBUG)
                                Log.d(TAG, "findActsInOtherFolders: " + findActsInOtherFolders);
                            if (!findActsInOtherFolders) {
                                showConfirmDialogWhenDeleteAct();
                            } else {
                                ActsFolder.removeActsFolderContentById(getActivity(), mActsFolder.mId, mActId);
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                                dismiss();
                            }
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

    private boolean findActsInOtherFolders() {
        ArrayList<ActsFolder> folders = ActsFolder.query(getActivity(), null, null, null, null);
        for (ActsFolder f : folders) {
            if (f.mId == mActsFolder.mId) {
                continue;
            }
            if (f.mActIds.contains(mActId)) {
                return true;
            }
        }
        return false;
    }

    private void deleteActAndAllRelatedItems() {
        final Context context = getActivity();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Act act = Act.queryActById(context, mActId);
                Plan plan = Plan.queryByActId(context, mActId);
                // delete plan
                if (plan != null) {
                    Plan.deletePlan(context, plan.mId);
                }
                // delete note
                Note.deleteAllNotesByAct(context, act);
                // delete act
                Act.deleteActById(context, mActId);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // delete act in folder
        ActsFolder.removeActsFolderContentById(getActivity(), mActsFolder.mId, mActId);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
        dismiss();
    }

    private void showConfirmDialogWhenDeleteAct() {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment();
        dialog.setTargetFragment(this, ConfirmDialogFragment.REQUEST_CONFIRM);
        Bundle args = new Bundle();
        Act act = Act.queryActById(getActivity(), mActId);
        args.putString(ConfirmDialogFragment.ARGUS_TITLE, getActivity().getResources().getString(R.string.act_list_long_click_dialog_delete_all_act_content_title, act.getTitle()));
        args.putString(ConfirmDialogFragment.ARGUS_MESSAGE, getActivity().getResources().getString(R.string.act_list_long_click_dialog_delete_all_act_content_message, act.getTitle()));
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), ConfirmDialogFragment.class.getName());
    }


    @Override
    public int getTitleTextResources() {
        return R.string.act_list_long_click_dialog;
    }

    @Override
    public String getTitleText() {
        return null;
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
        } else if (requestCode == ConfirmDialogFragment.REQUEST_CONFIRM) {
            if (DEBUG) Log.d(TAG, "confirm delete dialog");
            deleteActAndAllRelatedItems();
        }
    }
}
