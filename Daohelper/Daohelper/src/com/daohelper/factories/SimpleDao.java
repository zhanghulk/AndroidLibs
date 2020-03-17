
package com.daohelper.factories;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.daohelper.db.DaoHelper;
import com.daohelper.db.IDaoHelper;
import com.daohelper.db.apis.IArea;
import com.daohelper.db.apis.ICollege;
import com.daohelper.db.entry.Area;
import com.daohelper.db.entry.College;
import com.daohelper.db.impls.AreaDao;
import com.daohelper.db.impls.CollegeDao;
import com.daohelper.factories.DaoFactory.DaoHelperCallback;
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
public class SimpleDao {

    private static final String ARAEA_DBNAME = "area.db";
    private static final String QUYOU_DBNAME = "college.db";
    private static final int QUYOU_VERSION = 1;//must > 0
    // tables dao:
    static IArea sAreaDao;
    static ICollege sCollegeDao;
    static DaoHelper sQuyouHelper;

    public static ICollege getCollegeDao(Context context) {
        if (sCollegeDao == null) {
            sCollegeDao = new CollegeDao(getQuyouHelper(context), College.TAB_NAME);
        }
        return sCollegeDao;
    }

    public static IArea getAreaDao(Context context) {
        if (sAreaDao == null) {
            sAreaDao = new AreaDao(DaoFactory.getDbFileHelper(context, ARAEA_DBNAME), Area.TAB_NAME);
        }
        return sAreaDao;
    }

    public static IDaoHelper getQuyouHelper(Context  context) {
        if(sQuyouHelper == null) {
            sQuyouHelper = createQuyouDaoHelper(context);
        }
        return sQuyouHelper;
    }

    public static DaoHelper createQuyouDaoHelper(Context context) {
        return DaoFactory.createDaoHelper(context, true, new DaoHelperCallback() {
            
            @Override
            public void onCreate(SQLiteDatabase db) {
                createTables(db);
            }
            
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL(College.SQL_DROP_TAB);
                createTables(db);
            }
            
            @Override
            public int getDbVersion() {
                return QUYOU_VERSION;
            }
            
            @Override
            public String getDbName() {
                return QUYOU_DBNAME;
            }

            public void createTables(SQLiteDatabase db) {
                //db.execSQL(College.SQL_CREATE_TAB);
            }
        });
    }
}
