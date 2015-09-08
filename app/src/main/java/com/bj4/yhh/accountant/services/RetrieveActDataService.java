package com.bj4.yhh.accountant.services;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.bj4.yhh.accountant.act.Act;
import com.bj4.yhh.accountant.act.Article;
import com.bj4.yhh.accountant.act.Chapter;
import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.parse.util.ActListItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by yenhsunhuang on 15/7/10.
 */
public class RetrieveActDataService extends IntentService {
    private static final boolean DEBUG = false;
    private static final String TAG = "RetrieveActDataService";
    private static final String ACTION_RETRIEVE_ACT = "action_retrieve_act";

    public static final String ACTION_RETRIEVE_ACT_DATA_DONE = "com.bj4.yhh.accountant.services.ACTION_RETRIEVE_ACT_DATA_DONE";
    public static final String EXTRA_RETRIEVE_ACT_DATA_TITLE = ActDatabase.TITLE;
    public static final String EXTRA_RETRIEVE_ACT_DATA_RESULT = "EXTRA_RETRIEVE_ACT_DATA_RESULT";

    public static final String ACTION_REQUEST_TO_CHECK_UNFINISHED_TASKS = "com.bj4.yhh.accountant.services.ACTION_REQUEST_TO_CHECK_UNFINISHED_TASKS";

    private static final String KEY_RETRIEVING_ACT_ITEMS_SET = "key_retrieving_act_items_set";
    private final ArrayList<String> mRetrievingActItemList = new ArrayList<String>();
    private final ArrayList<String> mParsingActItemList = new ArrayList<String>();
    private SharedPreferences mPreferences;

    private final Handler mHandler = new Handler();

    public static void retrieveActData(Context context, ActListItem item) {
        Intent retrieveActIntent = new Intent(context, RetrieveActDataService.class);
        retrieveActIntent.putExtra(ActListItem.class.getName(), item.toString());
        retrieveActIntent.setAction(ACTION_RETRIEVE_ACT);
        context.startService(retrieveActIntent);
    }

    public static void requestToCheckUnFinishedTask(Context context) {
        Intent startServiceIntent = new Intent(context, RetrieveActDataService.class);
        context.startService(startServiceIntent);
    }

    public RetrieveActDataService() {
        super("RetrieveActDataService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        scheduleUnFinishTask(0);
    }

    private final Runnable mScheduleUnFinishTask = new Runnable() {
        @Override
        public void run() {
            synchronized (mRetrievingActItemList) {
                if (mRetrievingActItemList.isEmpty()) return;
            }
            checkUnFinishTasks();
            scheduleUnFinishTask(60 * 1000);
        }
    };

    private void scheduleUnFinishTask(final int delayed) {
        mHandler.removeCallbacks(mScheduleUnFinishTask);
        mHandler.postDelayed(mScheduleUnFinishTask, delayed);
    }

    private void checkUnFinishTasks() {
        synchronized (mRetrievingActItemList) {
            mRetrievingActItemList.clear();
            mRetrievingActItemList.addAll(mPreferences.getStringSet(KEY_RETRIEVING_ACT_ITEMS_SET, new HashSet<String>()));
            if (!mRetrievingActItemList.isEmpty()) {
                for (final String json : mRetrievingActItemList) {
                    final boolean isParsing = isItemParsing(new ActListItem(json));
                    if (DEBUG) Log.d(TAG, "remain task: " + json + ", isParsing: " + isParsing);
                    if (isParsing) continue;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final ActListItem item = new ActListItem(json);
                            retrieveActData(item);
                        }
                    }).start();
                }
            } else {
                if (DEBUG) Log.d(TAG, "no remain tasks");
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (DEBUG)
            Log.d(TAG, "RetrieveActDataService onHandleIntent");
        if (intent == null)
            return;
        final String action = intent.getAction();
        if (ACTION_RETRIEVE_ACT.equals(action)) {
            final String jsonActListItem = intent.getStringExtra(ActListItem.class.getName());
            if (jsonActListItem == null) {
                return;
            }
            final ActListItem item = new ActListItem(jsonActListItem);
            final boolean isInRetrieving = isInRetrieving(item);
            final boolean isParsing = isItemParsing(item);
            if (DEBUG)
                Log.d(TAG, "item: " + item + ", isParsing: " + isParsing + ", isInRetrieving: " + isInRetrieving);
            if (isParsing) {
                return;
            }
            if (!isInRetrieving) {
                addRetrievingActItem(item);
            }
            retrieveActData(item);
        } else if (ACTION_REQUEST_TO_CHECK_UNFINISHED_TASKS.equals(action)) {
            checkUnFinishTasks();
        }
    }

    private boolean isInRetrieving(ActListItem item) {
        if (item == null) return true;
        synchronized (mRetrievingActItemList) {
            for (String json : mRetrievingActItemList) {
                if (json.equals(item.toString())) return true;
            }
        }
        return false;
    }

    private void addRetrievingActItem(ActListItem item) {
        if (item == null) return;
        synchronized (mRetrievingActItemList) {
            mRetrievingActItemList.add(item.toString());
            mPreferences.edit().putStringSet(KEY_RETRIEVING_ACT_ITEMS_SET, new HashSet<String>(mRetrievingActItemList)).commit();
        }
    }

    private void removeRetrievingActItem(ActListItem item) {
        if (item == null) return;
        synchronized (mRetrievingActItemList) {
            mRetrievingActItemList.remove(item.toString());
            mPreferences.edit().putStringSet(KEY_RETRIEVING_ACT_ITEMS_SET, new HashSet<String>(mRetrievingActItemList)).commit();
        }
    }

