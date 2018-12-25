package com.dxexwxexy.myssh;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

public class SSH {
    private int port;
    private Shell shell;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private String user;
    private String pass;
    private String host;
    private String response;
    private String error;


    public SSH(String user, String pass, int port, String host) {
        this.user = user;
        this.pass = pass;
        this.port = port;
        this.host = host;
        try {
            this.shell = new Shell.Safe(new Ssh(host, port, user, pass));
            this.error =  null;
        } catch (UnknownHostException e) {
            this.error = e.toString();
        }
    }

    public String exec(String command) {
        // FIXME: 12/25/2018 Initialize IO streams
        this.response = shell.exec();


    }

    /**
     * Getters and setters.
     * */
    public String getError() {
        return error;
    }

    public String getResponse() {
        return response;
    }
}
