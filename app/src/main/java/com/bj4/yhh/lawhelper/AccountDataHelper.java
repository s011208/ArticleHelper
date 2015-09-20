package com.bj4.yhh.lawhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.bj4.yhh.lawhelper.act.Act;
import com.bj4.yhh.lawhelper.database.ActDatabase;
import com.bj4.yhh.lawhelper.database.ActProvider;
import com.bj4.yhh.lawhelper.parse.service.ParseService;
import com.bj4.yhh.lawhelper.parse.util.ActListItem;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yenhsunhuang on 15/7/7.
 */
public class AccountDataHelper {
    private static final boolean DEBUG = true;
    private static final String TAG = "AccountDataHelper";
    private static final int PARSE_QUERY_LIMIT = 500;
    private static final HandlerThread sWorkerThread = new HandlerThread("AccountDataHelper-loader");

    static {
        sWorkerThread.start();
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    public synchronized static AccountDataHelper getInstance(Context context) {
        if (sAccountDataHelper == null) {
            sAccountDataHelper = new AccountDataHelper(context);
        }
        return sAccountDataHelper;
    }

    public interface Callback {
        void onStartRetrieveAllActDataFromParse();

        void onFinishRetrieveAllActDataFromParse();

        void onProgressUpdate(int progress, String extraMessage);
    }

    private final Handler mHandler = new Handler();

    public final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    private int mParseActItemCount = -1;

    public void addCallback(Callback cb) {
        if (cb == null) return;
        if (!mCallbacks.contains(cb)) {
            mCallbacks.add(cb);
        }
    }

    public void removeCallback(Callback cb) {
        if (cb == null) return;
        mCallbacks.remove(cb);
    }

    private boolean mIsRetrieveDataSuccess = false;

    public boolean isRetrieveDataSuccess() {
        return mIsRetrieveDataSuccess;
    }

    private boolean mIsRetrieveDataFromParse = false;

    public boolean isRetrievingDataFromParse() {
        return mIsRetrieveDataFromParse;
    }

    private void onFinishRetrieveAllActDataFromParse() {
        mIsRetrieveDataFromParse = false;
        onProgressUpdate(100, "");
        if (!mCallbacks.isEmpty()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (Callback cb : mCallbacks) {
                        cb.onFinishRetrieveAllActDataFromParse();
                    }
                }
            });
        }
    }

    private void onProgressUpdate(final int progress, final String extraMessage) {
        if (!mCallbacks.isEmpty()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (Callback cb : mCallbacks) {
                        cb.onProgressUpdate(progress, extraMessage);
                    }
                }
            });
        }
    }

    private void onStartRetrieveAllActDataFromParse() {
        mIsRetrieveDataFromParse = true;
        if (!mCallbacks.isEmpty()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (Callback cb : mCallbacks) {
                        cb.onStartRetrieveAllActDataFromParse();
                    }
                }
            });
        }
    }

    private static AccountDataHelper sAccountDataHelper;

    private static final ArrayList<Act> sActs = new ArrayList<Act>();


    private final Context mContext;

    private AccountDataHelper(Context context) {
        mContext = context.getApplicationContext();
        registerReceivers();
    }

    public static ArrayList<Act> getActs() {
        return sActs;
    }

    private void runOnWorker(Runnable r) {
        if (android.os.Process.myTid() == sWorkerThread.getThreadId()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }

    private void getActItemCount(final GetCallback cb) {
        ParseQuery.getQuery("ActItemCount").getFirstInBackground(cb);
    }

    private void getParseObjectCount(final String object, final CountCallback cb) {
        ParseQuery.getQuery(object).countInBackground(cb);
    }

    public int getAllActListCount() {
        int localAllActListCount = 0;
        Cursor data = mContext.getContentResolver().query(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST_COUNT), null, null, null, null);
        if (data != null) {
            try {
                while (data.moveToNext()) {
                    localAllActListCount = data.getInt(0);
                }
            } finally {
                data.close();
            }
        }
        return localAllActListCount;
    }

    public void parseAllActListFromParse() {
        onStartRetrieveAllActDataFromParse();
        Runnable insertOrUpdateTask = new Runnable() {
            @Override
            public void run() {
                getActItemCount(new GetCallback<ParseObject>() {
                                    @Override
                                    public void done(ParseObject parseObject, ParseException e) {
                                        if (parseObject == null) {
                                            mParseActItemCount = -1;
                                        } else {
                                            mParseActItemCount = parseObject.getInt("total_count");
                                        }
                                        final int localAllActListCount = getAllActListCount();
                                        Log.d(TAG, "getActItemCount done, mParseActItemCount: " + mParseActItemCount + ", localAllActListCount: " + localAllActListCount, e);
                                        if (mParseActItemCount >= 0 && localAllActListCount == mParseActItemCount) {
                                            if (DEBUG) {
                                                Log.v(TAG, "all act list has been synced");
                                            }
                                            mIsRetrieveDataSuccess = true;
                                            onFinishRetrieveAllActDataFromParse();
                                            return;
                                        } else {
                                            if (DEBUG) {
                                                Log.v(TAG, "start to update/insert act items");
                                            }
                                            final Runnable retrieveDataTask = new Runnable() {
                                                @Override
                                                public void run() {
                                                    int progress = 0;
                                                    onProgressUpdate(progress, "");
                                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("ActListItem_test");
                                                    List<ParseObject> listOnParse = new ArrayList<ParseObject>();
                                                    try {
                                                        final int totalQueryTime = mParseActItemCount / PARSE_QUERY_LIMIT + 1;
                                                        for (int i = 0; i < totalQueryTime; ++i) {
                                                            listOnParse.addAll(query.setLimit(PARSE_QUERY_LIMIT).setSkip(i * PARSE_QUERY_LIMIT).find());
                                                            if (DEBUG)
                                                                Log.v(TAG, "listOnParse size: " + listOnParse.size());
                                                            progress = 20 + (70 / totalQueryTime) * i;
                                                            onProgressUpdate(progress, "");
                                                        }
                                                        final ArrayList<ActListItem> itemInLocal = ActListItem.queryFromProvider(mContext, null, null, null, null);
                                                        final ArrayList<ActListItem> itemToAdd = new ArrayList<ActListItem>();
                                                        final ArrayList<ActListItem> itemToUpdate = new ArrayList<ActListItem>();
                                                        for (int i = 0; i < listOnParse.size(); i++) {
                                                            ParseObject object = listOnParse.get(i);
                                                            ActListItem item = new ActListItem(object.getString(ActDatabase.URL), object.getString(ActDatabase.TITLE),
                                                                    object.getString(ActDatabase.AMENDED_DATE), object.getString(ActDatabase.CATEGORY));
                                                            int indexOfItem = itemInLocal.indexOf(item);
                                                            if (indexOfItem == -1) {
                                                                // insert
                                                                itemToAdd.add(item);
                                                            } else {
                                                                // update
                                                                ActListItem tempIndexOfItem = itemInLocal.get(indexOfItem);
                                                                if (tempIndexOfItem.mAmendedDate.equals(item.mAmendedDate))
                                                                    continue;
                                                                tempIndexOfItem.mAmendedDate = item.mAmendedDate;
                                                                itemToUpdate.add(tempIndexOfItem);
                                                            }
                                                        }
                                                        if (DEBUG)
                                                            Log.d(TAG, "itemInLocal size: " + itemInLocal.size() + ", itemToAdd size: " + itemToAdd.size()
                                                                    + ", itemToUpdate size: " + itemToUpdate.size());
                                                        progress = 95;
                                                        onProgressUpdate(progress, "");
                                                        final ContentValues[] insertCvs = new ContentValues[itemToAdd.size()];
                                                        for (int i = 0; i < itemToAdd.size(); i++) {
                                                            insertCvs[i] = itemToAdd.get(i).getContentValues();
                                                        }
                                                        int rtn = mContext.getContentResolver().bulkInsert(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST_FROM_PARSE), insertCvs);
                                                        for (int i = 0; i < itemToUpdate.size(); i++) {
                                                            mContext.getContentResolver().update(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST_FROM_PARSE), itemToUpdate.get(i).getContentValues(), ActDatabase.ID + "=" + itemToUpdate.get(i).mId, null);
                                                        }
                                                        // delete duplicated items
                                                        int deletedDuplicatedItems = mContext.getContentResolver().delete(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST_REMOVE_DUPLICATED), null, null);
                                                        Log.d(TAG, "deletedDuplicatedItems: " + deletedDuplicatedItems);
                                                        progress = 98;
                                                        onProgressUpdate(progress, "");
                                                        if (DEBUG) {
                                                            Log.v(TAG, "listOnParse size: " + listOnParse.size() + ", insert row: " + rtn);
                                                            Log.d(TAG, "pid: " + android.os.Process.myPid() + ", tid: " + android.os.Process.myTid());
                                                        }
                                                        mIsRetrieveDataSuccess = true;
                                                    } catch (ParseException e) {
                                                        Log.w(TAG, "failed to find all act listOnParse item", e);
                                                    }
                                                    onFinishRetrieveAllActDataFromParse();
                                                }
                                            };
                                            if (mParseActItemCount <= 0) {
                                                // try to get count from list table
                                                getParseObjectCount("ActListItem", new CountCallback() {
                                                    @Override
                                                    public void done(final int parseDataCount, final ParseException e) {
                                                        mParseActItemCount = parseDataCount;
                                                        runOnWorker(retrieveDataTask);
                                                    }
                                                });
                                            } else {
                                                runOnWorker(retrieveDataTask);
                                            }
                                        }
                                    }
                                }
                );
            }
        };
        runOnWorker(insertOrUpdateTask);
    }

    private void registerReceivers() {
        registerActItemListReceiver();
    }

    private void registerActItemListReceiver() {
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST), true, new ContentObserver(sWorker) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Log.d(TAG, "registerActItemListReceiver onChange, selfChange: " + selfChange);
                if (!selfChange) {
                    ParseService.updateAllActsList(mContext);
                }
            }
        });
    }
}
