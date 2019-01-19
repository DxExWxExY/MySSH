package com.dxexwxexy.sftp.Activities;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dxexwxexy.sftp.Data.Client;
import com.dxexwxexy.sftp.R;

import java.util.ArrayList;

class ClientsViewer extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<Client> list;

    ClientsViewer(Context context, ArrayList<Client> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.client_item_view, viewGroup, false);
        return new ClientItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ClientItemHolder client = (ClientItemHolder) viewHolder;
        //Passing instance being displayed by holder
        client.data = list.get(i);
        client.info.setText(
                String.format("%s@%s -p %s", list.get(i).getUser(),
                        list.get(i).getHost(), list.get(i).getPort())
        );
    }

    @Override
    public int getItemCount() {
        if (list == null) {
            ((MainActivity) context).hint.setVisibility(View.VISIBLE);
            return 0;
        } else {
            ((MainActivity) context).hint.setVisibility(View.GONE);
            return list.size();
        }
    }

    void update() {
        list = ((MainActivity) context).db.getClients();
        notifyDataSetChanged();
    }

    class ClientItemHolder extends RecyclerView.ViewHolder {

        TextView info;
        Button menu;
        ConstraintLayout layout;
        Client data;

        ClientItemHolder(@NonNull View itemView) {
            super(itemView);
            info = itemView.findViewById(R.id.user_host_info);
            layout = itemView.findViewById(R.id.conn_layout);
            menu = itemView.findViewById(R.id.client_menu);
            //Menu Creation
            menu.setOnClickListener(e -> {
                PopupMenu popupMenu = new PopupMenu(context, menu);
                popupMenu.getMenuInflater().inflate(R.menu.client_item_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.item_edit:
                            editClientDialog(data);
                            return true;
                        case R.id.item_delete:
                            ((MainActivity) context).db.deleteClient(data);
                            update();
                            return true;
                        default:
                            return false;
                    }
                });
                popupMenu.show();
            });
            layout.setOnClickListener(e -> {
                Intent intent = new Intent(context, FilesActivity.class);
                intent.putExtra("client", data);
                context.startActivity(intent);
            });
        }

        void editClientDialog(Client o) {
            AlertDialog.Builder editClientBuilder = new AlertDialog.Builder(context);
            @SuppressLint("InflateParams")
            View editView = ((MainActivity) context).getLayoutInflater().inflate(R.layout.edit_client_dialog, null);
            EditText user = editView.findViewById(R.id.edit_dialog_user);
            EditText host = editView.findViewById(R.id.edit_dialog_host);
            EditText pass = editView.findViewById(R.id.edit_dialog_pass);
            EditText port = editView.findViewById(R.id.edit_dialog_port);
            Button update = editView.findViewById(R.id.edit_dialog_update);
            //Populate fields with existing data
            user.setText(o.getUser());
            host.setText(o.getHost());
            pass.setText(o.getPass());
            port.setText(String.valueOf(o.getPort()));
            editClientBuilder.setView(editView);
            AlertDialog editClientDialog = editClientBuilder.create();
            update.setOnClickListener(e -> {
                Client n = new Client(
                        MainActivity.getText(user),
                        MainActivity.getText(host),
                        MainActivity.getText(pass),
                        MainActivity.getInt(port));
                if (n.equals(o)) {
                    ((MainActivity) context).toast("Entry Not Updated", 1);
                } else {
                    ((MainActivity) context).db.editClient(o, n);
                    ((MainActivity) context).toast("Entry Updated", 1);
                    update();
                }
                editClientDialog.dismiss();
            });
            editClientDialog.show();
        }
    }
}
