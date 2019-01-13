package com.dxexwxexy.myssh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton add_client;
    ClientsDB db;
    ClientsViewer clientsViewer;
    TextView hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.my_clients);
        db = new ClientsDB(this);
        initUI();
        initListeners();
        initClientsViewer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all:
                deleteClientsDialog();
                return true;
            default:
                return false;
        }
    }

    private void initUI() {
        hint = findViewById(R.id.hint_add);
        add_client = findViewById(R.id.add_client);
    }

    private void initListeners() {
        add_client.setOnClickListener(view -> addClientDialog());
    }

    private void initClientsViewer() {
        RecyclerView recyclerView = findViewById(R.id.client_viewer);
        clientsViewer = new ClientsViewer(this, db.getClients());
        recyclerView.setAdapter(clientsViewer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Creates a dialog dedicated to add new clients to the DB.
    * */
    private void addClientDialog() {
        AlertDialog.Builder addClientDialogBuilder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams")
        View addView = getLayoutInflater().inflate(R.layout.add_client_dialog, null);
        EditText user = addView.findViewById(R.id.add_dialog_user);
        EditText host = addView.findViewById(R.id.add_dialog_host);
        EditText pass = addView.findViewById(R.id.add_dialog_pass);
        EditText port = addView.findViewById(R.id.add_dialog_port);
        Button add = addView.findViewById(R.id.add_dialog_add);
        addClientDialogBuilder.setView(addView);
        AlertDialog addClientDialog = addClientDialogBuilder.create();
        add.setOnClickListener(e -> {
            db.addClient(new Client(
                    getText(user),
                    getText(host),
                    getText(pass),
                    getInt(port))
            );
            clientsViewer.updateList();
            addClientDialog.dismiss();
            toast("Added Client", 1);
        });
        addClientDialog.show();
    }

    public void deleteClientsDialog() {
        AlertDialog.Builder deleteClientBuilder = new AlertDialog.Builder(this);
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    db.deleteAll();
                    clientsViewer.updateList();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        deleteClientBuilder.setMessage("Delete All Clients?");
        deleteClientBuilder.setPositiveButton("Yes", listener);
        deleteClientBuilder.setNegativeButton("Cancel", listener);
        deleteClientBuilder.show();
    }

    /**
     * Helper methods to reduce visual clutter.
     * */
    public static String getText(EditText field) {
        return String.valueOf(field.getText());
    }

    public static int getInt(EditText field) {
        return Integer.parseInt(String.valueOf(field.getText()));
    }

    public static boolean checkInternet(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (!wifi.isConnected()) {
            return mobile.isConnected();
        } else if (!mobile.isConnected()) {
            return wifi.isConnected();
        } else {
            return false;
        }
    }

    public void toast(String msg, int len) {
        Toast.makeText(this, msg, len).show();
    }
}
