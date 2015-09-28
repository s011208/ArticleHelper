package com.bj4.yhh.lawhelper.act;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bj4.yhh.lawhelper.database.ActDatabase;
import com.bj4.yhh.lawhelper.database.ActProvider;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by yenhsunhuang on 15/4/13.
 */
public class Chapter extends ActContent {
    public Long mActId;

    private final ArrayList<Article> mArticles = new ArrayList<Article>();

    public Chapter(String number, String content, int order) {
        this(number, content, order, ActDatabase.NO_ID, false, null, -1, -1, ActDatabase.NO_ID, "", "");
    }

    public Chapter(String number, String content, int order, long id, boolean hasHighLight, ArrayList<Long> links, int drawLineStart, int drawLineEnd
            , long actId, String updateAmendedDate, String updateContent) {
        super(number, content, order, id, hasHighLight, links, drawLineStart, drawLineEnd, updateAmendedDate, updateContent);
        mActId = actId;
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
                final int highLightColumn = data.getColumnIndex(ActDatabase.HAS_STAR);
                final int linksColumn = data.getColumnIndex(ActDatabase.LINKS);
                final int drawLineStartColumn = data.getColumnIndex(ActDatabase.DRAW_LINE_START);
                final int drawLineEndColumn = data.getColumnIndex(ActDatabase.DRAW_LINE_END);
                final int actIdColumn = data.getColumnIndex(ActDatabase.ACT_ID);
                final int updateAmendedDateColumn = data.getColumnIndex(ActDatabase.UPDATE_AMENDED_DATE);
                final int updateContentColumn = data.getColumnIndex(ActDatabase.UPDATE_CONTENT);
                while (data.moveToNext()) {
                    rtn.add(new Chapter(data.getString(numberColumn), data.getString(contentColumn)
                            , data.getInt(orderColumn), data.getLong(idColumn)
                            , data.getInt(highLightColumn) == ActDatabase.TRUE
                            , ActContent.convertLinksFromJSON(data.getString(linksColumn))
                            , data.getInt(drawLineStartColumn), data.getInt(drawLineEndColumn)
                            , data.getLong(actIdColumn)
                            , data.getString(updateAmendedDateColumn), data.getString(updateContentColumn)));
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
