package com.dxexwxexy.myssh.Testing;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

import com.dxexwxexy.myssh.Client;
import com.dxexwxexy.myssh.FilesViewer;
import com.dxexwxexy.myssh.MainActivity;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

import java.util.concurrent.atomic.AtomicBoolean;

public class SFTP implements Runnable {
    private Client c;
    private Handler h;
    private Context context;
    private UserInfo userInfo;
    private AtomicBoolean response = new AtomicBoolean(false);

    public SFTP(Client c, Handler h, Context context) {
        this.c = c;
        this.h = h;
        this.context = context;
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
                /*ynDialog(message);
                return response.get();*/
                return true;
            }

            @Override
            public void showMessage(String message) {
                ((MainActivity) context).toast(message, 1);
            }
        };
    }

    private void ynDialog(String msg) {
        android.support.v7.app.AlertDialog.Builder deleteClientBuilder = new android.support.v7.app.AlertDialog.Builder(context);
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    response.set(true);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    response.set(false);
                    break;
            }
        };
        deleteClientBuilder.setMessage(msg);
        deleteClientBuilder.setPositiveButton("Yes", listener);
        deleteClientBuilder.setNegativeButton("No", listener);
        deleteClientBuilder.show();
    }

    @Override
    public void run() {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(c.getUser(), c.getHost(), c.getPort());
            session.setUserInfo(userInfo);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect(1);
            ChannelSftp sftp = (ChannelSftp) channel;
            //Test Code
            String result = "";
            for (Object e : sftp.ls(sftp.pwd())) {
                result += String.format("%s\n", e);
            }
            Message message = new Message();
            message.arg1 = 1;
            message.obj = result;
            h.sendMessage(message);
        } catch (JSchException | SftpException e) {
            ((FilesViewer) context).toast(e.toString(), 1);
            ((FilesViewer) context).finish();
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
