package com.bj4.yhh.lawhelper.parse.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.bj4.yhh.lawhelper.database.ActProvider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/7/5.
 */
public class ActTypeParser {
    private static final boolean DEBUG = false;
    private static final String TAG = "ActTypeParser";

    public interface Callback {
        public void onStartParse();

        public void onProgressUpdate(int value);

        public void onFinishParse();
    }

    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    private Context mContext;

    private ParseAllActsTask mParseAllActsTask;

    public ActTypeParser(Context context) {
        this(context, null);
    }

    public ActTypeParser(Context context, Callback cb) {
        mContext = context;
        if (cb != null) {
            addCallback((cb));
        }
    }

    public void addCallback(Callback cb) {
        if (cb != null && mCallbacks.contains(cb) == false)
            mCallbacks.add(cb);
    }

    public void removeCallback(Callback cb) {
        if (cb != null)
            mCallbacks.remove(cb);
    }

    private void insertAllActsList(ArrayList<ActListItem> items) {
        ContentValues[] cvs = new ContentValues[items.size()];
        for (int i = 0; i < items.size(); i++) {
            cvs[i] = items.get(i).getContentValues();
        }
        int rtn = mContext.getContentResolver().bulkInsert(Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ALL_ACTS_LIST), cvs);
        Log.i(TAG, "insertAllActsList bulkInsert size: " + rtn);
    }

    private ArrayList<String> getAllLawClassListN() {
        final ArrayList<String> rtn = new ArrayList<String>();
        try {
            Document doc = Jsoup.connect("http://law.moj.gov.tw/LawClass/LawClassListN.aspx").get();
            Elements aHrefElements = doc.select("a[href]");
            if (DEBUG)
                Log.d(TAG, "elements size: " + aHrefElements.size());
            for (Element ele : aHrefElements) {
                final String url = ele.attr("href");
                if (url.startsWith("LawClassListN.aspx?")) {
                    rtn.add("http://law.moj.gov.tw/LawClass/" + url);
                }
                if (DEBUG)
                    Log.v(TAG, "text:" + ele.text()
                            + "\nattr: " + ele.attr("href"));
            }
        } catch (Exception e) {
            Log.w(TAG, "failed to run getParseActTypeRunnable", e);
        }
        return rtn;
    }

    private ArrayList<ActListItem> getAllLawList(ArrayList<String> allLawClassListN) {
        final ArrayList<ActListItem> rtn = new ArrayList<ActListItem>();
        if (allLawClassListN == null)
            return rtn;
        if (DEBUG)
            Log.v(TAG, "all law size: " + allLawClassListN.size());
        int percent = 0;
        for (String url : allLawClassListN) {
            try {
                Document doc = Jsoup.connect(url).get();
                Elements eleTitle = doc.select("div.classtitle ul li");
                String lawUrl, title, category, amendedDate;
                if (!eleTitle.isEmpty()) {
                    category = eleTitle.first().text();
                    if (DEBUG)
                        Log.d(TAG, "category: " + eleTitle.first().text());
                    int counter = 0;
                    Elements eleLaws = doc.select("a[href][title]");
                    for (Element lawItem : eleLaws) {
                        lawUrl = lawItem.attr("href");
                        if (lawUrl.contains("PCode")) {
                            ++counter;
                            title = lawItem.attr("title");
                            amendedDate = lawItem.parent().ownText();
                            lawUrl = "http://law.moj.gov.tw/LawClass/LawContent.aspx?" + lawUrl.substring(lawUrl.indexOf("PCode"));
                            ActListItem actItem = new ActListItem(lawUrl, title, amendedDate, category);
                            if (DEBUG)
                                Log.v(TAG, "actItem: " + actItem.toString());
                            rtn.add(actItem);
                        }
                    }
                    if (DEBUG)
                        Log.v(TAG, "law size: " + counter);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ++percent;
            mParseAllActsTask.publishProgress((int) ((percent / (float) allLawClassListN.size()) * 100));
        }
        return rtn;
    }

    private Runnable getParseActTypeRunnable() {
        Runnable rtn = new Runnable() {
            @Override
            public void run() {
                insertAllActsList(getAllLawList(getAllLawClassListN()));
            }
        };
        return rtn;
    }

    private class ParseAllActsTask extends AsyncTask<Void, Integer, Void> {
        private Runnable mTask;

        private ParseAllActsTask(Runnable task) {
            mTask = task;
        }

        public void publishProgress(int value) {
            super.publishProgress(value);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mTask.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            notifyOnProgressUpdate(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyOnFinishParse();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            notifyOnStartParse();
        }
    }

    public void parse(final boolean async) {
        final Runnable parseTask = getParseActTypeRunnable();
        if (!async) {
            notifyOnStartParse();
            parseTask.run();
            notifyOnFinishParse();
        } else {
            mParseAllActsTask = new ParseAllActsTask(parseTask);
            mParseAllActsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    private void notifyOnFinishParse() {
        for (Callback cb : mCallbacks) {
            cb.onFinishParse();
        }
    }

    private void notifyOnStartParse() {
        for (Callback cb : mCallbacks) {
            cb.onStartParse();
        }
    }

    private void notifyOnProgressUpdate(int value) {
        for (Callback cb : mCallbacks) {
            cb.onProgressUpdate(value);
        }
    }
}
