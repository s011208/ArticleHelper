package com.bj4.yhh.accountant.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.bj4.yhh.accountant.fragments.plan.Plan;
import com.bj4.yhh.accountant.fragments.test.TestItem;

/**
 * Created by yenhsunhuang on 15/8/18.
 */
public class PlanProvider extends ContentProvider {
    private static final boolean DEBUG = true;
    private static final String TAG = "PlanProvider";

    public static final String AUTHORITY = "com.bj4.yhh.accountant.PlanProvider";

    public static final String PATH_PLAN = "path_plan";
    private static final int CODE_PLAN = 1000;

    public static final String PATH_TEST_ITEM = "path_test_item";
    private static final int CODE_TEST_ITEM = 2000;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, PATH_PLAN, CODE_PLAN);
        URI_MATCHER.addURI(AUTHORITY, PATH_TEST_ITEM, CODE_TEST_ITEM);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_PLAN:
                return PlanDatabase.getInstance(getContext()).getDataBase().query(PlanDatabase.TABLE_PLAN, projection, selection, selectionArgs, null, null, sortOrder);
            case CODE_TEST_ITEM:
                return PlanDatabase.getInstance(getContext()).getDataBase().query(PlanDatabase.TABLE_TEST_ITEM, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_PLAN:
                return ContentUris.withAppendedId(uri, PlanDatabase.getInstance(getContext()).getDataBase().insert(PlanDatabase.TABLE_PLAN, null, values));
            case CODE_TEST_ITEM:
                return ContentUris.withAppendedId(uri, PlanDatabase.getInstance(getContext()).getDataBase().insert(PlanDatabase.TABLE_TEST_ITEM, null, values));
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_PLAN:
                return PlanDatabase.getInstance(getContext()).getDataBase().delete(PlanDatabase.TABLE_PLAN, selection, selectionArgs);
            case CODE_TEST_ITEM:
                return PlanDatabase.getInstance(getContext()).getDataBase().delete(PlanDatabase.TABLE_TEST_ITEM, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_PLAN:
                return PlanDatabase.getInstance(getContext()).getDataBase().update(PlanDatabase.TABLE_PLAN, values, selection, selectionArgs);
            case CODE_TEST_ITEM:
                return PlanDatabase.getInstance(getContext()).getDataBase().update(PlanDatabase.TABLE_TEST_ITEM, values, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        String table = null;
        switch (URI_MATCHER.match(uri)) {
            case CODE_PLAN:
                table = PlanDatabase.TABLE_PLAN;
                break;
            case CODE_TEST_ITEM:
                table = PlanDatabase.TABLE_TEST_ITEM;
                break;
        }
        if (table == null)
            return super.bulkInsert(uri, values);
        else {
            int numInserted = 0;
            SQLiteDatabase sqlDB = PlanDatabase.getInstance(getContext()).getDataBase();
            sqlDB.beginTransaction();
            try {
                for (ContentValues cv : values) {
                    long newID = sqlDB.insertOrThrow(table, null, cv);
                    if (newID <= 0) {
                        throw new SQLException("Failed to insert row into " + uri);
                    }
                }
                sqlDB.setTransactionSuccessful();
                numInserted = values.length;
            } finally {
                sqlDB.endTransaction();
            }
            return numInserted;
        }
    }

    private static class PlanDatabase extends SQLiteOpenHelper {
        private static PlanDatabase sInstance;

        public synchronized static final PlanDatabase getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new PlanDatabase(context.getApplicationContext());
            }
            return sInstance;
        }

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "plan_database.db";

        private static final String TABLE_PLAN = "table_plan";
        private static final String TABLE_TEST_ITEM = "table_test_item";
        private SQLiteDatabase mDb;
        private Context mContext;

        public PlanDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            mDb = getWritableDatabase();
            createTablePlan();
            createTableTestItem();
        }

        private void createTableTestItem() {
            final SQLiteDatabase database = getDataBase();
            if (database == null)
                return;
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TEST_ITEM + " ("
                    + TestItem.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TestItem.PLAN_ID + " INTEGER,"
                    + TestItem.HAS_FAILED + " INTEGER,"
                    + TestItem.IS_ANSWER + " INTEGER,"
                    + TestItem.IS_READ + " INTEGER,"
                    + TestItem.DISPLAY_DAY + " INTEGER,"
                    + TestItem.ACT_ID + " INTEGER,"
                    + TestItem.CHAPTER_ID + " INTEGER,"
                    + TestItem.ARTICLE_ID + " INTEGER)");
        }

        private void createTablePlan() {
            final SQLiteDatabase database = getDataBase();
            if (database == null)
                return;
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PLAN + " ("
                    + Plan.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Plan.ACT_ID + " INTEGER,"
                    + Plan.PLAN_ORDER + " INTEGER,"
                    + Plan.FINISHED_ITEM + " INTEGER,"
                    + Plan.TOTAL_ITEMS + " INTEGER,"
                    + Plan.TOTAL_PLAN_PROGRESS + " INTEGER,"
                    + Plan.CURRENT_PLAN_PROGRESS + " INTEGER)");
        }

        public SQLiteDatabase getDataBase() {
            if (mDb != null && mDb.isOpen() == false) {
                try {
                    mDb = getWritableDatabase();
                } catch (SQLiteFullException e) {
                    Log.w(TAG, "SQLiteFullException", e);
                } catch (SQLiteException e) {
                    Log.w(TAG, "SQLiteException", e);
                } catch (Exception e) {
                    Log.w(TAG, "Exception", e);
                }
            }
            return mDb;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
