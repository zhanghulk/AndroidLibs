package com.daohelper.db.impls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.daohelper.db.DataChangeManager;
import com.daohelper.db.IDaoHelper;
import com.daohelper.db.apis.IFace;
import com.daohelper.db.entry.Base;
import com.daohelper.db.pairs.ExeSqlPair;
import com.daohelper.db.pairs.SavedResult;
import com.daohelper.factories.DaoConatants.BaseColumns;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

/**
 * database common method class
 * 
 * @author hao
 * 
 * @param <T>
 */
public abstract class CommonImpl<T extends Base> implements IFace<T> {

    private static final String TAG = "CommonImpl";

    protected IDaoHelper helper = null;
    protected String table = null;
    protected boolean debug = false;
    
    /**
     * return cursor columns, return null if not exist
     * 
     * @param db
     * @param table
     */
    public CommonImpl(IDaoHelper helper, String table) {
        this.helper = helper;
        this.table = table;
    }

    protected Cursor query() {
        return query(null);
    }

    protected Cursor query(String where) {
        return query(null, where);
    }

    public Cursor query(String[] columns, String where) {
        return query(columns, where, null, null, null);
    }

    protected Cursor query(String selection, String[] selectionArgs, String orderBy, String limit) {
        return query(null, selection, selectionArgs, null, null, orderBy, limit);
    }

    protected Cursor query(String[] columns, String selection, String[] selectionArgs, String orderBy, String limit) {
        return query(columns, selection, selectionArgs, null, null, orderBy, limit);
    }

    protected Cursor query(String[] columns, String selection, String[] selectionArgs,
             String groupBy, String having, String orderBy, String limit) {
        Cursor cursor = null;
        try {
            cursor = helper.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor == null) {
            logNullCursor(table, columns, selection, selectionArgs, groupBy, orderBy, limit);
        }
        return cursor;
    }

    public synchronized Cursor query(boolean distinct, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        Cursor cursor = helper.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        if (cursor == null) {
            logNullCursor(table, columns, selection, selectionArgs, groupBy, orderBy, limit);
        }
        return cursor;
    }

    protected Cursor queryOne(String selection, String[] selectionArgs, String orderBy, String limit) {
        return query(selection, selectionArgs, null, " 1 ");
    }

    public T get(long id) {
        List<T> list = get(whereID(id));
        T data = null;
        if (list != null && !list.isEmpty()) {
            data = list.get(0);
        }
        return data;
    }

    public List<T> get(String where) {
        Cursor cur = query(where);
        List<T> list = parseCursor(cur);
        if(cur != null && !cur.isClosed()) {
            cur.close();
        }
        return list;
    }

    @Override
    public List<T> get(String selection, String[] selectionArgs) {
        Cursor cur = query(selection, selectionArgs, null, null);
        return parseCursor(cur);
    }

    @Override
    public List<T> getOrderData(String orderBy, String where) {
        return getOrderLimitData(orderBy, null, where);
    }

    public T getOneData(String selection, String[] selectionArgs, String orderBy) {
        Cursor cur = queryOne(selection, selectionArgs, orderBy, " 1 ");
        return parseFirstCursor(cur);
    }

    /**
     * get limit data
     * @param limit 格式 {[offSet], maxNum}, offSet可以省略(取前面), 以0开始.  eg: 4  或者 4, 10
     * @param where
     * @return
     */
    @Override
    public List<T> getLimitData(String limit, String where) {
        return getOrderLimitData(null, limit, where);
    }
    
    @Override
    public List<T> getLimitData(int offSet, int limitNum, String where) {
        return getLimitData(offSet + "," + limitNum, where);
    }

    /**
     * get special list by condition
     * @param orderBy  eg: date desc
     * @param limit 格式 {[offSet], maxNum}, offSet可以省略(取前面), 以0开始.  eg: 4  或者 4, 10
     *  * @param where  eg: date = 135454456765
     * @return List<T>
     */
    @Override
    public List<T> getOrderLimitData(String orderBy, String limit, String where) {
        StringBuffer sqlBuffer = new StringBuffer(" SELECT * FROM " + table);
        if (!TextUtils.isEmpty(where)) {
            sqlBuffer.append(" WHERE " + where);
        }
        if (!TextUtils.isEmpty(orderBy)) {
            sqlBuffer.append(" ORDER BY " + orderBy);
        }
        if (!TextUtils.isEmpty(limit)) {
            sqlBuffer.append(" LIMIT " + limit);
        }
        String sql = sqlBuffer.toString();
        if(debug) Log.d(TAG, "getOrderLimitData sql: " + sql);
        Cursor cur = rawQuery(sql, null);
        if(cur == null) {
            Log.e(TAG, "rawQuery cursor is null, sql= " + sql);
        }
        List<T> list = parseCursor(cur);
        if(cur != null && !cur.isClosed()) {
            cur.close();
        }
        return list;
    }

