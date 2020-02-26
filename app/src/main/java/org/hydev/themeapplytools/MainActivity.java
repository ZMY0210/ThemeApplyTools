package org.hydev.themeapplytools;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    private static final String GITHUB_URL = "https://github.com/VergeDX/ThemeApplyTools";
    private static final String ME_COOLAPK_URL = "https://coolapk.com/u/506843";
    private static final String ICEBOX_COOLAPK_URL = "https://coolapk.com/apk/com.catchingnow.icebox";

    static boolean applied = false;
    private static String filePath = null;

    /**
     * After apply theme, make a dialog.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (applied) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("完成")
                    .setMessage("主题已应用，\n" +
                            "若没有效果，可能是该主题不在根目录下，\n" +
                            "请仔细阅读说明.")
                    .setPositiveButton("OK", null)
                    .show();

            applied = false;
        }
    }

    /**
     * After user choose file, check the file and set filePath.
     *
     * @param requestCode is always 7.
     * @param data        contains user chosen file Uri.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 7 && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();

            // try-with-source
            try (Cursor cursor = getContentResolver().query(fileUri, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    if (!fileName.endsWith(".mtz")) {
                        Snackbar.make(findViewById(R.id.bt_chooseFile).getRootView(), R.string.not_mtz_file, Snackbar.LENGTH_LONG)
                                .show();
                        fileName = null;
                    } else {
                        Snackbar.make(findViewById(R.id.bt_chooseFile).getRootView(), R.string.ensure_mtz, Snackbar.LENGTH_LONG)
                                .show();
                    }

                    if (fileName == null) {
                        filePath = null;
                    } else {
                        // MIUI theme manager needs absolute path.
                        filePath = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Open system file manager app.
        Button openFileManagerButton = findViewById(R.id.bt_openFileManager);
        openFileManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName("com.android.fileexplorer", "com.android.fileexplorer.activity.FileActivity"));
            startActivity(intent);
        });

        // Choose a file and return to onActivityResult.
        Button chooseFileButton = findViewById(R.id.bt_chooseFile);
        chooseFileButton.setOnClickListener(v -> FileUtils.chooseFile(this));

        // Apply theme, should call after choose file.
        Button applyThemeButton = findViewById(R.id.bt_applyTheme);
        applyThemeButton.setOnClickListener(v -> {
            if (filePath == null) {
                Snackbar.make(v, R.string.no_Choose_File, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                ThemeUtils.applyTheme(this, filePath);
                filePath = null;
            }
        });

        TextInputLayout themeShareLinkTextInputLayout = findViewById(R.id.til_themeShareLink);

        // Get theme share link and get theme info to show.
        Button getThemeDownloadLinkButton = findViewById(R.id.bt_getThemeDownloadLink);
        getThemeDownloadLinkButton.setOnClickListener(v -> {
            String inputShareLink = themeShareLinkTextInputLayout.getEditText().getText().toString();
            if (!inputShareLink.isEmpty()) {
                ThemeUtils.getThemeDownloadLinkAsync(this, inputShareLink, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(() -> new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("错误")
                                .setMessage("获取直链失败，\n" +
                                        "请检查网络连接后重试.")
                                .setNegativeButton("OK", null)
                                .show());
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        ResponseBody body = response.body();
                        Map<String, String> themeInfo = ThemeUtils.getThemeInfo(body);

                        // Cannot get theme info, maybe link is wrong.
                        if (themeInfo == null) {
                            runOnUiThread(() -> new MaterialAlertDialogBuilder(MainActivity.this)
                                    .setTitle("失败")
                                    .setMessage("获取主题信息失败，\n" +
                                            "可能是链接输入错误.")
                                    .setNegativeButton("OK", null)
                                    .show());
                        } else {
                            String downloadUrl = themeInfo.get("downloadUrl");
                            String fileName = themeInfo.get("fileName");

                            // Show theme info, set copy and download button.
                            runOnUiThread(() -> new MaterialAlertDialogBuilder(MainActivity.this)
                                    .setTitle(fileName)
                                                .setMessage("文件大小：" + themeInfo.get("fileSize") + "\n\n" +
                                                        "下载链接：\n" + downloadUrl + "\n\n" +
                                                        "哈希值：\n" + themeInfo.get("fileHash") + "\n")
                                                .setNegativeButton("复制链接", (dialog, which) -> FileUtils.copyLink(MainActivity.this, downloadUrl))
                                                .setPositiveButton("直接下载", (dialog, which) -> FileUtils.systemDownload(MainActivity.this, themeInfo))
                                    .show());
                        }
                    }
                });
            }
        });

        // Go to IceBox Coolapk download page.
        Button iceBoxCoolapkButton = findViewById(R.id.bt_iceboxCoolapk);
        iceBoxCoolapkButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ICEBOX_COOLAPK_URL));
            startActivity(intent);
        });

        // Default example path.
        TextInputLayout themeFilePathTextInputLayout = findViewById(R.id.til_path);
        themeFilePathTextInputLayout.getEditText().setText("/sdcard/test.mtz");

        // Get user input path and apply theme.
        Button advApplyThemeButton = findViewById(R.id.bt_advAppleTheme);
        advApplyThemeButton.setOnClickListener(v -> {
            String input = themeFilePathTextInputLayout.getEditText().getText().toString();

            if (input.equals("") || !input.endsWith(".mtz")) {
                Snackbar.make(v, R.string.input_isEmpty, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                ThemeUtils.applyTheme(this, input);
            }
        });

        // Go to GitHub open source page.
        Button githubButton = findViewById(R.id.bt_github);
        githubButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
            startActivity(intent);
        });

        // Show how this app works.
        Button howItWorkButton = findViewById(R.id.bt_howItWork);
        howItWorkButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("[原理] 应用主题")
                        .setMessage("向 MIUI 主题商店发送 Intent 请求 \n" +
                                "在请求中指定特定的参数 \n" +
                                "\"theme_file_path\" -> 对应主题路径 \n" +
                                "\"api_called_from\" -> \"test\" \n" +
                                "之后发送请求即可 \n\n" +
                                "由于是测试接口，\n" +
                                "所以有可能会恢复默认，请见谅")
                .setPositiveButton("OK", (dialog, which) ->
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("[原理] 获取直链")
                                .setMessage("向 MIUI api 发送 get 请求 \n" +
                                        "在其中包含主题 Token \n" +
                                        "返回的 Json 中有下载链接 \n\n" +
                                        "例如：\n" +
                                        "http://zhuti.xiaomi.com/detail/f02025cb-8f0e-44e0-b39a-653e46d84d42 \n" +
                                        "的主题 Token 即为 \n" +
                                        "f02025cb-8f0e-44e0-b39a-653e46d84d42")
                                .setPositiveButton("OK", null)
                                .show())
                .show());

        // Go to my page in Coolapk.
        Button meCoolapkButton = findViewById(R.id.bt_meCoolapk);
        meCoolapkButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ME_COOLAPK_URL));
            startActivity(intent);
        });
    }
}
