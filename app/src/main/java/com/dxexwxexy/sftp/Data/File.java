package com.dxexwxexy.sftp.Data;

import android.support.annotation.NonNull;

import java.util.Optional;

public class File extends FileSystemEntry {

    private String type;

    public File(String[] data) {
        super(data);
        this.type = Optional.ofNullable(getName())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(getName().lastIndexOf(".") + 1))
                .toString();
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " " + type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
