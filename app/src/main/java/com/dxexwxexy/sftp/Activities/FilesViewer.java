package com.dxexwxexy.sftp.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dxexwxexy.sftp.Data.Directory;
import com.dxexwxexy.sftp.Data.File;
import com.dxexwxexy.sftp.Data.FileSystemEntry;
import com.dxexwxexy.sftp.Networking.ProgressMonitor;
import com.dxexwxexy.sftp.Networking.SFTP;
import com.dxexwxexy.sftp.R;

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
        file.i = viewHolder.getAdapterPosition();
        file.setData();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    void update() {
        list = getContext().sftp.list;
        notifyDataSetChanged();
    }

    private FilesActivity getContext() {
        return ((FilesActivity) context);
    }

    private SFTP getSftp() {
        return getContext().sftp;
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

        @SuppressLint("SetTextI18n")
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
                    getContext().recyclerView.setVisibility(View.GONE);
                    getContext().fetchProgress.setVisibility(View.VISIBLE);
                    getSftp().pushParentDir(getSftp().getPath());
                    getSftp().setPath(((Directory) data).getPath());
                    getSftp().fetch = true;
                } else if (data instanceof File) {
                    getSftp().get(data.getName(), getContext().dir.getAbsolutePath(),
                            new ProgressMonitor(getContext(), FilesActivity.DW, data.getName()));

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
                        case R.id.file_menu_rename:
                            renameDialog();
                            return true;
                        case R.id.file_menu_delete:
                            deleteDialog();
                            return true;
                        default:
                            return false;
                    }
                });
                popupMenu.show();
            });
        }

        private void deleteDialog() {
            android.support.v7.app.AlertDialog.Builder deleteBuilder = new android.support.v7.app.AlertDialog.Builder(context);
            if (data instanceof Directory) {
                deleteBuilder.setTitle("Delete Directory?");
            } else if (data instanceof File) {
                deleteBuilder.setTitle("Delete File?");
            }
            DialogInterface.OnClickListener listener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (data instanceof Directory) {
                            getContext().sftp.deleteFile(data.getName(), 1);
                        } else if (data instanceof File) {
                            getContext().sftp.deleteFile(data.getName(), 0);
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            };
            deleteBuilder.setPositiveButton("Yes", listener);
            deleteBuilder.setNegativeButton("No", listener);
            deleteBuilder.show();
        }

        @SuppressLint("SetTextI18n")
        private void fileDetailsDialog() {
            AlertDialog.Builder detailsBuilder = new AlertDialog.Builder(context);
            @SuppressLint("InflateParams")
            View detailsView = getContext().getLayoutInflater().inflate(R.layout.file_details_dialog, null);
            TextView infoDisplay = detailsView.findViewById(R.id.file_dialog_details);
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

        private void renameDialog() {
            int p = (int) context.getResources().getDisplayMetrics().density*20;
            final EditText input = new EditText(context);
            input.setText(data.getName());
            input.setHint(context.getString(R.string.new_file_name));
            AlertDialog builder = new AlertDialog.Builder(context)
                    .setPositiveButton("OK", (dialog, which) ->
                            getContext().sftp.renameFile(data.getName(),input.getText().toString()))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setTitle("Rename File")
                    .create();
            builder.setView(input, p, p, p, p);
            builder.show();
        }
    }
}