    public List<T> queryDatas(String sql, String[] selectionArgs) {
        Cursor cur = rawQuery(sql, selectionArgs);
        List<T> list = null;
        if(cur != null) {
            list = parseCursor(cur);
            if(!cur.isClosed()) {
                cur.close();
            }
        }
        return list;
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return helper.rawQuery(sql, selectionArgs);
    }

    @Override
    public void exeSQL(String sql, Object[] bindArgs) {
        helper.exeSQL(sql, bindArgs);
    }

    @Override
    public boolean exeTransactionSQL(String... sqls) {
        return helper.exeTransactionSQL(sqls);
    }

    public T getFirst(String where, String orderBy) {
        List<T> list = getOrderLimitData(orderBy, " 1 ", where);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public List<T> parseCursor(Cursor cursor) {
        final Cursor cur = cursor;
        try {
            if (cur != null && cur.moveToFirst()) {
                List<T> list = new ArrayList<T>();
                do {
                    T item = parseData(cur);
                    if(item != null) {
                        list.add(item);
                    }
                } while (cur.moveToNext());
                if(cur != null) {
                    cur.close();
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cur != null) cur.close();
        }
        return null;
    }
    
    /**
     * parse only first record
     * @param cursor
     * @return
     */
    public T parseFirstCursor(Cursor cursor) {
        final Cursor cur = cursor;
        if (cur != null && cur.moveToFirst()) {
            T it = parseData(cur);
            if(it != null) {
                return it;
            }
        }
        return null;
    }
    
    public List<T> getAll() {
        Cursor cur = query();
        return parseCursor(cur);
    }

    @Override
    public int getCount(String countColumnName, boolean isDistinct) {
        if(TextUtils.isEmpty(countColumnName)) {
            countColumnName = BaseColumns.ID;
        }
        return helper.getCount(table, countColumnName, isDistinct);
    }

    @Override
    public SavedResult save(T data) {
        return save(data, null);
    }
    
    @Override
    public SavedResult save(T data, String updateWhere) {
        if (data == null)
            return null;
        long rowid = has(data);
        if (rowid > 0) {
            StringBuffer sb = new StringBuffer(Base.ID + "=" + rowid);
            if(!TextUtils.isEmpty(updateWhere)) {
                sb.append(" AND " + updateWhere);
            }
            int count = update(data, sb.toString());
            return new SavedResult(count);
        } else {
            long rawID = insert(data);
            return new SavedResult(rawID);
        }
    }
    
    @Override
    public int replace(T data) {
        ContentValues values = getContentValues(data);
        return replace(values);
    }
    
    @Override
    public int replace(ContentValues values) {
        synchronized (helper) {
            int rowId = (int) helper.replace(table, null, values);
            if(rowId <= 0) {
                Log.e(TAG, "REPLACE failed table: " + table + ", values: " + values);
            }
            return rowId;
        }
    }

    public int insert(T data) {
        ContentValues cv = getContentValues(data);
        return insert(cv);
    }

    public synchronized int insert(ContentValues values) {
        if (values == null) {
            Log.e(TAG, "insert failed table: " + table
                    + ", ContentValues is null !");
            return -1;
        }
        int rowid = (int) helper.insert(table, null, values);
        if(rowid > 0) {
            DataChangeManager.notifyInsertOrReplaced(table, values, rowid);
        } else {
            Log.e(TAG, "INSERT failed table: " + table
                    + ", rowid= " + rowid + ", values: " + values);
        }
        return rowid;
    }

    public int update(T data) {
        if(data == null) {
            return -1;
        }
        return update(data, null);
    }

    public int update(T data, String where) {
        int count = 0;
        T obj = getUniqueDatas(where);
        if (obj != null) {
            count = update(getContentValues(data), where);
        } else {
            int rowId = insert(data);
            Log.i(TAG, "update invalid where: " + where +",but insert rowId = " + rowId);
        }
        return count;
    }

    private T getUniqueDatas(String where) {
        return getFirst(where, null);
    }

    public int update(ContentValues values, String where) {
        return update(values, where, null);
    }

    public synchronized int update(ContentValues values, String selection, String[] selectionArgs) {
        if (values == null || values.size() == 0)
            return 0;
        int count = helper.update(table, values, selection, selectionArgs);
        if(count > 0) {
            DataChangeManager.notifyUpdated(table, selection, values, count);
        } else {
            Log.e(TAG, "UPDATE failed table: " + table + ", selection: " + selection
                    + ", selectionArgs= " + Arrays.toString(selectionArgs)
                    + ", values: " + values + ", count= " + count);
        }
        return count;
    }

    @Override
    public int updateField(int id, String fieldName, String value) {
        return updateField(fieldName, value, whereID(id));
    }

    @Override
    public int updateField(String fieldName, String value, String where) {
        return update(getFieldContentValues(null, fieldName, value), where);
    }

    public int delete(int id) {
        String where = whereID(id);
        int count = delete(where);
        return count;
    }

    public int delete(String where) {
        return delete(where, null);
    }

    public synchronized int delete(String where, String[] whereArgs) {
        int count = 0;
        try {
            count = helper.delete(table, where, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "delete table : " + table + ", Exception: " + e);
            e.printStackTrace();
        }
        if(count > 0) {
            DataChangeManager.notifyDeleted(table, where, count);
        } else {
            Log.w(TAG, "delete: table= " + table + ", where: " + where);
        }
        return count;
    }

    public int deleteAll() {
        return delete(null);
    }
    
    public String getCloneWhere(T t) {
        return null;
    }

    /**
     * return its id if the unique record is existed according to id, or return
     * -1 if you want to select db, please override it
     * 
     * @param mIUser
     * @return its id, or return -1
     */
    public long has(T data) {
        if (data == null)
            return -1;
        long id = data.getId();
        if (id < 0)
            return id;
        if (get(id) == null)
            id = -1;
        return id;
    }

    public boolean isIdentical(T t1, T t2) {
        if (t1 == null || t2 == null) {
            return false;
        }
        return t1.getId() == t2.getId();
    }

    /**
     * get ContentValues object, it will return new ContentValues if values is
     * null.
     * 
     * @param values
     *            can null
     * @param fieldName
     * @param value
     * @return
     */
    protected ContentValues getFieldContentValues(ContentValues values,
            String fieldName, String value) {
        if (values == null) {
            values = new ContentValues();
        }
        values.put(fieldName, value);
        return values;
    }
    
    public List<String> getFieldValueList(String fieldName, String where) {
        String sql = "SELECT " + fieldName + " FROM " +  table + " WHERE " + where;
        Cursor cur = rawQuery(sql, null);
        List<String> list = new ArrayList<String>();
        if(cur != null && cur.moveToFirst()) {
            do {
                list.add(cur.getString(0));
            } while (cur.moveToNext());
            cur.close();
        }
        return list;
    }

    protected long getValidTime(long timeMillis) {
        if (timeMillis <= 0) {
            timeMillis = System.currentTimeMillis();
        }
        return timeMillis;
    }
    
    protected String whereID(long id) {
        return BaseColumns.ID + " = " + id;
    }

    protected String whereAndOther(String where, String whereOther) {
        StringBuffer whereSb = new StringBuffer(where);
        if(!TextUtils.isEmpty(whereOther)) {
            whereSb.append(" AND " + whereOther);
        }
        return whereSb.toString();
    }
    
    protected void warnLogIfMulti(List<T> list, String where) {
        if(list != null && list.size() > 0) {
            Log.w(TAG, table + ", multi result:" + list.size() +",where:" + where);
            for (T t : list) {
                Log.w(TAG, "" + t.toString());
            }
        }
    }
    
    @Override
    public T getMaxRecord(String fieldName, String where) {
        return getMaxRecord(fieldName, null, where);
    }

    /**
     * eg  sql: select * from weight where value in (select max(value) from weight where uid=6155934);
     */
    @Override
    public T getMaxRecord(String fieldName, String groupBy, String where) {
        String selectExtremumFunction = " max(" + fieldName +") ";
        return getExtremum(fieldName, selectExtremumFunction, groupBy, where);
    }
    
    @Override
    public T getMinRecord(String fieldName, String where) {
        return getMinRecord(fieldName, null, where);
    }

    /**
     * eg  sql: select * from weight where value in (select max(value) from weight where uid=6155934);
     */
    @Override
    public T getMinRecord(String fieldName, String groupBy, String where) {
        String selectExtremumFunction = " min(" + fieldName +") ";
        return getExtremum(fieldName, selectExtremumFunction, groupBy, where);
    }

    /**
     * 
     * @param selectExtremumFunction:eg " max(" + fieldName + ") ", " min(" + fieldName + ") "
     * @param groupBy
     * @param where
     * @return
     */
    private T getExtremum(String fieldName, String selectExtremumFunction, String groupBy, String where) {
        String sqlBase = "SELECT * FROM " +  table + " WHERE " + fieldName +" IN (SELECT " + selectExtremumFunction +" FROM " +  table;
        StringBuffer sqlBuffer = new StringBuffer(sqlBase);
        if(!TextUtils.isEmpty(where)) {
            sqlBuffer.append(" where " + where);
        }
        if(!TextUtils.isEmpty(groupBy)) {
            sqlBuffer.append(" group by + " + groupBy + " ) group by + " + groupBy + ";");
        } else {
            sqlBuffer.append(");");
        }
        synchronized (helper) {
            String sql = sqlBuffer.toString();
            Cursor cur = rawQuery(sql, null);
            if(cur != null) {
                return parseFirstCursor(cur);
            }
            Log.e(TAG, "getExtremum cursor is null, sql= " + sql);
        }
        return null;
    }
    
    protected int updateData(T originalT, Cursor c) {
        if(c == null) {
            Log.w(TAG, "Cursor NULL, originalT: " + originalT);
        }
        return -1;
    }

    /**
     * execute multiple sql: db.execSQL("INSERT INTO users (name,amount) VALUES(?,?)", new Object[]{person.getName(),person.getAmount()});
     */
    @Override
    public int insertBatch(List<T> list) {
        if(list == null || list.size() == 0) {
            return 0;
        }
        List<ExeSqlPair> pairs = new ArrayList<ExeSqlPair>(); 
        for (T data : list) {
            ContentValues values = getContentValues(data);
            StringBuffer selArgs = new StringBuffer();
            StringBuffer sql = new StringBuffer("INSERT INTO ");
            sql.append(table).append(" ( ");
            List<Object> objs = new ArrayList<Object>();
            for (Entry<String, Object> entry : values.valueSet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                sql.append(key).append(",");
                selArgs.append("?").append(",");//user ?
                objs.add(val);
            }
            if(objs.isEmpty()) {
                continue;
            }
            sql.deleteCharAt(sql.length() - 1);
            //generate bindArgs
            selArgs.deleteCharAt(selArgs.length() - 1);
            sql.append(") VALUES (").append(selArgs).append(")");
            Object[] bindArgs = objs.toArray(new Object[objs.size()]);
            pairs.add(new ExeSqlPair(sql.toString(), bindArgs));
        }
        if(pairs.isEmpty()) {
            Log.w(TAG, "Insert batch data failed for EMPTY data for table: " + table);
            return 0;
        }
        int count = helper.exeTransactionSQL(pairs);
        if(count <= 0) {
            Log.w(TAG, "Insert batch data failed for table: " + table);
        }
        return count;
    }

    public String getString(Cursor c, String columnName) {
        return c.getString(c.getColumnIndex(columnName));
    }

    public int getInt(Cursor c, String columnName) {
        return c.getInt(c.getColumnIndex(columnName));
    }

    public long getLong(Cursor c, String columnName) {
        return c.getLong(c.getColumnIndex(columnName));
    }

    public double getDouble(Cursor c, String columnName) {
        return c.getDouble(c.getColumnIndex(columnName));
    }

    private void logNullCursor(String table, String[] columns, String where, String[] selectionArgs, String groupBy, String orderBy, String limit) {
        Log.w(TAG, "query Cursor is null !" + ", table: " + table
                + ", columns: "+ Arrays.toString(columns) 
                + ", where: " + where + ", selections: "+ Arrays.toString(selectionArgs) 
                + ", groupBy: " + groupBy + ", orderBy: " + orderBy  
                + ", limit: " + limit);
    }
    protected abstract T parseData(Cursor c);

    protected abstract ContentValues getContentValues(T data);
}
