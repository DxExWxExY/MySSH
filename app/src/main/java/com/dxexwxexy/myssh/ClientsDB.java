package com.dxexwxexy.myssh;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class ClientsDB extends SQLiteOpenHelper {

    private static final String TB_NAME = "clients";
    /*Columns*/
    private static final String USER = "user";
    private static final String HOST = "host";
    private static final String PASS = "pass";
    private static final String PORT = "port";

    public ClientsDB(Context context) {
        super(context, TB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String cmd = String.format("CREATE TABLE %s ( %s TEXT, %s TEXT, %s TEXT, %s REAL )",
                TB_NAME, USER, HOST, PASS, PORT);
        sqLiteDatabase.execSQL(cmd);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String cmd = String.format("DROP TABLE IF EXISTS %s", TB_NAME);
        sqLiteDatabase.execSQL(cmd);
        onCreate(sqLiteDatabase);
    }

    public void addClient(Client c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put(USER, c.getUser());
        content.put(HOST, c.getHost());
        content.put(PASS, c.getPass());
        content.put(PORT, c.getPass());
        db.insert(TB_NAME, null, content);
    }

    public void editClient(Client older, Client newer) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put(USER, newer.getUser());
        content.put(HOST, newer.getHost());
        content.put(PASS, newer.getPass());
        content.put(PORT, newer.getPort());
        String whereClause = String.format("%s =? AND %s =? AND %s =? AND %s =?",
                USER, HOST, PASS, PORT);
        db.update(TB_NAME, content, whereClause, older.getData());
    }

    public void deleteClient(Client c) {
        SQLiteDatabase db = getWritableDatabase();
        @SuppressLint("DefaultLocale") String cmd = String.format(
                "DELETE FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s' AND %s = '%d'",
                TB_NAME, USER, c.getUser(), HOST, c.getHost(), PASS, c.getPass(), PORT , c.getPort()
        );
        db.execSQL(cmd);
    }

    public ArrayList<Client> getClients() {
        // FIXME: 1/10/2019 Some exemption around here
        SQLiteDatabase db = getReadableDatabase();
        String cmd = String.format("SELECT * FROM %s", TB_NAME);
        Cursor data = db.rawQuery(cmd, null);
        ArrayList<Client> clients = new ArrayList<>();
        if (data.getCount() == 0) {
            return clients;
        }
        while (data.moveToNext()) {
            clients.add(new Client(
                    data.getString(0),
                    data.getString(1),
                    data.getString(2),
                    Integer.parseInt(data.getString(3)))
            );
        }
        data.close();
        return clients;
    }
}
