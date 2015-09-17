package com.bj4.yhh.lawhelper.fragments.searchActs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;

import com.bj4.yhh.lawhelper.AccountDataHelper;
import com.bj4.yhh.lawhelper.R;
import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.act.ActsFolder;
import com.bj4.yhh.lawhelper.database.ActDatabase;
import com.bj4.yhh.lawhelper.database.ActProvider;
import com.bj4.yhh.lawhelper.fragments.display.ActFragment;
import com.bj4.yhh.lawhelper.fragments.display.actcontent.DisplayActContentFragment;
import com.bj4.yhh.lawhelper.parse.util.ActListItem;
import com.bj4.yhh.lawhelper.services.RetrieveActDataService;
import com.bj4.yhh.lawhelper.utils.BaseFragment;
import com.bj4.yhh.lawhelper.utils.BaseToast;
import com.bj4.yhh.lawhelper.utils.FloatingActionButton;
import com.bj4.yhh.lawhelper.utils.SunProgressBar;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/9.
 */
public class SearchActFragment extends BaseFragment implements AccountDataHelper.Callback {
    private static final boolean DEBUG = false;
    private static final String TAG = "SearchActFragment";

    public static final int REQUEST_ADD_TITLE = 1000;
    public static final int REQUEST_SELECT_FOLDER = 1001;
    public static final int REQUEST_UPDATE_EXPANDABLE_ITEMS = 1002;
    public static final int REQUEST_MOVE_TO = 1003;
    public static final int REQUEST_COPY_TO = 1004;
    public static final int REQUEST_EDIT_FOLDER_TITLE = 1006;

    private final Handler mHandler = new Handler();
    private AccountDataHelper mAccountDataHelper;

    private AutoCompleteTextView mAutoCompleteTextView;

    private SunProgressBar mAutoCompleteLoadingProgressBar;
    private ExpandableListView mExpandableListView;
    private ExpandableActListAdapter mExpandableActListAdapter;

    private FloatingActionButton mFloatingActionButton;

    private String mPendingActTitle;

    private int mExpandableListViewArrowWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountDataHelper = AccountDataHelper.getInstance(getActivity());
        mAccountDataHelper.addCallback(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterObserver();
    }

    @Override
    public void onStart() {
        super.onStart();
        registerObserver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAccountDataHelper.removeCallback(this);
    }

    private void setActData() {
        if (mExpandableActListAdapter != null) {
            mExpandableActListAdapter.updateContent();
            mExpandableActListAdapter.notifyDataSetChanged();
        }
    }

