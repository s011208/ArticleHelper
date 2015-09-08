package com.bj4.yhh.accountant;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.database.ActProvider;
import com.bj4.yhh.accountant.parse.service.ParseService;
import com.bj4.yhh.accountant.parse.util.ActListItem;
import com.parse.CountCallback;
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

        void onProgressUpdate(int progress);
    }

    private final Handler mHandler = new Handler();

    public final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

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
        onProgressUpdate(100);
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

    private void onProgressUpdate(final int progress) {
        if (!mCallbacks.isEmpty()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (Callback cb : mCallbacks) {
                        cb.onProgressUpdate(progress);
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
        getParseObjectCount("ActListItem", new CountCallback() {
            @Override
            public void done(final int parseDataCount, final ParseException e) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        int progress = 0;
                        onProgressUpdate(progress);
                        if (e != null) {
                            Log.w(TAG, "failed to getCount", e);
                            onFinishRetrieveAllActDataFromParse();
                            return;
                        }
                        if (DEBUG) {
                            Log.i(TAG, "ActListItem count: " + parseDataCount);
                        }
                        int localAllActListCount = getAllActListCount();
                        progress = 20;
                        onProgressUpdate(progress);
                        if (localAllActListCount == parseDataCount) {
                            if (DEBUG) {
                                Log.v(TAG, "all act list has been synced");
                            }
                            mIsRetrieveDataSuccess = true;
                            onFinishRetrieveAllActDataFromParse();
                            return;
                        }
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("ActListItem");
                        List<ParseObject> list = new ArrayList<ParseObject>();
                        try {
                            final int totalQueryTime = parseDataCount / PARSE_QUERY_LIMIT + 1;
                            for (int i = 0; i < totalQueryTime; ++i) {
                                list.addAll(query.setLimit(PARSE_QUERY_LIMIT).setSkip(i * PARSE_QUERY_LIMIT).find());
                                Log.v(TAG, "list size: " + list.size());
                                progress = 20 + (70 / totalQueryTime) * i;
                                onProgressUpdate(progress);
                            }
                            if (list.size() <= localAllActListCount) {
                                onFinishRetrieveAllActDataFromParse();
                                return;
                            }
                            mContext.getContentResolver().delete(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST_REMOVE_ALL), null, null);
                            final ContentValues[] cvs = new ContentValues[list.size()];
                            for (int i = 0; i < list.size(); i++) {
                                ParseObject object = list.get(i);
                                cvs[i] = new ActListItem(object.getString(ActDatabase.URL), object.getString(ActDatabase.TITLE),
                                        object.getString(ActDatabase.AMENDED_DATE), object.getString(ActDatabase.CATEGORY)).getContentValues();
                            }
                            progress = 95;
                            onProgressUpdate(progress);
                            int rtn = mContext.getContentResolver().bulkInsert(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST_FROM_PARSE), cvs);
                            progress = 98;
                            onProgressUpdate(progress);
                            if (DEBUG) {
                                Log.v(TAG, "list size: " + list.size() + ", insert row: " + rtn);
                                Log.d(TAG, "pid: " + android.os.Process.myPid() + ", tid: " + android.os.Process.myTid());
                            }
                            mIsRetrieveDataSuccess = true;
                        } catch (ParseException e1) {
                            Log.w(TAG, "failed to find all act list item", e);
                        }
                        onFinishRetrieveAllActDataFromParse();
                    }
                };
                runOnWorker(task);
            }
        });
    }

    private void registerReceivers() {
        registerActItemListReceiver();
    }

    private void registerActItemListReceiver() {
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST), true, new ContentObserver(sWorker) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (!selfChange) {
                    ParseService.updateAllActsList(mContext);
                }
            }
        });
    }
}
