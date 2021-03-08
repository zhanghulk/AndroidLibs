package com.hulk.android.report;

import java.io.File;
import java.util.Comparator;

final class LastModifiedComparator implements Comparator<File> {
    @Override
    public int compare(File lhs, File rhs) {
        long l = lhs.lastModified();
        long r = rhs.lastModified();
        return l < r ? -1 : (l == r ? 0 : 1);
    }
}