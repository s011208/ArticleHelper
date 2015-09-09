package com.bj4.yhh.accountant.act;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bj4.yhh.accountant.database.ActDatabase;
import com.bj4.yhh.accountant.database.ActProvider;

import java.util.ArrayList;

/**
 * Created by yenhsunhuang on 15/4/13.
 */
public class Article extends ActContent {
    public long mChapterId;

    public Article(String number, String content, int order) {
        this(number, content, order, ActDatabase.NO_ID, false, null, -1, -1, ActDatabase.NO_ID);
    }

    public Article(String number, String content, int order, long id, boolean hasHighLight, ArrayList<Long> links, int drawLineStart, int drawLineEnd, long chapterId) {
        super(number, content, order, id, hasHighLight, links, drawLineStart, drawLineEnd);
        mChapterId = chapterId;
    }

    public Article(String jsonString) {
        super(jsonString);
    }

    private static Uri getBaseUri() {
        return Uri.parse("content://" + ActProvider.AUTHORITY + "/" + ActProvider.PATH_ARTICLES);
    }

    public static int deleteByChapterId(Context context, long chapterId) {
        return context.getContentResolver().delete(getBaseUri(), ActDatabase.CHAPTER_ID + "=" + chapterId, null);
    }

    public static int delete(Context context, Article article) {
        if (article == null) {
            return 0;
        }
        return context.getContentResolver().delete(getBaseUri(), ActDatabase.ID + "=" + article.mId, null);
    }

    public static Uri insert(Context context, ContentValues values) {
        return context.getContentResolver().insert(getBaseUri(), values);
    }

    public static int bulkInsert(Context context, ContentValues[] values) {
        return context.getContentResolver().bulkInsert(getBaseUri(), values);
    }

    public static ArrayList<Article> queryArticleByChapterId(Context context, long chapterId) {
        return query(context, null, ActDatabase.CHAPTER_ID + "=" + chapterId, null, ActDatabase.COLUMN_ORDER + " asc");
    }

    public static ArrayList<Article> quertArticleByArticleId(Context context, long articleId) {
        return query(context, null, ActDatabase.ID + "=" + articleId, null, ActDatabase.COLUMN_ORDER + " asc");
    }

    public static ArrayList<Article> query(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final ArrayList<Article> rtn = new ArrayList<Article>();
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
                final int chapterIdColumn = data.getColumnIndex(ActDatabase.CHAPTER_ID);
                while (data.moveToNext()) {
                    rtn.add(new Article(data.getString(numberColumn), data.getString(contentColumn)
                            , data.getInt(orderColumn), data.getLong(idColumn)
                            , data.getInt(highLightColumn) == ActDatabase.TRUE
                            , ActContent.convertLinksFromJSON(data.getString(linksColumn))
                            , data.getInt(drawLineStartColumn), data.getInt(drawLineEndColumn)
                            , data.getLong(chapterIdColumn)));
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }
}
