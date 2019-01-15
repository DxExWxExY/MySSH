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
import java.util.concurrent.atomic.AtomicBoolean;

public class SFTP extends Thread {

    private Client c;
    private Handler handler;
    private UserInfo userInfo;
    private ChannelSftp sftp;
    private AtomicBoolean response = new AtomicBoolean(false);

    public String path;
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
            Message message = new Message();
            message.arg1 = 1;
            message.obj = "Connection Successful!";
            handler.sendMessage(message);
        } catch (JSchException | SftpException e) {
            ((FilesViewer) context).toast(e.toString(), 1);
            ((FilesViewer) context).finish();
        }
    }

    public void setResponse(boolean response) {
        this.response.set(response);
    }

    public void getFiles() {
        try {
            list = new ArrayList<>();
            for (Object e : sftp.ls(path)) {
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
            e.printStackTrace();
        }
    }
}
