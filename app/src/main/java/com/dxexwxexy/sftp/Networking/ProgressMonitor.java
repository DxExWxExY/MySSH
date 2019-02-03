package com.dxexwxexy.sftp.Networking;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.dxexwxexy.sftp.R;
import com.jcraft.jsch.SftpProgressMonitor;

public class ProgressMonitor implements SftpProgressMonitor {

    private Context context;
    private String channel, fileName;
    private NotificationCompat.Builder notification;
    private NotificationManagerCompat notificationManager;
    private long size;

    public ProgressMonitor(Context context, String channel, String fileName) {
        this.context = context;
        this.channel = channel;
        this.fileName = fileName;
        initNotification();
    }

    private void initNotification() {
        notification = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setProgress(0, 0, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_transfer);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(channel, name, importance);
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager = NotificationManagerCompat.from(context);
    }

    @Override
    public void init(int op, String src, String dest, long max) {
        size = max;
        if (op == SftpProgressMonitor.PUT) {
            notification.setContentTitle(context.getString(R.string.uploading) + " " + fileName);
        } else if (op == SftpProgressMonitor.GET) {
            notification.setContentTitle(context.getString(R.string.downloading) + " " + fileName);
        }
        notificationManager.notify(Integer.parseInt(channel), notification.build());
    }

    @Override
    public boolean count(long count) {
        return count < size;
    }

    @Override
    public void end() {
        notification.setContentText(context.getString(R.string.transfer_complete));
        notification.setProgress(0, 0, false);
        notificationManager.notify(Integer.parseInt(channel), notification.build());
    }
}
