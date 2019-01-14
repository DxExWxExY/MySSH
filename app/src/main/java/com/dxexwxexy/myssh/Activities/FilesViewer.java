package com.dxexwxexy.myssh.Activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dxexwxexy.myssh.Data.FileSystemEntry;
import com.dxexwxexy.myssh.R;

import java.util.ArrayList;

public class FilesViewer extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<FileSystemEntry> list;

    public FilesViewer(Context context, ArrayList<FileSystemEntry> list) {
        this.context = context;
        this.list = list;
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
        // FIXME: 1/13/2019 pass instance of FileSystemEntry
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    void update() {
        // FIXME: 1/13/2019 Update data using an sftp request
        notifyDataSetChanged();
    }

    class FileItemHolder extends RecyclerView.ViewHolder {

        public FileItemHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
