
package com.daohelper.db.pairs;

import java.util.Arrays;

public class ExeSqlPair {
    public String sql;
    public Object[] bindArgs;

    public ExeSqlPair(String sql, Object[] bindArgs) {
        this.sql = sql;
        this.bindArgs = bindArgs;
    }

    @Override
    public String toString() {
        return "ExeSqlPair [sql=" + sql + ", bindArgs=" + Arrays.toString(bindArgs) + "]";
    }

}
