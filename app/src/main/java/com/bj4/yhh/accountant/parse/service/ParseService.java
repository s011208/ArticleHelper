package com.bj4.yhh.accountant.parse.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.database.ActProvider;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yenhsunhuang on 15/4/14.
 */
public class ParseService extends Service {
    private static final String TAG = "ParseService";
    private static final boolean DEBUG = true;

    private static final HandlerThread sWorkerThread = new HandlerThread("ParseService-loader");

    static {
        sWorkerThread.start();
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    private static final int MAXIMUM_ITEM_PER_REQUEST = 500;

    private static final int DELAYED_TIME = 1000 * 30;

    private static final String ACTION_PARSE_ALL_ACTS_LIST = "parse_all_acts_list";

    private final ArrayList<Runnable> mRequestTasks = new ArrayList<Runnable>();

    private void addTasks(Runnable task) {
        synchronized (mRequestTasks) {
            mRequestTasks.add(task);
        }
    }

    private void scheduleNextTask() {
        synchronized (mRequestTasks) {
            if (!mRequestTasks.isEmpty()) {
                Runnable task = mRequestTasks.remove(0);
                sWorker.post(task);
            }
            sWorker.removeCallbacks(mScheduleTaskRunnable);
            sWorker.postDelayed(mScheduleTaskRunnable, DELAYED_TIME);
        }
    }

    private final Runnable mScheduleTaskRunnable = new Runnable() {
        @Override
        public void run() {
            scheduleNextTask();
        }
    };


    public static void updateAllActsList(Context context) {
        Intent updateIntent = new Intent(context, ParseService.class);
        updateIntent.setAction(ACTION_PARSE_ALL_ACTS_LIST);
        context.startService(updateIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleNextTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sWorker.removeCallbacks(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateParseAllActsList() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ActListItem");
        query.setLimit(Integer.MAX_VALUE).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (DEBUG) {
                    Log.d(TAG, "find ActListItem size: " + parseObjects.size());
                }
                Cursor allActList = getContentResolver().query(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST), null, null, null, null);
                if (allActList != null) {
                    if (DEBUG) {
                        Log.v(TAG, "query list size: " + allActList.getCount());
                    }
                    try {
                        final ArrayList<ParseObject> allItemList = new ArrayList<ParseObject>();
                        final int indexOfUrl = allActList.getColumnIndex(ActDatabase.URL);
                        final int indexOfTitle = allActList.getColumnIndex(ActDatabase.TITLE);
                        final int indexOfAmendedDate = allActList.getColumnIndex(ActDatabase.AMENDED_DATE);
                        final int indexOfCategory = allActList.getColumnIndex(ActDatabase.CATEGORY);
                        while (allActList.moveToNext()) {
                            final String addedTitle = allActList.getString(indexOfTitle);
                            final String addedCategory = allActList.getString(indexOfCategory);
                            String objectId = null;
                            for (ParseObject originObject : parseObjects) {
                                final String originTitle = originObject.getString(ActDatabase.TITLE);
                                final String originCategory = originObject.getString(ActDatabase.CATEGORY);
                                if (addedTitle.equals(originTitle) && addedCategory.equals(originCategory)) {
                                    objectId = originObject.getObjectId();
                                    break;
                                }
                            }
                            ParseObject object = null;
                            if (objectId == null) {
                                object = new ParseObject("ActListItem");
                            } else {
                                object = ParseObject.createWithoutData("ActListItem", objectId);
                            }
                            object.put(ActDatabase.URL, allActList.getString(indexOfUrl));
                            object.put(ActDatabase.TITLE, allActList.getString(indexOfTitle));
                            object.put(ActDatabase.AMENDED_DATE, allActList.getString(indexOfAmendedDate));
                            object.put(ActDatabase.CATEGORY, allActList.getString(indexOfCategory));
                            allItemList.add(object);
                        }
                        if (!allItemList.isEmpty()) {
                            for (int i = 0; i <= (allItemList.size() / MAXIMUM_ITEM_PER_REQUEST); ++i) {
                                final int counter = i;
                                final int finalStartIndex = MAXIMUM_ITEM_PER_REQUEST * counter;
                                final int finalEndIndex = (allItemList.size() <= (finalStartIndex + MAXIMUM_ITEM_PER_REQUEST) ? allItemList.size() : (finalStartIndex + MAXIMUM_ITEM_PER_REQUEST));
                                if (DEBUG) {
                                    Log.v(TAG, "start index: " + finalStartIndex + ", end index: " + finalEndIndex);
                                }
                                if (finalStartIndex >= finalEndIndex) {
                                    continue;
                                }
                                addTasks(new Runnable() {
                                    @Override
                                    public void run() {
                                        ParseObject.saveAllInBackground(allItemList.subList(finalStartIndex, finalEndIndex), new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (DEBUG) {
                                                    Log.d(TAG, "save done, counter: " + counter);
                                                }
                                                if (e != null) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                });
                            }

                        }
                    } finally {
                        allActList.close();
                    }
                } else {
                    if (DEBUG) {
                        Log.v(TAG, "allActList is null");
                    }
                }
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (DEBUG)
                Log.v(TAG, "action: " + action);
            if (ACTION_PARSE_ALL_ACTS_LIST.equals(action)) {
                updateParseAllActsList();
            }
        } else {
            if (DEBUG)
                Log.v(TAG, "handleIntent null intent");
        }
    }
}
