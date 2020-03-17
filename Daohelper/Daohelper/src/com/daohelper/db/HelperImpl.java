package com.daohelper.db;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.daohelper.db.pairs.ExeSqlPair;

public abstract class HelperImpl implements IDaoHelper {
    
    private static final String TAG = null;
    Lock mLock = new ReentrantLock();
    /**This SQLiteDatabase is used to OPEN a existed database file in accsets folder mainly.*/
    SQLiteDatabase fileDb = null;

    /**
     * insert data item
     * 
     * @param table
     * @param nullColumnHack (default null) column names are known and an empty row can't be inserted
     * @param values
     * @return the inserted raw ID, -2 if values is null or empty.
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {
        if (values == null || values.size() == 0) {
            Log.w(TAG, "INSERT failed for ContentValues is null int table= " + table);
            return -2;
        }
        mLock.lock();
        try {
            SQLiteDatabase db = getWritableDatabase();
            return db.insert(table, nullColumnHack, values);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * delete data
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        mLock.lock();
        try {
            SQLiteDatabase db = getWritableDatabase();
            return db.delete(table, whereClause, whereArgs);
        } finally {
            mLock.unlock();
        }
    }

    public int deleteAll(String table) {
        return delete(table, null, null);
    }

    public int update(String table, ContentValues values, String whereClause,
            String[] whereArgs) {
        if (values == null || values.size() == 0) {
           Log.w(TAG, "UPDATE failed for ContentValues is null in table= " + table + ",whereClause:" + whereClause);
            return -2;
        }
        try {
            mLock.lock();
            SQLiteDatabase db = getWritableDatabase();
            return db.update(table, values, whereClause, whereArgs);
        } finally {
            mLock.unlock();
        }
    }

    public int update(String table, ContentValues values, String where) {
        return update(table, values, where, null);
    }

    public Cursor query(String table, String[] columns, String wheres) {
        return query(table, columns, wheres, null);
    }

    public Cursor query(String table, String[] columns, String selection,
            String[] selectionArgs) {
        return query(table, columns, selection, selectionArgs, null, null);
    }

    public Cursor query(String table, String[] columns, String selection,
            String[] selectionArgs, String orderBy, String limit) {
        return query(table, columns, selection, selectionArgs, null, null,
                orderBy, limit);
    }

    public Cursor query(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having,
            String orderBy, String limit) {
        mLock.lock();
        try {
            SQLiteDatabase db = getReadableDatabase();
            return db.query(table, columns, selection, selectionArgs, groupBy,
                    having, orderBy, limit);
        } finally {
            mLock.unlock();
        }
    }

    public Cursor query(boolean distinct, String table, String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit) {
        mLock.lock();
        try {
            SQLiteDatabase db = getReadableDatabase();
            return db.query(distinct, table, columns, selection, selectionArgs,
                    groupBy, having, orderBy, limit);
        } finally {
            mLock.unlock();
        }
    }

    public long replace(String table, String nullColumnHack, ContentValues values) {
        mLock.lock();
        try {
            SQLiteDatabase db = getReadableDatabase();
            db.replace(table, nullColumnHack, values);
            return db.replace(table, nullColumnHack, values);
        } finally {
            mLock.unlock();
        }
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        mLock.lock();
        try {
            SQLiteDatabase db = getReadableDatabase();
            return db.rawQuery(sql, selectionArgs);
        } finally {
            mLock.unlock();
        }
    }

    public boolean exeSQL(String sql, Object[] bindArgs) {
        try {
            mLock.lock();
            SQLiteDatabase db = getReadableDatabase();
            db.execSQL(sql, bindArgs);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
        return false;
    }

    /**
     * Execute sql:
     * SQLiteDatabase db = getReadableDatabase();
     <p>   db.beginTransaction();
     <p>   try {
     <p>       for (String sql : sqls) {
     <p>           db.execSQL(sql);
     <p>       }
     <p>       db.setTransactionSuccessful();
     <p>       return true;
     <p>   } catch (Exception e) {
     <p>       Log.e(TAG, "exeSQLTransaction Exception: " + e);
     <p>       e.printStackTrace();
     <p>   } finally {
     <p>       db.endTransaction();
     <p>   }
     * @param sqls
     * @return true if successfully, or false
     */
    public boolean exeTransactionSQL(String... sqls) {
        if(sqls == null || sqls.length == 0) {
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        try {
            for (String sql : sqls) {
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "exeSQLTransaction Exception: " + e);
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return false;
    }

    /**
     * execute multiple sqls: 
     * eg: db.execSQL("INSERT INTO users (name,amount) VALUES(?,?)", new Object[]{person.getName(),person.getAmount()});
     * <p>db.execSQL("INSERT INTO users (name,amount) VALUES(?,?)", new Object[]{person.getName(),person.getAmount()});
     * @param pairs
     * @return
     */
    public int exeTransactionSQL(List<ExeSqlPair> pairs) {
        if(pairs == null || pairs.size() == 0) {
            return 0;
        }
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        int count = 0;
        try {
            for (ExeSqlPair pair : pairs) {
                db.execSQL(pair.sql, pair.bindArgs);
                count++;
            }
            db.setTransactionSuccessful();
            return count;
        } catch (Exception e) {
            Log.e(TAG, "exeSQLTransaction Exception: " + e);
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return count;
    }

    public int getCount(String table, String countColumnName, boolean isDistinct) {
        String sql = null;
        if (isDistinct) {
            sql = "SELECT COUNT(DISTINC " + countColumnName + ") FROM " + table;
        } else {
            sql = "SELECT COUNT(" + countColumnName + ") FROM " + table;
        }
        mLock.lock();
        Cursor cur = null;
        try {
            cur = rawQuery(sql , null);
            if(cur != null && cur.moveToFirst()) {
                return cur.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cur != null) {
                cur.close();
            }
            mLock.unlock();
        }
        return 0;
    }

    public void close() {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null && db.isOpen()) {
            try {
                mLock.lock();
                db.close();
            } finally {
                mLock.unlock();
            }
        }
    }

    /**
     * Get the version of database
     * @return  version is -1 if the db not exist
     */
    @Override
    public int getVersion() {
        SQLiteDatabase db = getReadableDatabase();
        int version = -1;
        try {
            if(db != null) {
                version = db.getVersion();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(db != null) {
                db.close();
            }
        }
        return version;
    }

    public abstract SQLiteDatabase getWritableDatabase();

    public abstract SQLiteDatabase getReadableDatabase();
}
