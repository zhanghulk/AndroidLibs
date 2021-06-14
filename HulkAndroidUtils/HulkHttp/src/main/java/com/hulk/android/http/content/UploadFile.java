package com.hulk.android.http.content;

import java.io.File;

public class UploadFile {
    public String fileName = null;
    public String path = null;
    public String contentType = null;

    public UploadFile(String fileName, String path, String contentType) {
        this.fileName = fileName;
        this.path = path;
        this.contentType = contentType;
    }

    public File getFile() {
        return new File(path);
    }
}
