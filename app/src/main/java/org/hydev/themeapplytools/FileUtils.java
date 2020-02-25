package org.hydev.themeapplytools;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();

        try {
            String[] urlSplit = url.split("/");
            String fileName = URLDecoder.decode(urlSplit[urlSplit.length - 1], "UTF-8");
            request.setDestinationInExternalPublicDir("Download", fileName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new AssertionError();
        }

        downloadManager.enqueue(request);

        Toast.makeText(activity, "已开始下载", Toast.LENGTH_LONG).show();
    }

    static void chooseFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.startActivityForResult(intent, 7);
    }
}
