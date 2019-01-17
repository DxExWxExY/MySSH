package com.dxexwxexy.myssh.Testing;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dxexwxexy.myssh.Data.Client;
import com.dxexwxexy.myssh.Data.Directory;
import com.dxexwxexy.myssh.Data.File;
import com.dxexwxexy.myssh.Data.FileSystemEntry;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class SFTP extends Thread {

    private Client c;
    private Handler handler;
    private UserInfo userInfo;
    private ChannelSftp sftp;
    private AtomicBoolean response = new AtomicBoolean(false);

    public static boolean fetch;
    public static String path;
    public static Stack<String> hierarchy;
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
            ((FilesViewer) context).toast(e.toString(), 1);
            ((FilesViewer) context).finish();
        }
    }

    public void renameFile(String o, String n) {
        new Thread(() -> {
            try {
                sftp.cd(path);
                sftp.rename(o, n);
                getFiles();
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
                        sftp.rmdir(name);
                }
                getFiles();
            } catch (SftpException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createHierarchy() {
        hierarchy = new Stack<>();
        String[] tmp = path.split("/");
        for (String e : tmp) {
            if (hierarchy.isEmpty()) {
                hierarchy.push("/");
            } else if (hierarchy.peek().equals("/")){
                hierarchy.push("/" + e);
            } else {
                hierarchy.push(hierarchy.peek() + "/" + e);
            }
        }
        hierarchy.pop();
        Log.e("H", hierarchy.peek());
    }

    public void setResponse(boolean response) {
        this.response.set(response);
    }

    public void getFiles() {
        try {
            Vector<ChannelSftp.LsEntry> v = new Vector<>();
            ChannelSftp.LsEntrySelector selector = entry -> {
                if (!entry.getFilename().equals("..")
                    && !entry.getFilename().equals("."))
                    v.addElement(entry);
                return 0;
            };
            list = new ArrayList<>();
            Log.e("PATH", path);
            sftp.ls(path, selector);
            for (Object e : v) {
                String[] data = e.toString().split("\\s+");
                if (data[0].matches("[dl].+")) { //dir or link
                    list.add(new Directory(data, path));
                } else { //file
                    list.add(new File(data));
                }
            }
            Message m = new Message();
            m.arg1 = 3;
            handler.sendMessage(m);
        } catch (SftpException e) {
            Log.e("SFTP", e.toString());
            Message m = new Message();
            m.arg1 = 2;
            m.obj = e.getMessage();
            handler.sendMessage(m);
            e.printStackTrace();
        }
    }
}
