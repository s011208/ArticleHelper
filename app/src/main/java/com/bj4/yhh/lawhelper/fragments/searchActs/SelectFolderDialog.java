package com.bj4.yhh.lawhelper.fragments.searchActs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.ActsFolder;
import com.bj4.yhh.lawhelper.database.ActDatabase;
import com.bj4.yhh.lawhelper.utils.dialogs.BaseDialogFragment;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/8/3.
 */
public class SelectFolderDialog extends BaseDialogFragment {
    private static final String TAG = "SelectFolderDialog";
    private static final boolean DEBUG = false;

    private View mCustomMessage;
    private ListView mFolderList;
    private ArrayAdapter<String> mFolderListAdapter;
    private ArrayList<ActsFolder> mActFolders;

    private int mRequestCode = 0;

    private String[] getFolders() {
        mActFolders = new ArrayList<ActsFolder>();
        mActFolders.clear();
        mActFolders.addAll(ActsFolder.query(getActivity(), null, null, null, ActDatabase.ACT_FOLDER_TITLE));
        boolean addNewFolder = false;
        if ((mRequestCode & SearchActFragment.REQUEST_COPY_TO) != 0 || (mRequestCode & SearchActFragment.REQUEST_MOVE_TO) != 0) {
            addNewFolder = false;
        } else {
            addNewFolder = true;
        }

        if (DEBUG) {
            Log.d(TAG, "addNewFolder: " + addNewFolder + ", mRequestCode: " + mRequestCode);
        }
        final String[] items = new String[mActFolders.size() + (addNewFolder ? 1 : 0)];
        for (int i = 0; i < mActFolders.size(); i++) {
            items[i] = mActFolders.get(i).mTitle;
        }
        if (addNewFolder) {
            items[mActFolders.size()] = getActivity().getResources().getString(R.string.select_folder_dialog_new_folder);
        }
        return items;
    }

    private void setAdapter() {
        mFolderList = (ListView) mCustomMessage.findViewById(R.id.folder_list);
        mFolderListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, getFolders());
        mFolderList.setAdapter(mFolderListAdapter);
    }

    @Override
    public View getCustomMessage() {
        mCustomMessage = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.select_act_folder_dialog_view, null);
        setAdapter();
        mFolderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == mActFolders.size()) {
                    AddActsFolderDialog dialog = new AddActsFolderDialog();
                    dialog.setTargetFragment(SelectFolderDialog.this, SearchActFragment.REQUEST_ADD_TITLE);
                    dialog.show(getFragmentManager(), AddActsFolderDialog.class.getName());
                } else {
                    onItemSelected(mActFolders.get(position).mId);
                }
            }
        });
        return mCustomMessage;
    }

    @Override
    public int getTitleTextResources() {
        return R.string.select_folder_dialog_title;
    }

    @Override
    public String getTitleText() {
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode & SearchActFragment.REQUEST_ADD_TITLE) != 0) {
            if (resultCode == Activity.RESULT_OK) {
                final String title = data.getStringExtra("title");
                SearchActFragment.addTitle(getActivity(), title);
                setAdapter();
            }
        }
    }

    private void onItemSelected(long folderId) {
        Intent intent = getActivity().getIntent();
        intent.putExtra("selected_folder_id", folderId);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }

    public void onInitComponents() {
        mRequestCode = getTargetRequestCode();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDialogBuilder();
        return builder.create();
    }
}
