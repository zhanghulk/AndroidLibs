package com.daohelper.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * database create and update helper class
 * @author hao
 *
 */
public abstract class DaoHelper extends HelperImpl {
	private static final String TAG = "DaoHelper";

	private MyOpenHelper mHelper;
	public DaoHelper(Context context, String dbName, int version) {
		this(context, dbName, null, version);
	}

	public DaoHelper(Context context, String dbName, CursorFactory factory, int version) {
		mHelper = new MyOpenHelper(context, dbName, factory, version);
	}

	@Override
	public SQLiteDatabase getWritableDatabase() {
	    return mHelper.getWritableDatabase();
	}

	@Override
	public SQLiteDatabase getReadableDatabase() {
	    return mHelper.getReadableDatabase();
	}

	public abstract void onDBCreate(SQLiteDatabase db);
    public abstract void onDBUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

	class MyOpenHelper extends SQLiteOpenHelper {

        public MyOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "MyOpenHelper.onCreate db file:" + db.getPath());
            onDBCreate(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "MyOpenHelper.onUpgrade db file:" + db.getPath());
            onDBUpgrade(db, oldVersion, newVersion);
        }
	}
}