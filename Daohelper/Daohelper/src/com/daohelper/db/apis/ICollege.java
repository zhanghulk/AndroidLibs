package com.daohelper.db.apis;

import com.daohelper.db.entry.College;

public interface ICollege extends IFace<College> {

	College getCollege(String collegeId);
}
