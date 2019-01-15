package com.dxexwxexy.myssh.Activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dxexwxexy.myssh.Data.Directory;
import com.dxexwxexy.myssh.Data.File;
import com.dxexwxexy.myssh.Data.FileSystemEntry;
import com.dxexwxexy.myssh.R;

import java.util.ArrayList;

public class FilesViewer extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<FileSystemEntry> list;

    FilesViewer(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.file_item_view, viewGroup, false);
        return new FileItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        FileItemHolder file = (FileItemHolder) viewHolder;
        file.i = i;
        file.setData();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    void update() {
        list = ((FilesActivity) context).sftp.list;
        notifyDataSetChanged();
    }

    class FileItemHolder extends RecyclerView.ViewHolder {

        int i;
        FileSystemEntry data;
        Button menu;
        TextView info;
        ConstraintLayout layout;

        FileItemHolder(@NonNull View itemView) {
            super(itemView);
            menu = itemView.findViewById(R.id.file_menu);
            info = itemView.findViewById(R.id.file_info);
            layout = itemView.findViewById(R.id.file_layout);

            // TODO: 1/14/2019 Define menu and check instanceof to determine
            // the action of the listener
        }

        void setData() {
            data = list.get(i);
            if (data instanceof Directory) {
                info.setText(data.getName()+"/");
            } else if (data instanceof File) {
                info.setText(data.getName());
            }
        }
    }
}
