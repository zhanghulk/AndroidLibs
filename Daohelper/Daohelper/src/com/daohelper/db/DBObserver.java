package com.daohelper.db;

import android.content.ContentValues;

public interface DBObserver {
	void onDeleteDb(String table, String where, int deletedCount);

	void onInsertOrReplaceDb(String table, ContentValues values, int rowId);

	void onUpdateDb(String table, String where, ContentValues values, int updatedCount);
}
