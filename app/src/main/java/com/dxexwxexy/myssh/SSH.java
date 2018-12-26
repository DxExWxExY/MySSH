package com.dxexwxexy.myssh;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.SshByPassword;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

public class SSH {
    private int port;
    private SshByPassword ssh;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private String user;
    private String pass;
    private String host;
    private String response;


    public SSH(String user, String pass, int port, String host) {
        this.user = user;
        this.pass = pass;
        this.port = port;
        this.host = host;
        try {
            this.ssh = new SshByPassword(host, port, user, pass);
        } catch (UnknownHostException e) {
            this.response = e.toString();
        }
    }

    public String exec(String command, File file) throws IOException {
        // FIXME: 12/25/2018 Learn how to upload and download files properly
        if (file == null) {
            this.response = new Shell.Plain(new Shell.Safe(ssh)).exec(command);
        } else {
            // file upload
            Shell shell = new Shell.Safe(ssh);
            shell.exec(command, new FileInputStream(file), out, err);
        }
        return response;
    }

    /**
     * Getters and setters.
     */
    public String getresponse() {
        return response;
    }
}
