package com.dxexwxexy.sftp.Data;

import android.support.annotation.NonNull;

import java.util.Comparator;

public abstract class FileSystemEntry implements Comparable {

    private String permissions;
    private String links;
    private String owner;
    private String group;
    private String size;
    private String date;
    private String name;
    private int weigh;

    public static final Comparator<FileSystemEntry> SORT = FileSystemEntry::compareTo;

    FileSystemEntry(String[] data) {
        this.permissions = data[0];
        this.links = data[1];
        this.owner = data[2];
        this.group = data[3];
        this.size = data[4];
        this.date = String.format("%s %s %s", data[5], data[6], data[7]);
        this.name = data[8];
        this.weigh = this.name.hashCode();
        if (data[0].matches("[dl].+")) { //dir or link
            if (this.weigh > 0) {
                this.weigh *= -1;
            }
        } else {
            if (this.weigh < 0) {
                this.weigh *= -1;
            }
        }
    }

    @NonNull
    public String toString() {
        return String.format("%s %s %s %s %s %s %s",
                permissions, links, owner, group, size, date, name);
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.weigh, ((FileSystemEntry) o).weigh);
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
