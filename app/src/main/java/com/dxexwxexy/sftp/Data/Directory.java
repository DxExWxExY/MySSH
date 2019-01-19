package com.dxexwxexy.sftp.Data;

import android.support.annotation.NonNull;

public class Directory extends FileSystemEntry {

    private String path;

    public Directory(String[] data, String path) {
        super(data);
        this.path = path + "/" + getName();
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " " + path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
