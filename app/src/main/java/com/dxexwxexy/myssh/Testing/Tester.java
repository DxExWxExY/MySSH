package com.dxexwxexy.myssh.Testing;

import com.dxexwxexy.myssh.SSH;

import java.io.IOException;
import java.util.Scanner;

public class Tester {

    final static Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
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
