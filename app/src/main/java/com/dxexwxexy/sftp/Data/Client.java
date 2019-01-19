package com.dxexwxexy.sftp.Data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Client implements Parcelable {
    private String user;
    private String host;
    private String pass;
    private String phrase;
    private int port;

    public Client(String user, String host, String pass, int port) {
        this.user = user;
        this.host = host;
        this.pass = pass;
        this.port = port;
    }

    protected Client(Parcel in) {
        user = in.readString();
        host = in.readString();
        pass = in.readString();
        phrase = in.readString();
        port = in.readInt();
    }

    public static final Creator<Client> CREATOR = new Creator<Client>() {
        @Override
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }

        @Override
        public Client[] newArray(int size) {
            return new Client[size];
        }
    };

    public String[] getData() {
        return new String[]{user, host, pass, String.valueOf(port)};
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Client) {
            Client that = (Client) obj;
            return user.equals(that.user) && host.equals(that.host)
                    && pass.equals(that.pass) && port == that.port;
        }
        return false;
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

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public String getPhrase() {
        return phrase;
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

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(user);
        parcel.writeString(host);
        parcel.writeString(pass);
        parcel.writeString(phrase);
        parcel.writeInt(port);
    }
}
