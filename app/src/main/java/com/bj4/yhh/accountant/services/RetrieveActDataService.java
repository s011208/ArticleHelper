package com.bj4.yhh.accountant.services;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

/**
 * Created by yenhsunhuang on 15/7/10.
 */
public class RetrieveActDataService extends IntentService {
    private static final boolean DEBUG = true;
    private static final String TAG = "RetrieveActDataService";
    private static final String ACTION_RETRIEVE_ACT = "action_retrieve_act";

    public static final String ACTION_RETRIEVE_ACT_DATA_DONE = "com.bj4.yhh.accountant.services.ACTION_RETRIEVE_ACT_DATA_DONE";
    public static final String EXTRA_RETRIEVE_ACT_DATA_TITLE = ActDatabase.TITLE;
    public static final String EXTRA_RETRIEVE_ACT_DATA_RESULT = "EXTRA_RETRIEVE_ACT_DATA_RESULT";

    public static void retrieveActData(Context context, ActListItem item) {
        Intent retrieveActIntent = new Intent(context, RetrieveActDataService.class);
        retrieveActIntent.putExtra(ActListItem.class.getName(), item.toString());
        retrieveActIntent.setAction(ACTION_RETRIEVE_ACT);
        context.startService(retrieveActIntent);
    }

    public RetrieveActDataService() {
        super("RetrieveActDataService");
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
            retrieveActData(item);
        }
    }

    private void retrieveActData(ActListItem item) {
        Intent notifyIntent = new Intent(ACTION_RETRIEVE_ACT_DATA_DONE);
        try {
            final String pCode = item.mUrl.substring(item.mUrl.lastIndexOf('?'));
            Document doc = Jsoup.connect("http://law.moj.gov.tw/LawClass/LawAll.aspx" + pCode).get();
            retrieveContent(doc, item);
            notifyIntent.putExtra(EXTRA_RETRIEVE_ACT_DATA_TITLE, item.mTitle);
            notifyIntent.putExtra(EXTRA_RETRIEVE_ACT_DATA_RESULT, true);
        } catch (IOException e) {
            if (DEBUG)
                Log.d(TAG, "failed to retrieveActData", e);
            notifyIntent.putExtra(EXTRA_RETRIEVE_ACT_DATA_RESULT, false);
        } finally {
            sendBroadcast(notifyIntent);
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
                    if(DEBUG) Log.i(TAG, "user empty chapter");
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
