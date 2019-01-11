package com.dxexwxexy.myssh;

import android.support.annotation.NonNull;

class Client {
    private String user;
    private String host;
    private String pass;
    private int port;

    public Client(String user, String host, String pass, int port) {
        this.user = user;
        this.host = host;
        this.pass = pass;
        this.port = port;
    }

    public String[] getData() {
        return new String[]{user, host, pass, String.valueOf(port)};
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s@%s:%s %s", user, host, port, pass);
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public String getPass() {
        return pass;
    }

    public int getPort() {
        return port;
    }
}
