package com.taboozle.common;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.taboozle.common.Pack.Cards;

/**
 * Provides access to a database of cards. Each card has a title and the words
 * the user cannot say when trying to describe the word.
 */
public class PackProvider extends ContentProvider {

    private static final String TAG = "PackProvider";

    private static final String DATABASE_NAME = "cards.db";
    private static final int DATABASE_VERSION = 2;
    private static final String CARDS_TABLE_NAME = "cards";

    private static final int CARDS = 1;
    private static final int CARD_ID = 2;
    private static HashMap<String, String> sCardsProjectionMap;
    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + CARDS_TABLE_NAME + " ("
                    + Cards._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Cards.TITLE + " TEXT,"
                    + Cards.BAD_WORDS + " TEXT,"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CARDS_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case CARDS:
            qb.setProjectionMap(sCardsProjectionMap);
            break;

        case CARD_ID:
            qb.setProjectionMap(sCardsProjectionMap);
            qb.appendWhere(Cards._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, null);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
          case CARDS:
            return Cards.CONTENT_TYPE;
          case CARD_ID:
            return Cards.CONTENT_ITEM_TYPE;
          default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != CARDS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        if (values.containsKey(Pack.Cards.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(Pack.Cards.TITLE, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(Pack.Cards.BAD_WORDS) == false) {
            values.put(Pack.Cards.BAD_WORDS, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(CARDS_TABLE_NAME, Cards.BAD_WORDS, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Pack.Cards.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case CARDS:
            count = db.delete(CARDS_TABLE_NAME, where, whereArgs);
            break;

        case CARD_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(CARDS_TABLE_NAME, Cards._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case CARDS:
            count = db.update(CARDS_TABLE_NAME, values, where, whereArgs);
            break;

        case CARD_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(CARDS_TABLE_NAME, values, Cards._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Pack.AUTHORITY, "cards", CARDS);
        sUriMatcher.addURI(Pack.AUTHORITY, "cards/#", CARD_ID);

        sCardsProjectionMap = new HashMap<String, String>();
        sCardsProjectionMap.put(Cards._ID, Cards._ID);
        sCardsProjectionMap.put(Cards.TITLE, Cards.TITLE);
        sCardsProjectionMap.put(Cards.BAD_WORDS, Cards.BAD_WORDS);
    }
}