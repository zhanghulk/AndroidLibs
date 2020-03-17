
package com.daohelper.db.pairs;

public class SavedResult {

    public long insertedRawID = -1;
    public int updatedCount = -1;

    public SavedResult(long insertedRawID) {
        this.insertedRawID = insertedRawID;
    }

    public SavedResult(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    @Override
    public String toString() {
        return "SavedResult [insertedRawID=" + insertedRawID + ", updatedCount=" + updatedCount
                + "]";
    }
}
