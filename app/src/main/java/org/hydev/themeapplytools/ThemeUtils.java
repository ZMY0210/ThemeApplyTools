package org.hydev.themeapplytools;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

class ThemeUtils {
    private static final String THEME_API_URL = "https://thm.market.xiaomi.com/thm/download/v2/";

    static void applyTheme(Activity activity, String filePath) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName("com.android.thememanager", "com.android.thememanager.ApplyThemeForScreenshot"));

        Bundle bundle = new Bundle();
        bundle.putString("theme_file_path", filePath);
        bundle.putString("api_called_from", "test");

        intent.putExtras(bundle);
        activity.startActivity(intent);

        MainActivity.applied = true;
    }

    static void getThemeDownloadLinkAsync(Activity activity, String themeLink, Callback callback) {
        String[] themeLinkSplit = themeLink.split("/detail/.*?");
        if (themeLinkSplit.length != 2) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle("错误")
                    .setMessage("请输入主题的分享链接，例如：\n" +
                            "http://zhuti.xiaomi.com/detail/f02025cb-8f0e-44e0-b39a-653e46d84d42")
                    .setNegativeButton("OK", null)
                    .show();

            return;
        }

        String themeToken = themeLinkSplit[themeLinkSplit.length - 1];

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(THEME_API_URL + themeToken).build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    static Map<String, String> getThemeInfo(ResponseBody responseBody) {
        try {
            JsonObject jsonObject = new Gson().fromJson(responseBody.string(), JsonObject.class);
            int apiCode = jsonObject.get("apiCode").getAsInt();

            if (apiCode == 0) {
                JsonObject apiDataJsonObject = jsonObject.getAsJsonObject("apiData");

                String downloadUrl = URLDecoder.decode(apiDataJsonObject.get("downloadUrl").getAsString(), "UTF-8");
                String fileHash = apiDataJsonObject.get("fileHash").getAsString().toUpperCase();
                String fileSize = String.format(Locale.CHINESE, "%.2f", apiDataJsonObject.get("fileSize").getAsInt() / 10e5) + " MB";

                String[] downloadUrlSpilt = downloadUrl.split("/");
                String fileName = URLDecoder.decode(downloadUrlSpilt[downloadUrlSpilt.length - 1], "UTF-8");

                Map<String, String> themeInfo = new HashMap<>();
                themeInfo.put("downloadUrl", downloadUrl);
                themeInfo.put("fileHash", fileHash);
                themeInfo.put("fileSize", fileSize);
                themeInfo.put("fileName", fileName);

                return themeInfo;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }
}
