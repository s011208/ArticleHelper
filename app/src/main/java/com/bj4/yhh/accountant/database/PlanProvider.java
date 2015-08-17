package com.bj4.yhh.accountant.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.bj4.yhh.accountant.fragments.plan.Plan;

/**
 * Created by yenhsunhuang on 15/8/18.
 */
public class PlanProvider extends ContentProvider {
    private static final boolean DEBUG = true;
    private static final String TAG = "PlanProvider";

    private PlanDatabase mPlanDatabase;

    public static final String AUTHORITY = "com.bj4.yhh.accountant.PlanProvider";

    public static final String PATH_PLAN = "path_plan";
    private static final int CODE_PLAN = 1000;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, PATH_PLAN, CODE_PLAN);
    }

    @Override
    public boolean onCreate() {
        mPlanDatabase = PlanDatabase.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_PLAN:
                return mPlanDatabase.getDataBase().query(PlanDatabase.TABLE_PLAN, projection, selection, selectionArgs, null, null, sortOrder);
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
                return ContentUris.withAppendedId(uri, mPlanDatabase.getDataBase().insert(PlanDatabase.TABLE_PLAN, null, values));
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_PLAN:
                return mPlanDatabase.getDataBase().delete(PlanDatabase.TABLE_PLAN, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_PLAN:
                return mPlanDatabase.getDataBase().update(PlanDatabase.TABLE_PLAN, values, selection, selectionArgs);
        }
        return 0;
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

        private SQLiteDatabase mDb;
        private Context mContext;

        public PlanDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            createTableActsFolder();
        }

        private void createTableActsFolder() {
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
