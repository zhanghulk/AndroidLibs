package com.daohelper.db;

import android.database.sqlite.SQLiteDatabase;

public interface IDbFileHelper extends IDaoHelper {

    /**
     * This method is used to GET a existed database file in accsets folder mainly.
     * @param dbName
     * @return the SQLiteDatabase object if existed.
     */
    SQLiteDatabase getDB(String dbName);
    /**
     * This method is used to OPEN a existed database file in accsets folder mainly.
     * @param dbName  db file simple name
     * @return the SQLiteDatabase object if existed.
     */
    SQLiteDatabase open(String dbName);
    /**
     * This method is used to OPEN a existed database file in accsets folder mainly.
     * @param dbPath  the db file path
     * @return the SQLiteDatabase object if existed.
     */
    SQLiteDatabase openDB(String dbPath);
}
