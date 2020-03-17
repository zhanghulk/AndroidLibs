package com.daohelper.db;

import java.util.List;

import com.daohelper.db.pairs.ExeSqlPair;

import android.content.ContentValues;
import android.database.Cursor;

public interface IDaoHelper {

    /**
     * insert data item
     * 
     * @param table
     * @param nullColumnHack (default null) column names are known and an empty row can't be inserted
     * @param values
     * @return the inserted raw ID, -2 if values is null or empty.
     */
    long insert(String table, String nullColumnHack, ContentValues values);
    int delete(String table, String whereClause, String[] whereArgs);
    int deleteAll(String table);
    int update(String table, ContentValues values, String whereClause, String[] whereArgs);
    int update(String table, ContentValues values, String where);
    Cursor query(String table, String[] columns, String wheres);
    Cursor query(String table, String[] columns, String selection, String[] selectionArgs);
    Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String orderBy, String limit);
    Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);
    Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);
    long replace(String table, String nullColumnHack, ContentValues values);
    Cursor rawQuery(String sql, String[] selectionArgs);
    boolean exeSQL(String sql, Object[] bindArgs);
    int getCount(String table, String countColumnName, boolean isDistinct);
    /**
     * Get the version of database
     * @return  version is -1 if the db not exist
     */
    int getVersion();
    void close();
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
    public boolean exeTransactionSQL(String... sqls);
    /**
     * execute multiple sqls: 
     * eg: db.execSQL("INSERT INTO users (name,amount) VALUES(?,?)", new Object[]{person.getName(),person.getAmount()});
     * <p>db.execSQL("INSERT INTO users (name,amount) VALUES(?,?)", new Object[]{person.getName(),person.getAmount()});
     * @param pairs
     * @return
     */
    public int exeTransactionSQL(List<ExeSqlPair> pairs);
}
