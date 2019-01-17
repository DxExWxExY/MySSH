package com.dxexwxexy.myssh.Testing;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

public class Tester {

    final static Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        String a = "fdsfs";
        System.out.println(a.substring(-1, 1));
//        ssh(args);
        sftp(args);
        Vector
    }

    private static void sftp(String[] args) {
        int port = 22;
        String user = "fs-dexefree";
        String pass = args[0];
        String host = "war.freeshells.org";
        JSch jsch = new JSch();
//        String knownHostsFilename = System.getProperty("user.dir")+"\\host.txt";
        try {
            Session session = jsch.getSession(user, host, port);
            // FIXME: 12/30/2018 Implement prompts
            UserInfo userInfo = new UserInfo() {
                @Override
                public String getPassphrase() {
                    System.out.printf("Enter Passphrase: \n");
                    return IN.nextLine();
                }

                @Override
                public String getPassword() {
                    System.out.printf("Enter Password: \n");
                    return pass;
                }

                @Override
                public boolean promptPassword(String message) {
                    System.out.printf("%s\n", message);
                    return true;
                }

                @Override
                public boolean promptPassphrase(String message) {
                    System.out.printf("%s\n", message);
                    return true;
                }

                @Override
                public boolean promptYesNo(String message) {
                    System.out.printf("%s: (y/n)? \n", message);
                    return IN.next().toLowerCase().equals("y");
                }

                @Override
                public void showMessage(String message) {
                    System.out.printf("Message: %s.\n", message);
                }
            };
            session.setUserInfo(userInfo);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftp = (ChannelSftp) channel;
            String path = sftp.pwd();
            for (Object e : sftp.ls(path)) {
                System.out.printf("%s\n", e);
            }
            System.out.println("=================");
            for (Object e : sftp.ls(path+"/testdir")) {
                System.out.printf("%s\n", e);
            }
//            sftp.get("/var/www/clients/client4257/web4405/home/fs-dexefree/t.txt", "D:\\t.txt");
        } catch (JSchException | SftpException e) {
            System.out.printf("sftp: %s\n", e.toString());
        }
    }

    private static void ssh(String[] args)  {
        int port = 22;
        String user = "fs-dexefree";
        String pass = args[0];
        String host = "war.freeshells.org";
        SSH ssh = new SSH(user, pass, port, host);
        String cmd = "pwd";
        do {
            try {
                System.out.print(ssh.exec(cmd, null) + "> ");
                cmd = IN.nextLine();
            } catch (IllegalArgumentException | IOException e) {
                System.out.printf("ssh: %s, try again.\n> ", e.toString());
                cmd = IN.nextLine();
            }
        } while (!cmd.equals("exit"));
        System.out.println("Exiting Shell...");
    }
}
