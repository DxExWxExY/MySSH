package com.dxexwxexy.myssh.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dxexwxexy.myssh.Data.Directory;
import com.dxexwxexy.myssh.Data.File;
import com.dxexwxexy.myssh.Data.FileSystemEntry;
import com.dxexwxexy.myssh.Networking.SFTP;
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
        ImageView icon;

        FileItemHolder(@NonNull View itemView) {
            super(itemView);
            menu = itemView.findViewById(R.id.file_menu);
            info = itemView.findViewById(R.id.file_info);
            layout = itemView.findViewById(R.id.file_layout);
            icon = itemView.findViewById(R.id.file_entry_type);
        }

        void setData() {
            data = list.get(i);
            if (data instanceof Directory) {
                info.setText(data.getName()+"/");
                icon.setImageResource(R.drawable.ic_folder);
            } else if (data instanceof File) {
                info.setText(data.getName());
                icon.setImageResource(R.drawable.ic_file);
            }
            layout.setOnClickListener(e -> {
                if (data instanceof Directory) {
                    SFTP.path = ((Directory) data).getPath() + "/" + data.getName();
                    ((FilesActivity) context).sftp.fetch = true;
                }
            });
            menu.setOnClickListener(e -> {
                PopupMenu popupMenu = new PopupMenu(context, menu);
                popupMenu.getMenuInflater().inflate(R.menu.file_item_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.file_menu_details:
                            fileDetailsDialog();
                            return true;
                        default:
                            return false;
                    }
                });
                popupMenu.show();
            });
        }

        private void fileDetailsDialog() {
            AlertDialog.Builder detailsBuilder = new AlertDialog.Builder(context);
            @SuppressLint("InflateParams")
            View detailsView = ((FilesActivity) context).getLayoutInflater().inflate(R.layout.file_details_dialog, null);
            TextView infoDisplay = detailsView.findViewById(R.id.file_dialog_deltails);
            Button dismiss = detailsView.findViewById(R.id.file_dialog_dismiss);
            final String details = String.format(
                    "Permissions: %s\n# Links: %s\nOwner: %s\nGroup: %s\nSize: %s" +
                            "\nLast Mod.: %s\nName: %s\n",
                    data.getPermissions(), data.getLinks(), data.getOwner(), data.getGroup(),
                    data.getSize(), data.getDate(), data.getName());
            if (data instanceof Directory) {
                infoDisplay.setText(details + "Path: " + ((Directory) data).getPath());
                Log.e("DEBUG", ((Directory) data).getPath());
            } else if (data instanceof File) {
                infoDisplay.setText(details + "Type: " + ((File) data).getType());
            }
            Log.e("DEBUG", details);
            detailsBuilder.setView(detailsView);
            AlertDialog detailsDialog = detailsBuilder.create();
            detailsDialog.show();
            dismiss.setOnClickListener(e -> detailsDialog.dismiss());
        }
    }
}
