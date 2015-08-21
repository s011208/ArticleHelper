package com.bj4.yhh.accountant.act;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.database.ActProvider;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by yenhsunhuang on 15/4/13.
 */
public class Chapter extends ActContent {

    private final ArrayList<Article> mArticles = new ArrayList<Article>();

    public Chapter(String number, String content, int order) {
        this(number, content, order, ActDatabase.NO_ID, false);
    }

    public Chapter(String number, String content, int order, long id, boolean hasHighLight) {
        super(number, content, order, id, hasHighLight);
    }

    public Chapter(String jsonString) {
        super(jsonString);
    }

    public ArrayList<Article> getArticles() {
        return mArticles;
    }

    public void addAtricle(Article article) {
        mArticles.add(article);
        Collections.sort(mArticles);
    }

    public void setArticles(ArrayList<Article> articles) {
        mArticles.clear();
        mArticles.addAll(articles);
    }

    private static Uri getBaseUri() {
        return Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_CHAPTERS);
    }

    public static int deleteByActId(Context context, long actId) {
        return context.getContentResolver().delete(getBaseUri(), ActDatabase.ACT_ID + "=" + actId, null);
    }

    public static int delete(Context context, Chapter chapter) {
        if (chapter == null)
            return 0;
        return context.getContentResolver().delete(getBaseUri(), ActDatabase.ID + "=" + chapter.mId, null);
    }

    public static Uri insert(Context context, ContentValues values) {
        return context.getContentResolver().insert(getBaseUri(), values);
    }

    public static void queryAllChapterContent(Context context, Chapter chapter) {
        final ArrayList<Article> articles = chapter.getArticles();
        articles.clear();
        articles.addAll(Article.queryArticleByChapterId(context, chapter.mId));
    }

    public static ArrayList<Chapter> queryChapterByActId(Context context, long actId) {
        return query(context, null, ActDatabase.ACT_ID + "=" + actId, null, ActDatabase.ACT_ID + " asc");
    }

    public static ArrayList<Chapter> queryChapterByChapterId(Context context, long chapterId) {
        return query(context, null, ActDatabase.ID + "=" + chapterId, null, null);
    }

    public static ArrayList<Chapter> query(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final ArrayList<Chapter> rtn = new ArrayList<Chapter>();
        Cursor data = context.getContentResolver().query(getBaseUri(), projection, selection, selectionArgs, sortOrder);
        if (data != null) {
            try {
                final int idColumn = data.getColumnIndex(ActDatabase.ID);
                final int numberColumn = data.getColumnIndex(ActDatabase.NUMBER);
                final int contentColumn = data.getColumnIndex(ActDatabase.CONTENT);
                final int orderColumn = data.getColumnIndex(ActDatabase.COLUMN_ORDER);
                final int highLightColumn = data.getColumnIndex(ActDatabase.HIGHLIGHT);
                while (data.moveToNext()) {
                    rtn.add(new Chapter(data.getString(numberColumn), data.getString(contentColumn), data.getInt(orderColumn), data.getLong(idColumn), data.getInt(highLightColumn) == ActDatabase.TRUE));
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }

    public boolean isEmptyChapter() {
        return "".equals(mNumber) && "".equals(mContent);
    }
}
