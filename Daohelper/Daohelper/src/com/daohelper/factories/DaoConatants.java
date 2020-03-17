package com.daohelper.factories;

public class DaoConatants {

	public static final String DROP_TAB_PRDFIX = "DROP TABLE IF EXISTS ";
	public static final String CREATE_TAB_PRDFIX = "CREATE TABLE IF NOT EXISTS ";

	public static interface BaseColumns {
		//common field:
		public final static String ID = "id";
		public final static String FLAGS = "flags";
		public final static String REMARK = "remark";
	}

	public static interface CollegeColumns extends BaseColumns {
		public final static String TAB_NAME = "colleges";

		//field:
		public final static String COLLEGE_ID = "college_id";
		public static final String COLLEGE_NAME = "college_name";
		
		public static final String SQL_DROP_TAB = DROP_TAB_PRDFIX + TAB_NAME;
		public static final String SQL_CREATE_TAB = CREATE_TAB_PRDFIX + TAB_NAME +
				"( id INTEGER PRIMARY KEY," +
				" flags INTEGER  DEFAULT 0," +
				" college_id TEXT," +
				" college_name TEXT," +
				" remark TEXT)";
	}

	/**
	 * Note: The city database is constant, can not be create and upgrade.
	 * <p> The city.db file is saved in accets folder, and it will copied to databases folder to use.
	 * @author hulk
	 *
	 */
	public static interface AreaColumns extends BaseColumns {
	    public final static String DB_NAME = "area.db";
        public final static String TAB_NAME = "area";
        /** city name*/
        public final static String NAME = "name";
        /** pin yin index*/
        public final static String IDX_CHAR = "idx_char";
        /** The parent id of city or area */
        public final static String PID = "pid";
        public static final String ORDER_DEF = IDX_CHAR + " DESC ";
    }
}
