
package com.daohelper.db.entry;

import com.daohelper.factories.DaoConatants.AreaColumns;

public class Area extends Base implements AreaColumns {
    private static final long serialVersionUID = 3087241404642127411L;
    /**the first level: the pids of province is 0*/
    public final static int TYPE_PROVINCE = 0;
    /**the second level*/
    public final static int TYPE_CITY = 0;
    /**the last level*/
    public final static int TYPE_DISTRICT = 0;

    private String name;
    private String idx_char;//the index of name

    //the parent id, the pid of province is 0.
    private int pid;
    /**
     * reference to TYPE_*
     */
    private int type = 0;

    public Area() {
    }

    public Area(int type) {
        this.type = type;
    }

    public Area(int id, String name, String idx_char, int pid) {
        this.id = id;
        this.name = name;
        this.idx_char = idx_char;
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdxChar() {
        return idx_char;
    }

    public void setIdxChar(String idx_char) {
        this.idx_char = idx_char;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getType() {
        return type;
    }

    /**
     * @param type reference to TYPE_*
     */
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Area [name=" + name + ", indx_char=" + idx_char + ", pid="
                + pid + ", type=" + type + "]";
    }

    @Override
    protected Area clone() {
    	try {
			return (Area) super.clone();
		} catch (CloneNotSupportedException e) {
			System.err.println("Area clone failed:" + e);
			e.printStackTrace();
		}
		return null;
    }
}