    private final ContentObserver mActObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (DEBUG) {
                Log.v(TAG, "mActObserver get called, selfChange: " + selfChange);
            }
            if (!selfChange) {
                setActData();
            }
        }
    };

    private final ContentObserver mAllActsListFromParseObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (!selfChange) {
                setAutoCompleteTextViewItem();
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) return;
            if (RetrieveActDataService.ACTION_RETRIEVE_ACT_DATA_DONE.equals(action)) {
                boolean retrieveSuccess = intent.getBooleanExtra(RetrieveActDataService.EXTRA_RETRIEVE_ACT_DATA_RESULT, false);
                if (retrieveSuccess) {
                    String resultTitle = intent.getStringExtra(RetrieveActDataService.EXTRA_RETRIEVE_ACT_DATA_TITLE);
                    if (resultTitle == null) return;
                    if (mExpandableActListAdapter != null) {
                        mExpandableActListAdapter.updateContent();
                        mExpandableActListAdapter.notifyDataSetChanged();
                    }
                } else {
                    // ignore
                }
            }
        }
    };

    private void registerObserver() {
        getActivity().getContentResolver().registerContentObserver(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ACTS), true, mActObserver);
        getActivity().getContentResolver().registerContentObserver(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST_FROM_PARSE), true, mAllActsListFromParseObserver);
        IntentFilter filter = new IntentFilter(RetrieveActDataService.ACTION_RETRIEVE_ACT_DATA_DONE);
        getActivity().registerReceiver(mReceiver, filter);
    }

    private void unregisterObserver() {
        getActivity().getContentResolver().unregisterContentObserver(mAllActsListFromParseObserver);
        getActivity().getContentResolver().unregisterContentObserver(mActObserver);
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.search_act_fragment, null);
        initAutoCompleteTextView(root);
        initExpandedListView(root);
        initFloatingActionButton(root);
        return root;
    }

    private void initFloatingActionButton(View root) {
        Resources res = getActivity().getResources();
        mFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.floating_action_button);
        mFloatingActionButton.setIconDrawable(res.getDrawable(R.drawable.white_folder_plus))
                .setTinitColor(res.getColor(R.color.main_title_color))
                .setPressTintColor(res.getColor(R.color.main_title_color_dark)).build();
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddActsFolderDialog dialog = new AddActsFolderDialog();
                dialog.setTargetFragment(SearchActFragment.this, REQUEST_ADD_TITLE);
                dialog.show(getFragmentManager(), AddActsFolderDialog.class.getName());
            }
        });
        mFloatingActionButton.bringToFront();
    }

    private void initExpandedListView(View root) {
        mExpandableListViewArrowWidth = getActivity().getResources().getDimensionPixelSize(R.dimen.selected_act_expandable_list_view_arrow_width);
        mExpandableListView = (ExpandableListView) root.findViewById(R.id.expand_act_list);
        mExpandableActListAdapter = new ExpandableActListAdapter(getActivity());
        mExpandableListView.setAdapter(mExpandableActListAdapter);
        mExpandableListView.expandGroup(0);
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Act clickedItem = mExpandableActListAdapter.getChild(groupPosition, childPosition);
                showActContent(clickedItem);
                return false;
            }
        });

        mExpandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ExpandableListView listView = (ExpandableListView) parent;
                final long pos = listView.getExpandableListPosition(position);
                final int groupPosition = ExpandableListView.getPackedPositionGroup(pos);
                if (DEBUG) Log.d(TAG, "onItemLongClick groupPosition: " + groupPosition);
                ActListLongClickDialog dialog = new ActListLongClickDialog();
                dialog.setTargetFragment(SearchActFragment.this, REQUEST_UPDATE_EXPANDABLE_ITEMS);
                Bundle argus = new Bundle();
                argus.putLong(ActListLongClickDialog.FOLDER_ID, mExpandableActListAdapter.getGroup(groupPosition).mId);
                if (ExpandableListView.getPackedPositionType(pos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    final int childPosition = ExpandableListView.getPackedPositionChild(pos);
                    if (DEBUG) Log.d(TAG, "onItemLongClick childPosition: " + childPosition);
                    argus.putInt(ActListLongClickDialog.ITEM_TYPE, ActListLongClickDialog.ITEM_TYPE_ACTS);
                    argus.putLong(ActListLongClickDialog.ACT_ID, mExpandableActListAdapter.getChild(groupPosition, childPosition).getId());
                } else {
                    argus.putInt(ActListLongClickDialog.ITEM_TYPE, ActListLongClickDialog.ITEM_TYPE_FOLDER);
                }
                dialog.setArguments(argus);
                dialog.show(getFragmentManager(), dialog.getClass().getName());
                return true;
            }
        });
        mExpandableListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver vto = mExpandableListView.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.removeOnGlobalLayoutListener(this);
                }
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    mExpandableListView.setIndicatorBounds(mExpandableListView.getRight() - mExpandableListViewArrowWidth, mExpandableListView.getWidth());
                } else {
                    mExpandableListView.setIndicatorBoundsRelative(mExpandableListView.getRight() - mExpandableListViewArrowWidth, mExpandableListView.getWidth());
                }
            }
        });
    }


    private void showActContent(Act act) {
        if (DEBUG)
            Log.d(TAG, "clickedItem: " + act);
        if (!act.hasLoadedSuccess()) {
            final String toast = getString(R.string.act_has_not_loaded_toast, act.getTitle());
            BaseToast.showToast(getActivity(), toast);
            return;
        } else {
            ActFragment displayActContent = new DisplayActContentFragment();
            Bundle args = new Bundle();
            args.putString(ActFragment.ARGUMENT_JSON_ACT, act.toJson().toString());
            displayActContent.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.container, displayActContent, DisplayActContentFragment.class.getName()).addToBackStack(null).commitAllowingStateLoss();
            if (mAutoCompleteTextView != null) {
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mAutoCompleteTextView.getWindowToken(), 0);
            }
        }
    }

    private void initAutoCompleteTextView(View root) {
        mAutoCompleteTextView = (AutoCompleteTextView) root.findViewById(R.id.auto);
        mAutoCompleteTextView.setThreshold(2);
        mAutoCompleteTextView.setSingleLine(true);
        mAutoCompleteTextView.setMaxLines(1);
        mAutoCompleteTextView.setEllipsize(TextUtils.TruncateAt.END);
        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                mPendingActTitle = item;
                SelectFolderDialog dialog = new SelectFolderDialog();
                dialog.setTargetFragment(SearchActFragment.this, REQUEST_SELECT_FOLDER);
                dialog.show(getFragmentManager(), SelectFolderDialog.class.getName());
            }
        });
        mAutoCompleteLoadingProgressBar = (SunProgressBar) root.findViewById(R.id.auto_loading_progress);
        setAutoCompleteTextViewItem();
    }

    @Override
    public void onStartRetrieveAllActDataFromParse() {
        mAutoCompleteLoadingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishRetrieveAllActDataFromParse() {
        mAutoCompleteLoadingProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onProgressUpdate(int progress, String extraMessage) {
    }

    @Override
    public boolean onBackPress() {
        return false;
    }

    public static void addTitle(Context context, String title) {
        ActsFolder actsFolder = new ActsFolder(title);
        ActsFolder.insertOrUpdate(context, actsFolder);
        Log.d(TAG, "addTitle title: " + title);
    }

    private void onAddTitle(String title) {
        addTitle(getActivity(), title);
        mExpandableActListAdapter.updateContent();
        mExpandableActListAdapter.notifyDataSetChanged();
    }

    private class OnAutoCompleteItemClickTask extends AsyncTask<Void, Void, Boolean> {
        private final String mTitle;

        private final Context mContext;

        private final long mFolderId;

        public OnAutoCompleteItemClickTask(String title, Context context, long folderId) {
            mTitle = title;
            mContext = context.getApplicationContext();
            mFolderId = folderId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAutoCompleteTextView.setAdapter(null);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            setAutoCompleteTextViewItem();
            mAutoCompleteTextView.setText("");
            if (mExpandableActListAdapter != null) {
                mExpandableActListAdapter.updateContent();
                mExpandableActListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final ActListItem item = getActListItem();
            if (item == null) {
                return false;
            }
            ContentValues cv = new ContentValues();
            cv.put(ActDatabase.TITLE, item.mTitle);
            cv.put(ActDatabase.AMENDED_DATE, item.mAmendedDate);
            cv.put(ActDatabase.CATEGORY, item.mCategory);
            cv.put(ActDatabase.HAS_LOAD_SUCCESS, ActDatabase.FALSE);
            final String pCode = item.mUrl.substring(item.mUrl.lastIndexOf('?'));
            cv.put(ActDatabase.URL, "http://law.moj.gov.tw/LawClass/LawAll.aspx" + pCode);
            if (DEBUG) Log.d(TAG, "ActDatabase.URL: " + cv.getAsString(ActDatabase.URL));
            long actId = ContentUris.parseId(mContext.getContentResolver().insert(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ACTS), cv));
            ActsFolder.updateActsFolderContentById(getActivity(), mFolderId, actId);
            RetrieveActDataService.retrieveActData(mContext, item);
            return true;
        }

        private ActListItem getActListItem() {
            ActListItem item = null;
            Cursor data = mContext.getContentResolver().query(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST), null, ActDatabase.TITLE + "='" + mTitle + "'", null, null);
            if (data != null) {
                try {
                    if (data.moveToFirst()) {
                        item = new ActListItem(data.getString(data.getColumnIndex(ActDatabase.URL)), data.getString(data.getColumnIndex(ActDatabase.TITLE)),
                                data.getString(data.getColumnIndex(ActDatabase.AMENDED_DATE)), data.getString(data.getColumnIndex(ActDatabase.CATEGORY)));
                    }
                } finally {
                    data.close();
                }
            }
            return item;
        }
    }

    private void setAutoCompleteTextViewItem() {
        new AsyncTask<Void, Void, Boolean>() {
            final ArrayList<ActListItem> mResult = new ArrayList<ActListItem>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mAutoCompleteLoadingProgressBar != null) {
                    mAutoCompleteLoadingProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                final Activity activity = getActivity();
                if (activity == null)
                    return false;
                final ArrayList<Act> actList = Act.query(activity, null, null, null, null);
                final ArrayList<String> actListTitle = new ArrayList<String>();
                for (Act act : actList) {
                    actListTitle.add(act.getTitle());
                    if (DEBUG)
                        Log.d(TAG, "act: " + act.getTitle());
                }

                Cursor allActList = activity.getContentResolver().query(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST), null, null, null, null);
                if (allActList != null) {
                    try {
                        final int indexOfUrl = allActList.getColumnIndex(ActDatabase.URL);
                        final int indexOfTitle = allActList.getColumnIndex(ActDatabase.TITLE);
                        final int indexOfAmendedDate = allActList.getColumnIndex(ActDatabase.AMENDED_DATE);
                        final int indexOfCategory = allActList.getColumnIndex(ActDatabase.CATEGORY);
                        while (allActList.moveToNext()) {
                            final String title = allActList.getString(indexOfTitle);
                            if (actListTitle.contains(title)) continue;
                            mResult.add(new ActListItem(allActList.getString(indexOfUrl), title
                                    , allActList.getString(indexOfAmendedDate), allActList.getString(indexOfCategory)));
                        }
                    } finally {
                        allActList.close();
                    }
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result == false) {
                    return;
                }
                final Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                ArrayList<String> data = new ArrayList<String>();
                for (ActListItem item : mResult) {
                    data.add(item.mTitle);
                }
                ArrayAdapter<String> adapter = new ActAutoCompleteAdapter(activity,
                        android.R.layout.simple_dropdown_item_1line, data);
                mAutoCompleteTextView.setAdapter(adapter);
                if (DEBUG)
                    Log.v(TAG, "set auto complete Adapter done, data size: " + data.size());
                if (mAutoCompleteLoadingProgressBar != null) {
                    if (!mAccountDataHelper.isRetrievingDataFromParse()) {
                        mAutoCompleteLoadingProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_TITLE) {
            if (resultCode == Activity.RESULT_OK) {
                final String title = data.getStringExtra("title");
                onAddTitle(title);
            }
        } else if (requestCode == REQUEST_SELECT_FOLDER) {
            if (resultCode == Activity.RESULT_OK) {
                final long selectedFolderId = data.getLongExtra("selected_folder_id", -1);
                if (selectedFolderId != -1) {
                    onAddAct(selectedFolderId);
                }
            }
        } else if (requestCode == REQUEST_UPDATE_EXPANDABLE_ITEMS) {
            if (resultCode == Activity.RESULT_OK) {
                setActData();
                setAutoCompleteTextViewItem();
            }
        }
        mPendingActTitle = null;
        if (DEBUG)
            Log.d(TAG, "requestCode: " + requestCode + ", resultCode: " + resultCode);
    }

    private void onAddAct(long folderId) {
        if (mPendingActTitle == null || folderId < 0)
            return;
        new OnAutoCompleteItemClickTask(mPendingActTitle, getActivity(), folderId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
