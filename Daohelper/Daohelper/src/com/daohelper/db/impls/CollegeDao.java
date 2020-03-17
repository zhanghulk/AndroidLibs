package com.daohelper.db.impls;

import android.content.ContentValues;
import android.database.Cursor;

import com.daohelper.db.IDaoHelper;
import com.daohelper.db.apis.ICollege;
import com.daohelper.db.entry.College;
import com.daohelper.factories.DaoConatants.CollegeColumns;

public class CollegeDao extends CommonImpl<College> implements ICollege, CollegeColumns {

	public CollegeDao(IDaoHelper helper, String table) {
		super(helper, table);
	}

	@Override
	public College getCollege(String collegeId) {
		return getOneData(College.COLLEGE_ID + "= ? ", new String[]{collegeId}, null);
	}

	@Override
	protected College parseData(Cursor c) {
		College data = new College();
		data.setId(getLong(c, ID));
		data.setFlags(getInt(c, FLAGS));
		data.setCollegeId(getString(c, COLLEGE_ID));
		data.setCollegeName(getString(c, COLLEGE_NAME));
		data.setRemark(getString(c, REMARK));
		return data;
	}

	@Override
	protected ContentValues getContentValues(College data) {
		ContentValues cv = new ContentValues();
		cv.put(College.FLAGS, data.getFlags());
		cv.put(College.COLLEGE_ID, data.getCollegeId());
		cv.put(College.COLLEGE_NAME, data.getCollegeName());
		cv.put(College.REMARK, data.getRemark());
		return cv;
	}
}
