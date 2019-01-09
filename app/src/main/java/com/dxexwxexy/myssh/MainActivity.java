package com.dxexwxexy.myssh;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton add_client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.my_clients);
        initUI();
        initListeners();
    }

    private void initUI() {
        add_client = findViewById(R.id.add_client);
    }

    private void initListeners() {
        add_client.setOnClickListener(view -> {
            addClientDialog();
        });
    }

    /**
     * Creates a dialog dedicated to add new clients to a DB.
    * */
    private void addClientDialog() {
        AlertDialog.Builder addClientDialogBuilder = new AlertDialog.Builder(this);
        View addView = getLayoutInflater().inflate(R.layout.add_client_dialog, null);
        EditText user = addView.findViewById(R.id.add_dialog_user);
        EditText host = addView.findViewById(R.id.add_dialog_host);
        EditText pass = addView.findViewById(R.id.add_dialog_pass);
        EditText port = addView.findViewById(R.id.add_dialog_port);
        Button add = addView.findViewById(R.id.add_dialog_add);
        addClientDialogBuilder.setView(addView);
        AlertDialog addClientDialog = addClientDialogBuilder.create();
        add.setOnClickListener(e -> {
            // FIXME: 1/8/2019 add entry to DB
            addClientDialog.dismiss();
        });
        addClientDialog.show();
    }
}
