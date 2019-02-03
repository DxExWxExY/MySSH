package com.dxexwxexy.sftp.Networking;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dxexwxexy.sftp.Data.Client;
import com.dxexwxexy.sftp.Data.Directory;
import com.dxexwxexy.sftp.Data.File;
import com.dxexwxexy.sftp.Data.FileSystemEntry;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class SFTP extends Thread {

    private Client c;
    private String path;
    private Handler handler;
    private ChannelSftp sftp;
    private UserInfo userInfo;
    private Stack<String> parents;
    private AtomicBoolean response = new AtomicBoolean(false);

    public boolean fetch;
    public ArrayList<FileSystemEntry> list;
    public final Object lock = new Object();

    public SFTP(Client c, Handler handler) {
        this.c = c;
        this.handler = handler;
        userInfo = new UserInfo() {
            @Override
            public String getPassphrase() {
                return c.getPhrase();
            }

            @Override
            public String getPassword() {
                return c.getPass();
            }

            @Override
            public boolean promptPassword(String message) {
                return true;
            }

            @Override
            public boolean promptPassphrase(String message) {
                return true;
            }

            @Override
            public boolean promptYesNo(String message) {
                synchronized (lock) {
                    try {
                        Message msg = new Message();
                        msg.arg1 = 4;
                        msg.obj = message;
                        handler.sendMessage(msg);
                        lock.wait();
                        return response.get();
                    } catch (InterruptedException e) {
                        Log.e("SFTP", e.toString());
                    }
                }
                return false;
            }

            @Override
            public void showMessage(String message) {
                Message m = new Message();
                m.arg1 = 5;
                m.obj = message;
                handler.sendMessage(m);
            }
        };
    }

    @Override
    public void run() {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(c.getUser(), c.getHost(), c.getPort());
            session.setUserInfo(userInfo);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            path = sftp.pwd();
            createHierarchy();
            Message message = new Message();
            message.arg1 = 1;
            message.obj = "Connection Successful!";
            handler.sendMessage(message);
        } catch (JSchException | SftpException e) {
            Log.e("SFTP", e.toString());
            Message m = new Message();
            m.arg1 = 2;
            m.arg2 = 1;
            m.obj = e.getMessage();
            handler.sendMessage(m);
            e.printStackTrace();
        }
    }

    public void renameFile(String o, String n) {
        new Thread(() -> {
            try {
                sftp.cd(path);
                sftp.rename(o, n);
                fetchFiles();
            } catch(SftpException e){

                e.printStackTrace();
            }
        }).start();
    }

    public void deleteFile(String name,int type) {
        new Thread(() -> {
            try {
                sftp.cd(path);
                switch (type) {
                    case 0: //file
                        sftp.rm(name);
                        break;
                    case 1: //dir
                        rmdir(path + "/" + name);
                }
                fetchFiles();
            } catch (SftpException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void rmdir(String path) {
        try {
            Vector<ChannelSftp.LsEntry> v = new Vector<>();
            sftp.cd(path);
            sftp.ls(path, entry -> {
                if (!entry.getFilename().equals("..")
                        && !entry.getFilename().equals("."))
                    v.addElement(entry);
                return 0;
            });
            for (ChannelSftp.LsEntry e : v) {
                if (e.getAttrs().isDir()) {
                    try { // empty dir
                        sftp.rmdir(e.getFilename());
                    } catch (SftpException a) { // non_empty dir
                        rmdir(path + "/" + e.getFilename());
                    }
                } else { //file
                    sftp.rm(e.getFilename());
                }
            }
            sftp.rmdir(path);
        } catch (SftpException e) {
            e.printStackTrace();
        }

    }

    public void fetchFiles() {
        try {
            Vector<ChannelSftp.LsEntry> v = new Vector<>();
            list = new ArrayList<>();
            sftp.ls(path, entry -> {
                if (!entry.getFilename().equals("..")
                        && !entry.getFilename().equals("."))
                    v.addElement(entry);
                return 0;
            });
            for (Object e : v) {
                String[] data = e.toString().split("\\s+");
                if (data[0].matches("[dl].+")) { //dir or link
                    list.add(new Directory(data, path));
                } else { //file
                    list.add(new File(data));
                }
            }
            list.sort(FileSystemEntry.SORT);
            Message m = new Message();
            m.arg1 = 3;
            handler.sendMessage(m);
        } catch (SftpException e) {
            Message m = new Message();
            m.arg1 = 2;
            m.obj = e.getMessage();
            handler.sendMessage(m);
            e.printStackTrace();
        }
    }

    public void mkdir(String name) {
        new Thread(() -> {
            try {
                sftp.cd(path);
                sftp.mkdir(name);
                fetchFiles();
            } catch (SftpException e) {
                Message m = new Message();
                m.arg1 = 2;
                m.obj = "Directory Already Exists";
                handler.sendMessage(m);
                e.printStackTrace();
            }
        }).start();
    }

    public void put(java.io.File file, SftpProgressMonitor monitor) {
        new Thread(() -> {
            try {
                sftp.cd(path);
                sftp.put(file.getAbsolutePath(), file.getName(), monitor);
                fetchFiles();
            } catch (SftpException e) {
                Message m = new Message();
                m.arg1 = 2;
                m.obj = "Error Uploading File";
                handler.sendMessage(m);
                e.printStackTrace();
            }
        }).start();
    }

    public void get(String name, String dir, SftpProgressMonitor monitor) {
        new Thread(() -> {
            try {
                sftp.cd(path);
                sftp.get(name,
                        new FileOutputStream(dir + java.io.File.separator + name),
                        monitor);
                fetchFiles();
            } catch (SftpException | FileNotFoundException e) {
                Message m = new Message();
                m.arg1 = 2;
                m.obj = "Error Downloading File";
                handler.sendMessage(m);
                e.printStackTrace();
            }
        }).start();
    }

    private void createHierarchy() {
        parents = new Stack<>();
        String[] tmp = path.split("/");
        for (String e : tmp) {
            if (parents.isEmpty()) {
                parents.push("/");
            } else if (parents.peek().equals("/")){
                parents.push("/" + e);
            } else {
                parents.push(parents.peek() + "/" + e);
            }
        }
        parents.pop();
    }

    public void setResponse(boolean response) {
        this.response.set(response);
    }

    public void close() {
        if (sftp != null) {
            sftp.quit();
            fetch = false;
            path = null;
            parents = null;
        }
    }

    public String getInfo() {
        return c.getUser()+"@"+c.getHost();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String popParentDir() {
        return parents.pop();
    }

    public void pushParentDir(String parent) {
        parents.push(parent);
    }

    public boolean hasParentDir() {
        return !parents.isEmpty();
    }
}
