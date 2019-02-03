package com.dxexwxexy.sftp.Activities;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dxexwxexy.sftp.Data.Client;
import com.dxexwxexy.sftp.Networking.ProgressMonitor;
import com.dxexwxexy.sftp.Networking.SFTP;
import com.dxexwxexy.sftp.R;

import java.io.File;
import java.util.Objects;

public class FilesActivity extends AppCompatActivity {

    File dir;
    SFTP sftp;
    Thread fetcher;
    Handler connHandler;
    FilesViewer filesViewer;
    RecyclerView recyclerView;
    ConstraintLayout fetchProgress;
    final static String UP = "1", DW = "2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        initUI();
        initHandlers();
        initConn();
        initFilesViewer();
    }

    @Override
    public void onBackPressed() {
        try {
            if (sftp.hasParentDir()) {
                recyclerView.setVisibility(View.GONE);
                fetchProgress.setVisibility(View.VISIBLE);
                sftp.setPath(sftp.popParentDir());
                sftp.fetch = true;
            } else {
                toast(getString(R.string.no_parent_dir), 1);
            }
        } catch (NullPointerException e) {
            toast("Connection Unsuccessful", 1);
            sftp.close();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.files_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // TODO: 1/16/2019 finish implementations of menu
            case R.id.files_menu_put:
                fileSelector();
                return true;
            case R.id.files_menu_mkdir:
                createDirDialog();
                return true;
            case R.id.files_menu_shell:

                return true;
            case R.id.files_menu_close:
                closeConnDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode != 0) {
            assert data != null;
            Uri uri = data.getData();
            File file = new File(getPath(uri));
            sftp.put(file, new ProgressMonitor(this, UP, file.getName()));
        }
    }

    void initUI() {
        fetchProgress = findViewById(R.id.conn_start);
    }

    void initFilesViewer() {
        recyclerView = findViewById(R.id.files_viewer);
        filesViewer = new FilesViewer(this);
        recyclerView.setAdapter(filesViewer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    void initConn() {
        if (MainActivity.checkInternet(this)) {
            initHandlers();
            Client c = Objects.requireNonNull(getIntent().getExtras()).getParcelable("client");
            sftp = new SFTP(c, connHandler);
            sftp.start();
        } else {
            toast("No Internet Connection", 1);
            finish();
        }
    }

    private void createFolder() {
        File parent = new File(Environment.getExternalStorageDirectory()
                + File.separator + "Download" + File.separator + "SFTP");
        dir = new File(Environment.getExternalStorageDirectory()
                + File.separator + "Download" + File.separator + "SFTP"
                + File.separator + sftp.getInfo());
        if (!parent.exists() && !parent.isDirectory()) {
            if (parent.mkdir()) {
                Log.e("parent", "created");
            } else {
                Log.e("parent", "not created");
            }
        }
        if (!dir.exists() && !dir.isDirectory()) {
            if (dir.mkdir()) {
                Log.e("dir", "created client");
            } else {
                Log.e("dir", "not created client");
            }
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    void initHandlers() {
        connHandler = new Handler(message -> {
            switch (message.arg1) {
                case 1: //success
                    updateTitle();
                    createFolder();
                    fetcher.start();
                    sftp.fetch = true;
                    return true;
                case 2: //error
                    Log.e("Error Toast", message.obj.toString());
                    if (message.arg2 == 1) { //fatal
                        sftp.close();
                        finish();
                    }
                    if (!message.obj.toString().isEmpty()) {
                        toast(message.obj.toString(), 1);
                    }
                    return true;
                case 3: //refresh
                    fetchProgress.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    filesViewer.update();
                    updateTitle();
                    return true;
                case 4: //prompt
                    ynDialog(message.obj.toString());
                    return true;
                case 5: //toast
                    toast((String) message.obj, 1);
                    return true;
                default:
                    return false;
            }
        });
        fetcher = new Thread(() -> {
            while (true) {
                if (sftp.fetch) {
                    sftp.fetchFiles();
                    sftp.fetch = false;
                }
            }
        });
    }

    private void updateTitle() {
        String title = sftp.getPath().substring(sftp.getPath().lastIndexOf("/") + 1);
        setTitle(title + "/");
    }

    /**
     * Helper methods to reduce visual clutter.
     * */
    public void toast(String msg, int len) {
        Toast.makeText(this, msg, len).show();
    }

    public void ynDialog(String msg) {
        android.support.v7.app.AlertDialog.Builder ynBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            synchronized (sftp.lock) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        sftp.setResponse(true);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        sftp.setResponse(false);
                        break;
                }
                sftp.lock.notify();
            }
        };
        ynBuilder.setTitle(R.string.conn_msg);
        ynBuilder.setMessage(msg);
        ynBuilder.setCancelable(false);
        ynBuilder.setPositiveButton("Yes", listener);
        ynBuilder.setNegativeButton("No", listener);
        ynBuilder.show();
    }

    public void closeConnDialog() {
        android.support.v7.app.AlertDialog.Builder closeConnBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        closeConnBuilder.setTitle("Close Connection?");
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    finish();
                    sftp.close();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        };
        closeConnBuilder.setPositiveButton("Yes", listener);
        closeConnBuilder.setNegativeButton("No", listener);
        closeConnBuilder.show();
    }
    
    public void createDirDialog() {
        int p = (int) getResources().getDisplayMetrics().density*20;
        final EditText input = new EditText(this);
        input.setHint(R.string.directory_name);
        AlertDialog renameDialog = new AlertDialog.Builder(this)
                .setTitle("Create Directory")
                .setPositiveButton("OK", (dialog, which) ->
                        sftp.mkdir(String.valueOf(input.getText())))
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss())
                .create();
        renameDialog.setView(input, p, p, p, p);
        renameDialog.show();
    }

    private void fileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"),1);
        } catch (android.content.ActivityNotFoundException ex) {
            toast("Please install a File Manager.", 1);
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param uri The Uri to query.
     * @author paulburke
     */
    public String getPath(final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        final String column = "_data";
//        final String[] projection = {column};
        final String[] projection = new String[]{
                "content://downloads/public_downloads",
                "content://downloads/my_downloads",
                "content://downloads/all_downloads"
        };

        try (Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
