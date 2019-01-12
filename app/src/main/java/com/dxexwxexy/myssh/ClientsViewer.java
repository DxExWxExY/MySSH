package com.dxexwxexy.myssh;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

public class ClientsViewer extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<Client> list;

    public ClientsViewer(Context context, ArrayList<Client> list) {
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
        client.info.setText(String.format("%s@%s -p %s",
                list.get(i).getUser(),
                list.get(i).getHost(),
                list.get(i).getPort()));
        client.layout.setOnClickListener(e -> {
            // FIXME: 1/11/2019 Remove toast and implement activity
            ((MainActivity) context).toast(list.get(i).toString(), 0);
        });
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

    public void updateList() {
        list = ((MainActivity) context).db.getClients();
        notifyDataSetChanged();
    }

    class ClientItemHolder extends RecyclerView.ViewHolder {

        TextView info;
        Button menu;
        ConstraintLayout layout;
        Client data;

        public ClientItemHolder(@NonNull View itemView) {
            super(itemView);
            info = itemView.findViewById(R.id.user_host_info);
            layout = itemView.findViewById(R.id.conn_layout);
            menu = itemView.findViewById(R.id.client_menu);
            layout.setElevation(10);
            menu.setOnClickListener(e -> {
                PopupMenu popupMenu = new PopupMenu(context, menu);
                popupMenu.getMenuInflater().inflate(R.menu.client_item_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.item_edit:
                            // FIXME: 1/11/2019 create dialog tha passed data back as a new client
//                            ((MainActivity) context).db.editClient();
                            return true;
                        case R.id.item_delete:
                            ((MainActivity) context).db.deleteClient(data);
                            updateList();
                            return true;
                        default:
                            return false;
                    }
                });
                popupMenu.show();
            });
        }
    }
}
