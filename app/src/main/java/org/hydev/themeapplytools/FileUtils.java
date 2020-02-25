package org.hydev.themeapplytools;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

class FileUtils {
    static void copyLink(Activity activity, String link) {
        ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("downloadLink", link);
        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(activity, "已复制到剪切版", Toast.LENGTH_LONG).show();
    }

    static void systemDownload(Activity activity, String url) {
        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        downloadManager.enqueue(request);

        Toast.makeText(activity, "已开始下载", Toast.LENGTH_LONG).show();
    }
}
