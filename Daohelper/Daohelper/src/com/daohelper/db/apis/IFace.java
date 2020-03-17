package com.daohelper.db.apis;

import java.util.List;

import com.daohelper.db.pairs.SavedResult;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * 
 * @author Hulk
 * 
 * @param <T>
 */
public interface IFace<T> {
	T get(long id);

	List<T> get(String where);

	List<T> get(String selection, String[] selectionArgs);
	
	List<T> getAll();

	Cursor query(String[] columns, String where);

	int getCount(String countColumnName, boolean isDistinct);

	List<T> parseCursor(Cursor cur) throws Exception;

	List<T> getOrderData(String orderBy, String where);

	/**
	 * get special list by condition
	 * 
	 * @param where
	 *            eg: date = 135454456765
	 * @param orderBy
	 *            eg: date desc
	 * @param limit
	 *            格式 {[offSet], maxNum}, offSet可以省略(取前面), 以0开始. eg: 4 或者 4, 10
	 * @return List<T>
	 */
	List<T> getOrderLimitData(String orderBy, String limit, String where);

	/**
	 * get limit data
	 * 
	 * @param limit
	 *            格式 {[offSet], maxNum}, offSet可以省略(取前面), 以0开始. eg: 4 或者 4, 10
	 * @param where
	 * @return
	 */
	List<T> getLimitData(String limit, String where);

	List<T> getLimitData(int offSet, int limitNum, String where);
	
	List<String> getFieldValueList(String fieldName, String where);

	T getFirst(String where, String orderBy);

	/**
	 * insert or update record and return rowid
	 * 
	 * @param data
	 * @return the result with inserted rowid and update count
	 */
	SavedResult save(T data);

	/**
	 * insert or update record and return rowid
	 * 
	 * @param data
	 * @param updateWhere  if update case
	 * @return the result with inserted rowid and update count
	 */
	SavedResult save(T data, String updateWhere);

	/**
	 * insert or update record and return rowid
	 * @param data
	 * @return
	 */
	int replace(T data);
	
	int replace(ContentValues values);

	int insert(T data);

	int insert(ContentValues cv);

	int update(T data);

	int update(T data, String where);

	int update(ContentValues values, String where);

	int update(ContentValues values, String selection, String[] selectionArgs);

	int updateField(String fieldName, String value, String where);

	int updateField(int id, String fieldName, String value);

	/**
	 * return its id if the unique record is existed according to id, or return
	 * -1 if you want to select db, please override it
	 * 
	 * @param mIUser
	 * @return its id, or return -1
	 */
	long has(T data);

	int delete(int id);

	int delete(String where);

	int delete(String where, String[] whereArgs);

	int deleteAll();

	boolean isIdentical(T t1, T t2);

	T getMaxRecord(String fieldName, String where);

	/**
	 * eg.: select * from weight where value in (select min(value) from weight
	 * where uid=6155934);
	 * 
	 * @param fieldName
	 * @param groupBy
	 * @param where
	 * @return
	 */
	T getMaxRecord(String fieldName, String groupBy, String where);
	
	T getMinRecord(String fieldName, String where);

	/**
	 * eg.: select * from weight where value in (select min(value) from weight
	 * where uid=6155934);
	 * 
	 * @param fieldName
	 * @param groupBy
	 * @param where
	 * @return
	 */
	T getMinRecord(String fieldName, String groupBy, String where);

	Cursor rawQuery(String sql, String[] selectionArgs);

	void exeSQL(String sql, Object[] bindArgs);

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
	boolean exeTransactionSQL(String... sqls);

	/**
	 * User sqliteDatabase transaction to control inserting batch data.
	 * <p>eg: db.execSQL("INSERT INTO users (name,amount) VALUES(?,?)", new Object[]{person.getName(),person.getAmount()});
     * <p>db.execSQL("INSERT INTO users (name,amount) VALUES(?,?)", new Object[]{person.getName(),person.getAmount()});
	 * @param list
	 * @return
	 */
	int insertBatch(List<T> list);
}