    private boolean isItemParsing(ActListItem item) {
        synchronized (mParsingActItemList) {
            return mParsingActItemList.contains(item.toString());
        }
    }

    private void retrieveActData(ActListItem item) {
        synchronized (mParsingActItemList) {
            mParsingActItemList.add(item.toString());
        }
        Intent notifyIntent = new Intent(ACTION_RETRIEVE_ACT_DATA_DONE);
        boolean retrieveSuccess = false;
        try {
            final String pCode = item.mUrl.substring(item.mUrl.lastIndexOf('?'));
            Document doc = Jsoup.connect("http://law.moj.gov.tw/LawClass/LawAll.aspx" + pCode).get();
            retrieveContent(doc, item);
            notifyIntent.putExtra(EXTRA_RETRIEVE_ACT_DATA_TITLE, item.mTitle);
            notifyIntent.putExtra(EXTRA_RETRIEVE_ACT_DATA_RESULT, true);
            retrieveSuccess = true;
        } catch (IOException e) {
            if (DEBUG)
                Log.d(TAG, "failed to retrieveActData", e);
            notifyIntent.putExtra(EXTRA_RETRIEVE_ACT_DATA_RESULT, false);
            retrieveSuccess = false;

        } finally {
            sendBroadcast(notifyIntent);
            synchronized (mParsingActItemList) {
                mParsingActItemList.remove(item.toString());
            }
            if (retrieveSuccess) {
                removeRetrievingActItem(item);
            } else {
                scheduleUnFinishTask(0);
            }
        }
    }

    private long getActId(ActListItem item) {
        return Act.getActId(this, item.mTitle);
    }

    private long insertChapterDataIntoProvider(Chapter chapter, long actId) {
        ContentValues cv = new ContentValues();
        cv.put(ActDatabase.NUMBER, chapter.mNumber);
        cv.put(ActDatabase.CONTENT, chapter.mContent);
        cv.put(ActDatabase.COLUMN_ORDER, chapter.mOrder);
        cv.put(ActDatabase.ACT_ID, actId);
        Uri rtn = Chapter.insert(this, cv);
        return ContentUris.parseId(rtn);
    }

    private void retrieveContent(Document doc, ActListItem item) {
        Elements elements = doc.select("table.TableLawAll tbody tr");
        final long actId = getActId(item);
        long chapterId = ActDatabase.NO_ID;
        int chapterOrder = 0;
        int articleOrder = 0;
        final ArrayList<ContentValues> articleCvs = new ArrayList<ContentValues>();
        boolean usingEmptyChapter = false;
        Chapter emptyChapter = null;
        boolean hasAnyChapters = false;

        for (int i = 0; i < elements.size(); i++) {
            final Element element = elements.get(i);
            Chapter chapter = isChapter(element);
            final boolean isChapter = chapter != null;
            if (isChapter) {
                hasAnyChapters = true;
                chapter.mOrder = chapterOrder++;
                chapterId = insertChapterDataIntoProvider(chapter, actId);
                articleOrder = 0;
                if (DEBUG) {
                    Log.v(TAG, "chapterId: " + chapterId);
                }
            } else {
                if (chapter == null && !hasAnyChapters) {
                    if (DEBUG) Log.i(TAG, "user empty chapter");
                    if (usingEmptyChapter) {
//                        chapterId = emptyChapter.mId;
                    } else {
                        // no chapter data, create an empty one
                        emptyChapter = new Chapter("", "", ActDatabase.NO_ID);
                        emptyChapter.mOrder = chapterOrder++;
                        chapterId = insertChapterDataIntoProvider(emptyChapter, actId);
                        articleOrder = 0;
                        if (DEBUG) {
                            Log.i(TAG, "create empty chapter");
                        }
                        usingEmptyChapter = true;
                    }

                }
                Elements articleNumberElements = element.select("td a[href]");
                String articleNumber = articleNumberElements.get(0).text();
                Elements articleContentElements = element.select("td pre");
                String articleContent = articleContentElements.get(0).text();
                if (DEBUG) {
                    Log.v(TAG, "articleNumber: " + articleNumber + ", articleContent: " + articleContent);
                }
                ContentValues cv = new ContentValues();
                cv.put(ActDatabase.NUMBER, articleNumber);
                cv.put(ActDatabase.CONTENT, articleContent);
                cv.put(ActDatabase.COLUMN_ORDER, articleOrder++);
                cv.put(ActDatabase.CHAPTER_ID, chapterId);
                articleCvs.add(cv);
            }
        }
        ContentValues[] cvs = articleCvs.toArray(new ContentValues[0]);
        final int insertArticles = Article.bulkInsert(this, cvs);
        if (DEBUG) {
            Log.v(TAG, "insert articles: " + insertArticles);
        }
        if (insertArticles > 0) {
            ContentValues cv = new ContentValues();
            cv.put(ActDatabase.HAS_LOAD_SUCCESS, ActDatabase.TRUE);
            Act.update(this, cv, ActDatabase.ID + "=" + actId, null);
        }
    }

    private Chapter isChapter(Element element) {
        Chapter rtn = null;
        Elements childrenElement = element.select("td[colspan=3] pre");
        if (childrenElement.size() > 0) {
            String wholeContent = childrenElement.text().trim();
            final int lastIndexSpace = wholeContent.lastIndexOf(" ");
            String number = wholeContent.substring(0, lastIndexSpace);
            String content = wholeContent.substring(lastIndexSpace + 1);
            rtn = new Chapter(number, content, ActDatabase.NO_ID);
            if (DEBUG) Log.i(TAG, "isChapter, chapter: " + rtn);
        }
        return rtn;
    }
}
