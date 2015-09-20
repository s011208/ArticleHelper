package com.bj4.yhh.lawhelper.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


/**
 * Created by yenhsunhuang on 15/4/14.
 */
public class ActProvider extends ContentProvider {
    private static final String TAG = "ActProvider";
    private static final boolean DEBUG = true;
    public static final String AUTHORITY = "com.bj4.yhh.lawhelper";
    // all acts list
    public static final String PATH_ALL_ACTS_LIST = "all_acts_list"; // from ActTypeParser
    public static final String PATH_ALL_ACTS_LIST_FROM_PARSE = "all_acts_list_from_parse"; // from parse.com
    public static final String PATH_ALL_ACTS_LIST_COUNT = "all_acts_list_count";
    public static final String PATH_ALL_ACTS_LIST_REMOVE_ALL = "all_acts_list_remove_all";
    public static final String PATH_ALL_ACTS_LIST_REMOVE_DUPLICATED = "all_acts_list_remove_duplicated";

    public static final int CODE_ALL_ACTS_LIST = 1000;
    public static final int CODE_ALL_ACTS_LIST_COUNT = 1001;
    public static final int CODE_ALL_ACTS_LIST_REMOVE_ALL = 1002;
    public static final int CODE_ALL_ACTS_LIST_REMOVE_DUPLICATED = 1003;

    // acts
    public static final String PATH_ACTS = "acts";

    public static final int CODE_ACTS = 2000;

    // chapters
    public static final String PATH_CHAPTERS = "chapters";

    public static final int CODE_CHAPTERS = 3001;

    // articles
    public static final String PATH_ARTICLES = "articles";

    public static final int CODE_ARTICLES = 4001;

    // notes
    public static final String PATH_NOTES = "notes";

    public static final int CODE_NOTES = 5000;

    // acts folder
    public static final String PATH_ACTS_FOLDER = "acts_folder";

    public static final int CODE_ACTS_FOLDER = 6000;

    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mUriMatcher.addURI(AUTHORITY, PATH_ALL_ACTS_LIST, CODE_ALL_ACTS_LIST);
        mUriMatcher.addURI(AUTHORITY, PATH_ALL_ACTS_LIST_FROM_PARSE, CODE_ALL_ACTS_LIST);
        mUriMatcher.addURI(AUTHORITY, PATH_ALL_ACTS_LIST_COUNT, CODE_ALL_ACTS_LIST_COUNT);
        mUriMatcher.addURI(AUTHORITY, PATH_ALL_ACTS_LIST_REMOVE_ALL, CODE_ALL_ACTS_LIST_REMOVE_ALL);
        mUriMatcher.addURI(AUTHORITY, PATH_ALL_ACTS_LIST_REMOVE_DUPLICATED, CODE_ALL_ACTS_LIST_REMOVE_DUPLICATED);

        mUriMatcher.addURI(AUTHORITY, PATH_ACTS, CODE_ACTS);

        mUriMatcher.addURI(AUTHORITY, PATH_CHAPTERS, CODE_CHAPTERS);

        mUriMatcher.addURI(AUTHORITY, PATH_ARTICLES, CODE_ARTICLES);

        mUriMatcher.addURI(AUTHORITY, PATH_NOTES, CODE_NOTES);

