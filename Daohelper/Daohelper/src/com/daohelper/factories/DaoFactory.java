package com.daohelper.factories;

import com.daohelper.db.DaoHelper;
import com.daohelper.db.DbFileHelper;
import com.daohelper.db.IDbFileHelper;
import com.daohelper.db.apis.ICollege;
import com.daohelper.db.entry.College;
import com.daohelper.db.impls.CollegeDao;
import com.daohelper.db.impls.CommonImpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 数据库信息初始化工厂类：
 * <p>基于Dao模式和泛型实现:
 * <p>每一个数据库实现自己的DaoHelper，如 {@link QuDaoHelperImpl}的实现方式;
 * <p>每一个表构建自己的Dao：{@link UserDao}和{@link CollegeDao}，都继承于{@link CommonImpl},
 * 同时分别实现自己的接口，如： {@link ICollege} 和 {@link IUser}
 * <p>最好使用单例模式
 * @author hao/hulk
 *
 */
public class DaoFactory {

	private static final String TAG = "DaoFactory";

	static IDbFileHelper sDbFileHelper;
	static DaoHelper sDaoHelper;

	public static IDbFileHelper getDbFileHelper(Context context, String dbFileName) {
		if(sDbFileHelper == null) {
		    sDbFileHelper = createDbFileHelper(context, dbFileName);
		}
		return sDbFileHelper;
	}

	public static IDbFileHelper createDbFileHelper(Context context, String dnFileName) {
        return new AreaDaoHelperImpl(context, dnFileName);
    }

	public static DaoHelper getsDaoHelper(Context context, DaoHelperCallback callback) {
	    if(sDaoHelper == null) {
	        sDaoHelper = new DaoHelperImpl(context, callback);
	    }
        return sDaoHelper;
    }

	public static DaoHelper createDaoHelper(Context context, DaoHelperCallback callback) {
        return new DaoHelperImpl(context, callback);
    }

	/**
	 * create a Daohelper
	 * @param context
	 * @param isCreateCollogeTable to create college table for testing database if true, default false.
	 * @param callback
	 * @return
	 */
	public static DaoHelper createDaoHelper(Context context, boolean isCreateCollogeTable, DaoHelperCallback callback) {
        return new DaoHelperImpl(context, isCreateCollogeTable, callback);
    }

	public static void close() {
		if(sDbFileHelper != null) {
		    sDbFileHelper.close();
		}
	}

	private static class AreaDaoHelperImpl extends DbFileHelper {
        public AreaDaoHelperImpl(Context context, String dbFileName) {
            super(context, dbFileName);
        }
	}

	private static void log(String msg) {
		Log.i(TAG, msg);
	}

	private static class DaoHelperImpl extends DaoHelper {

	    boolean isCreateCollogeTable = false;
        DaoHelperCallback mCallback;

        public DaoHelperImpl(Context context, DaoHelperCallback callback) {
            super(context.getApplicationContext(), callback.getDbName(), callback.getDbVersion());
            mCallback = callback;
        }

        public DaoHelperImpl(Context context, boolean isCreateCollogeTable, DaoHelperCallback callback) {
            super(context.getApplicationContext(), callback.getDbName(), callback.getDbVersion());
            mCallback = callback;
            this.isCreateCollogeTable = isCreateCollogeTable;
        }

        @Override
        public void onDBCreate(SQLiteDatabase db) {
            log("onCreate database path: " + db.getPath() + ", version: " + db.getVersion() + ",isCreateCollogeTable=" + isCreateCollogeTable);
            mCallback.onCreate(db);
            if(isCreateCollogeTable) {
                db.execSQL(College.SQL_CREATE_TAB);
            }
        }

        @Override
        public void onDBUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            log("onCreate database path: " + db.getPath()
                    + ", oldVersion: " + oldVersion + ",newVersion=" + newVersion);
            //to recreate tables
            mCallback.onUpgrade(db, oldVersion, newVersion);
        }
    }

	public interface DaoHelperCallback {
	    String getDbName();
	    int getDbVersion();
	    void onCreate(SQLiteDatabase db);
	    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
	}
}
