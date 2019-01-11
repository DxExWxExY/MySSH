package com.dxexwxexy.myssh;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
        client.info.setText(String.format("%s@%s -p %s",
                list.get(i).getUser(),
                list.get(i).getHost(),
                list.get(i).getPort()));
        client.layout.setOnClickListener(e -> {
            ((MainActivity) context).toast(client.toString(), 1);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void updateList() {
        list = ((MainActivity) context).db.getClients();
        notifyDataSetChanged();
    }

    class ClientItemHolder extends RecyclerView.ViewHolder {

        EditText info;
        ConstraintLayout layout;
        // FIXME: 1/10/2019 Add button if menu fails

        public ClientItemHolder(@NonNull View itemView) {
            super(itemView);
            info = itemView.findViewById(R.id.user_host_info);
            layout = itemView.findViewById(R.id.conn_layout);
        }
    }
}