        mUriMatcher.addURI(AUTHORITY, PATH_ACTS_FOLDER, CODE_ACTS_FOLDER);
    }

    private ActDatabase mActDatabase;

    @Override
    public boolean onCreate() {
        mActDatabase = new ActDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor rtn = null;
        switch (mUriMatcher.match(uri)) {
            case CODE_ALL_ACTS_LIST:
                rtn = mActDatabase.getDataBase().query(ActDatabase.TABLE_ALL_ACTS_LISTS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_ALL_ACTS_LIST_COUNT:
                rtn = mActDatabase.getDataBase().rawQuery("select count(*) from " + ActDatabase.TABLE_ALL_ACTS_LISTS, null, null);
                break;
            case CODE_ACTS:
                rtn = mActDatabase.getDataBase().query(ActDatabase.TABLE_ACTS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_CHAPTERS:
                rtn = mActDatabase.getDataBase().query(ActDatabase.TABLE_CHAPTERS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_ARTICLES:
                rtn = mActDatabase.getDataBase().query(ActDatabase.TABLE_ARTICLES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_NOTES:
                rtn = mActDatabase.getDataBase().query(ActDatabase.TABLE_NOTES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_ACTS_FOLDER:
                rtn = mActDatabase.getDataBase().query(ActDatabase.TABLE_ACTS_FOLDER, projection, selection, selectionArgs, null, null, sortOrder);
                break;
        }
        return rtn;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rtn = ActDatabase.NO_ID;
        Log.d(TAG, "insert called");
        switch (mUriMatcher.match(uri)) {
            case CODE_ALL_ACTS_LIST:
                break;
            case CODE_ACTS:
                rtn = mActDatabase.getDataBase().insert(ActDatabase.TABLE_ACTS, null, values);
                break;
            case CODE_CHAPTERS:
                rtn = mActDatabase.getDataBase().insert(ActDatabase.TABLE_CHAPTERS, null, values);
                break;
            case CODE_ARTICLES:
                rtn = mActDatabase.getDataBase().insert(ActDatabase.TABLE_ARTICLES, null, values);
                break;
            case CODE_NOTES:
                rtn = mActDatabase.getDataBase().insert(ActDatabase.TABLE_NOTES, null, values);
                break;
            case CODE_ACTS_FOLDER:
                rtn = mActDatabase.getDataBase().insert(ActDatabase.TABLE_ACTS_FOLDER, null, values);
                break;
        }
        uri = ContentUris.withAppendedId(uri, rtn);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int rtn = 0;
        switch (mUriMatcher.match(uri)) {
            case CODE_ALL_ACTS_LIST:
                rtn = mActDatabase.bulkInsert(values, ActDatabase.TABLE_ALL_ACTS_LISTS);
                break;
            case CODE_CHAPTERS:
                rtn = mActDatabase.bulkInsert(values, ActDatabase.TABLE_CHAPTERS);
                break;
            case CODE_ARTICLES:
                rtn = mActDatabase.bulkInsert(values, ActDatabase.TABLE_ARTICLES);
                break;
            case CODE_NOTES:
                rtn = mActDatabase.bulkInsert(values, ActDatabase.TABLE_NOTES);
                break;
            case CODE_ACTS_FOLDER:
                rtn = mActDatabase.bulkInsert(values, ActDatabase.TABLE_ACTS_FOLDER);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rtn;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rtn = 0;
        switch (mUriMatcher.match(uri)) {
            case CODE_ALL_ACTS_LIST_REMOVE_DUPLICATED:
                rtn = mActDatabase.getDataBase().delete(ActDatabase.TABLE_ALL_ACTS_LISTS, ActDatabase.ID + " not in (select max(" + ActDatabase.ID + ") from " + ActDatabase.TABLE_ALL_ACTS_LISTS + " group by " + ActDatabase.TITLE + ")", null);
                break;
            case CODE_ALL_ACTS_LIST:
                rtn = mActDatabase.getDataBase().delete(ActDatabase.TABLE_ALL_ACTS_LISTS, selection, selectionArgs);
                break;
            case CODE_ALL_ACTS_LIST_REMOVE_ALL:
                rtn = mActDatabase.getDataBase().delete(ActDatabase.TABLE_ALL_ACTS_LISTS, null, null);
                break;
            case CODE_ACTS:
                rtn = mActDatabase.getDataBase().delete(ActDatabase.TABLE_ACTS, selection, selectionArgs);
                break;
            case CODE_ARTICLES:
                rtn = mActDatabase.getDataBase().delete(ActDatabase.TABLE_ARTICLES, selection, selectionArgs);
                break;
            case CODE_CHAPTERS:
                rtn = mActDatabase.getDataBase().delete(ActDatabase.TABLE_CHAPTERS, selection, selectionArgs);
                break;
            case CODE_NOTES:
                rtn = mActDatabase.getDataBase().delete(ActDatabase.TABLE_NOTES, selection, selectionArgs);
                break;
            case CODE_ACTS_FOLDER:
                rtn = mActDatabase.getDataBase().delete(ActDatabase.TABLE_ACTS_FOLDER, selection, selectionArgs);
                break;
        }
        return rtn;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rtn = 0;
        switch (mUriMatcher.match(uri)) {
            case CODE_ACTS:
                rtn = mActDatabase.getDataBase().update(ActDatabase.TABLE_ACTS, values, selection, selectionArgs);
                break;
            case CODE_ARTICLES:
                rtn = mActDatabase.getDataBase().update(ActDatabase.TABLE_ARTICLES, values, selection, selectionArgs);
                break;
            case CODE_CHAPTERS:
                rtn = mActDatabase.getDataBase().update(ActDatabase.TABLE_CHAPTERS, values, selection, selectionArgs);
                break;
            case CODE_NOTES:
                rtn = mActDatabase.getDataBase().update(ActDatabase.TABLE_NOTES, values, selection, selectionArgs);
                break;
            case CODE_ACTS_FOLDER:
                rtn = mActDatabase.getDataBase().update(ActDatabase.TABLE_ACTS_FOLDER, values, selection, selectionArgs);
                break;
            case CODE_ALL_ACTS_LIST:
                rtn = mActDatabase.getDataBase().update(ActDatabase.TABLE_ALL_ACTS_LISTS, values, selection, selectionArgs);
                break;
        }
        return rtn;
    }
}
