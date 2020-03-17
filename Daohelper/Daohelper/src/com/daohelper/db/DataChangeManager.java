package com.daohelper.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;

/**
 * 
 * @author hao
 *
 */
public class DataChangeManager {
	static List<DBObserver> sObservers = new ArrayList<DBObserver>();

	public static void addObserver(DBObserver observer) {
		sObservers.add(observer);
	}

	public static void removeObserver(DBObserver observer) {
		sObservers.add(observer);
	}

	public static void notifyDeleted(String table, String where, int deletedCount) {
		for (DBObserver ob : sObservers) {
			ob.onDeleteDb(table, where, deletedCount);
		}
	}

	public static void notifyInsertOrReplaced(String table, ContentValues values, int rowId) {
		for (DBObserver ob : sObservers) {
			ob.onInsertOrReplaceDb(table, values, rowId);
		}
	}

	public static void notifyUpdated(String table, String where, ContentValues values, int updatedCount) {
		for (DBObserver ob : sObservers) {
			ob.onUpdateDb(table, where, values, updatedCount);
		}
	}
}
