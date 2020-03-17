/**
 * @description: database class
 * 
 */
package com.daohelper.db;

import java.io.File;

import com.daohelper.utils.DaoUtils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

/**
 * open database according to database name
 * @author zhanghao
 *
 */
public class DbFileHelper extends HelperImpl implements IDbFileHelper {

	public static String TAG = "DbFileHelper";

	private Context mContext;
	SQLiteDatabase db = null;
	String dbName;

	public DbFileHelper(Context context, String dbName) {
		mContext = context;
		this.dbName = dbName;
	}

	public String getDBName() {
        return dbName;
    }

    public void setDBName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return getDB(dbName);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return getDB(dbName);
    }

    /**
     * get SQLiteDatabase according to db name
     * @param dbName :
     * slim.db
     * slim_energy.db
     * @return
     */
    public SQLiteDatabase getDB(String dbName) {
        if(db == null || !db.isOpen()) {
            db = open(dbName);
        }
        return db;
    }

    /**
     * open database according to db name
     */
    public SQLiteDatabase open(String dbName) {
        File file = mContext.getDatabasePath(dbName);
        String dbPath = file.getAbsolutePath();
        boolean copied = false;
        if(!file.exists()) {
            copied = DaoUtils.copyAssetsFile(mContext, dbName, dbPath);
            Log.i(TAG, "DB file copied= " + copied + ", dbName:" + dbName + " to dbPath:" + dbPath);
        } else {
            Log.i(TAG, "The existed dbName: " + dbName);
        }
        return openDB(dbPath);
    }
    
    public SQLiteDatabase openDB(String dbPath) {
        if(TextUtils.isEmpty(dbPath)) return null;
        try {
            SQLiteDatabase tmp = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            if(tmp != null) {
                Log.i(TAG, "openDB seccussfully path: " + tmp.getPath());
            } else {
                Log.e(TAG, "openDB failed from path: " + dbPath);
            }
            return tmp;
        } catch (SQLException e) {
            Log.e(TAG, "open database failed, dbPath: " + dbPath + ", \n SQLException: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "open db Exception dbPath= "+dbPath + ", \n " + e);
        }
        return null;
    }
}
