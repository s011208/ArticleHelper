package com.bj4.yhh.accountant.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.act.ActsFolder;

/**
 * Created by yenhsunhuang on 15/4/14.
 */
public class ActDatabase extends SQLiteOpenHelper {
    private static final String TAG = "ActDatabase";
    private static final boolean DEBUG = true;
    private static final String DATABASE_NAME = "act.db";
    private static final int DATABASE_VERSION = 1;

    // tables
    public static final String TABLE_ACTS = "acts";
    public static final String TABLE_ARTICLES = "articles";
    public static final String TABLE_CHAPTERS = "chapters";
    public static final String TABLE_NOTES = "notes";
    public static final String TABLE_ACTS_FOLDER = "acts_folder";

    /*
    * see ParseActItems
    * */
    public static final String TABLE_ALL_ACTS_LISTS = "all_acts_lists";

    // columns for all
    public static final String ID = "_id";
    public static final int NO_ID = -1;
    public static final String COLUMN_ORDER = "column_order";

    // act
    public static final String TYPE = "type_";
    public static final String TITLE = "title_";
    public static final String AMENDED_DATE = "amended_date";
    public static final String CATEGORY = "category_";
    public static final String HAS_LOAD_SUCCESS = "load_success";
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    // columns for chapter & article
    public static final String NUMBER = "number_";
    public static final String CONTENT = "content_";
    public static final String HAS_STAR = "high_light";
    public static final String HAS_IMAGE_NOTE = "has_image_note";
    public static final String HAS_TEXT_NOTE = "has_text_note";
    public static final String LINKS = "links_list";
    public static final String DRAW_LINE_START = "draw_line_start";
    public static final String DRAW_LINE_END = "draw_line_end";

    // acts folder
    public static final String ACT_FOLDER_TITLE = "act_folder_title";
    public static final String ACTS_ID = "acts_id";
    public static final String IS_DEFAULT_ITEM = "is_default";

    // chapter
    public static final String ACT_ID = "act_id";

    // article
    public static final String CHAPTER_ID = "chapter_id";

    // all acts lists
    public static final String URL = "url";

    // note
    public static final String ARTICLE_ID = "article_id";
    /* *
     * indicates act, article or chapter
     */
    public static final String NOTE_PARENT_TYPE = "note_parent_type";
    public static final int NOTE_PARENT_TYPE_ACT = 0;
    public static final int NOTE_PARENT_TYPE_CHAPTER = 1;
    public static final int NOTE_PARENT_TYPE_ARTICLE = 2;

    /* *
     * base on NOTE_PARENT_TYPE
     */
    public static final String NOTE_PARENT_ID = "note_parent_id";
    /* *
     * indicates text of image
     */
    public static final String NOTE_TYPE = "note_type";
    public static final int NOTE_TYPE_TEXT = 0;
    public static final int NOTE_TYPE_IMAGE = 1;

    public static final String NOTE_CONTENT = "note_content";

    // preference key
    private static final String KEY_HAS_INIT = "has_init";

    private SQLiteDatabase mDb;

    private SharedPreferences mPrefs;

    private Context mContext;

    public ActDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        try {
            mDb = getWritableDatabase();
            mPrefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            createTables();
        } catch (SQLiteFullException e) {
            Log.w(TAG, "SQLiteFullException", e);
        } catch (SQLiteException e) {
            Log.w(TAG, "SQLiteException", e);
        } catch (Exception e) {
            Log.w(TAG, "Exception", e);
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public int bulkInsert(ContentValues[] cvs, String table) {
        int counter = 0;
        SQLiteDatabase sqlDB = getDataBase();
        sqlDB.beginTransaction();
        try {
            for (ContentValues cv : cvs) {
                long newID = sqlDB.insertOrThrow(table, null, cv);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + table);
                }
                ++counter;
            }
            sqlDB.setTransactionSuccessful();
        } finally {
            sqlDB.endTransaction();
        }
        return counter;
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

    private void createTables() {
        createTableAct();
        createTableChapter();
        createTableArticle();
        createTableAllActsList();
        createTableNote();
        createTableActsFolder();
    }

    private void createTableActsFolder() {
        final SQLiteDatabase database = getDataBase();
        if (database == null)
            return;
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ACTS_FOLDER + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ACTS_ID + " TEXT,"
                + IS_DEFAULT_ITEM + " INTEGER,"
                + ACT_FOLDER_TITLE + " TEXT)");
        insertDefaultActsFolder();
    }

    private void insertDefaultActsFolder() {
        final SQLiteDatabase database = getDataBase();
        if (database == null)
            return;
        boolean insertDefault = true;
        Cursor counter = database.rawQuery("select count(*) from " + TABLE_ACTS_FOLDER, null);
        if (counter != null) {
            try {
                while (counter.moveToNext()) {
                    int count = counter.getInt(0);
                    if (count > 0) {
                        insertDefault = false;
                    }
                }
            } finally {
                counter.close();
            }
        }
        if (insertDefault) {
            ActsFolder folder = new ActsFolder(mContext.getResources().getString(R.string.default_acts_folder_name));
            folder.mIsDefault = true;
            database.insert(TABLE_ACTS_FOLDER, null, folder.toContentValues());
        }
    }

    private void createTableAllActsList() {
        final SQLiteDatabase database = getDataBase();
        if (database == null)
            return;
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ALL_ACTS_LISTS + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + URL + " TEXT,"
                + TITLE + " TEXT,"
                + AMENDED_DATE + " TEXT,"
                + CATEGORY + " TEXT)");
    }

    private void createTableAct() {
        final SQLiteDatabase database = getDataBase();
        if (database == null)
            return;
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ACTS + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NUMBER + " TEXT,"
                + TITLE + " TEXT,"
                + AMENDED_DATE + " TEXT,"
                + HAS_LOAD_SUCCESS + " INTEGER,"
                + CATEGORY + " TEXT)");
    }

    private void createTableChapter() {
        final SQLiteDatabase database = getDataBase();
        if (database == null)
            return;
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHAPTERS + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ACT_ID + " INTEGER,"
                + NUMBER + " TEXT,"
                + COLUMN_ORDER + " INTEGER,"
                + HAS_STAR + " INTEGER,"
                + LINKS + " TEXT,"
                + DRAW_LINE_START + " INTEGER DEFAULT -1,"
                + DRAW_LINE_END + " INTEGER DEFAULT -1,"
                + CONTENT + " TEXT)");
    }

    private void createTableArticle() {
        final SQLiteDatabase database = getDataBase();
        if (database == null)
            return;
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ARTICLES + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CHAPTER_ID + " INTEGER,"
                + TYPE + " TEXT,"
                + NUMBER + " TEXT,"
                + HAS_STAR + " INTEGER,"
                + COLUMN_ORDER + " INTEGER,"
                + LINKS + " TEXT,"
                + DRAW_LINE_START + " INTEGER,"
                + DRAW_LINE_END + " INTEGER,"
                + CONTENT + " TEXT)");
    }

    private void createTableNote() {
        final SQLiteDatabase database = getDataBase();
        if (database == null)
            return;
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTES + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NOTE_TYPE + " INTEGER,"
                + NOTE_PARENT_TYPE + " INTEGER,"
                + NOTE_PARENT_ID + " INTEGER,"
                + NOTE_CONTENT + " TEXT)");
    }
}
