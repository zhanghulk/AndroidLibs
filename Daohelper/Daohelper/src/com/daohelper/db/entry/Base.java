package com.daohelper.db.entry;

import java.io.Serializable;

import com.daohelper.factories.DaoConatants.BaseColumns;

/**
 * implements Serializable is in order to pass by intent between two activity
 * 
 * @author hao
 * 
 */
public class Base implements BaseColumns, Serializable, Cloneable {
	private static final long serialVersionUID = 5363516542501504552L;

	protected long id = 0;
	protected int flags = 0;
	protected String remark;

	public Base() {
	}

	public Base(int id, int flags, String remark) {
		super();
		this.id = id;
		this.flags = flags;
		this.remark = remark;
	}

	@Override
	public String toString() {
		return "Base [id=" + id + ", flags=" + flags 
				+ ", remark=" + remark + "]";
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * not need to submit if flags = 0, otherwise need to submit to server
	 * 
	 * @return the flags
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * not need to submit if flags = 0, otherwise need to submit to server
	 * 
	 * @param flags
	 *            the flags to set
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
