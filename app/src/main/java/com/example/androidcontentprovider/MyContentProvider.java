package com.example.androidcontentprovider;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URI;
import java.util.HashMap;

public class MyContentProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.example.androidcontentprovider.MyContentProvider";
    static  final String URL  = "content://" + PROVIDER_NAME+"/students";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String _ID  = "_id";
    static final String _Student = "name";
    static final String _School = "school";

    private static HashMap<String, String> Student_Data;

    static  final int students = 1;
    static final int Student_id = 2;

    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "students", students);
        uriMatcher.addURI(PROVIDER_NAME,"students/#",Student_id);
    }

    /* Database Specific Constants Declaration */

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "College";
    static final String STUDENTS_TABLE_NAME = "students";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE = "CREATE TABLE " + STUDENTS_TABLE_NAME +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
            "name TEXT NOT NULL," +
            "school TEXT NOT NULL);";

    /* Helper class */

    private  static class DatabaseHelper extends SQLiteOpenHelper{
        DatabaseHelper(Context context){
            super(context,DATABASE_NAME,null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + STUDENTS_TABLE_NAME);
            onCreate(db);

        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        db = dbHelper.getWritableDatabase();

        return (db == null)? false:true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(STUDENTS_TABLE_NAME);

        switch (uriMatcher.match(uri)){
            case students:
                qb.setProjectionMap(Student_Data);
                break;
            case Student_id:
                qb.appendWhere(_ID +"="+uri.getPathSegments().get(1));
                break;
            default:
        }

        if(sortOrder == null || sortOrder == ""){

            /* By default will sort on Student Names*/

            sortOrder = _Student;
        }

        Cursor c = qb.query(db,projection, selection,selectionArgs,null, null,sortOrder);

        /* registering watch on Content URI for Changes */

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)){
            case students:
                return "vnd.android.cursor.dir/vnd.example.students";
            case Student_id:
                return "vnd.android.cursor.item/vnd.example.students";
            default:
                throw new IllegalArgumentException("Unsupported URI:"+uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        long rowID = db.insert(STUDENTS_TABLE_NAME, "", values);

        /* if record is added successfully */
        if(rowID > 0){
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri,null);
            return  _uri;
        }
        throw new SQLException("Failed to add Data"+ uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case students:
                count = db.delete(STUDENTS_TABLE_NAME, selection, selectionArgs);
                break;

            case Student_id:
                String id = uri.getPathSegments().get(1);
                count = db.delete(STUDENTS_TABLE_NAME,
                        _ID + "="+id+(!TextUtils.isEmpty(selection)?"And ("+selection+')':""),selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case students:
                count = db.update(STUDENTS_TABLE_NAME, values, selection,selectionArgs);
                break;
            case Student_id:
                db.update(STUDENTS_TABLE_NAME, values,
                        _ID + "=" + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? "AND(" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
