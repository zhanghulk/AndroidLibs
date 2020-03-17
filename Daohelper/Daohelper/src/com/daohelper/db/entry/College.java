package com.daohelper.db.entry;

import com.daohelper.factories.DaoConatants.CollegeColumns;

public class College extends Base implements CollegeColumns {
    private static final long serialVersionUID = -2453158180001199811L;
    String collegeId;//college ID in server
	String collegeName;

	public College() {
    }

	public College(String collegeId, String collegeName) {
        this.collegeId = collegeId;
        this.collegeName = collegeName;
    }

    public String getCollegeId() {
		return collegeId;
	}

	public void setCollegeId(String collegeId) {
		this.collegeId = collegeId;
	}

	public String getCollegeName() {
		return collegeName;
	}

	public void setCollegeName(String collegeName) {
		this.collegeName = collegeName;
	}

	@Override
	public College clone() {
		try {
			return (College) super.clone();
		} catch (CloneNotSupportedException e) {
			System.err.println("College clone failed:" + e);
			e.printStackTrace();
		}
		return null;
	}
}
