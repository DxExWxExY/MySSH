package com.dxexwxexy.myssh;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dxexwxexy.myssh.Testing.SFTP;

import java.util.Objects;

public class FilesViewer extends AppCompatActivity {

    Handler connHandler;
    SFTP sftp;
    AlertDialog connDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_viewer);
        if (MainActivity.checkInternet(this)) {
            connectionDialog();
        } else {
            toast("No Internet Connection", 1);
            finish();
        }
    }

    void connectionDialog() {
        AlertDialog.Builder startConnBuilder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams")
        View startView = getLayoutInflater().inflate(R.layout.start_conn, null);
        ProgressBar wait = startView.findViewById(R.id.conn_dialog_prog);
        TextView msg = startView.findViewById(R.id.conn_dialog_msg);
        startConnBuilder.setView(startView);
        connDialog = startConnBuilder.create();
        connHandler = new Handler(message -> {
            if (message.arg1 == 1) {
                toast(message.obj.toString(), 1);
                connDialog.dismiss();
                return true;
            } else if (message.arg1 == 2) {
                msg.setText(message.obj.toString());
                wait.setVisibility(View.INVISIBLE);
                return false;
            }
            return false;
        });
        connDialog.show();
        Client c = Objects.requireNonNull(getIntent().getExtras()).getParcelable("client");
        new SFTPConnection().execute(c);

    }

    /**
     * Helper methods to reduce visual clutter.
     * */
    public void toast(String msg, int len) {
        Toast.makeText(this, msg, len).show();
    }

    class SFTPConnection extends AsyncTask<Client, Void, Void> {

        @Override
        protected Void doInBackground(Client... clients) {
            sftp = new SFTP(clients[0] , connHandler, FilesViewer.this);
            sftp.run();
            return null;
        }
    }
}
