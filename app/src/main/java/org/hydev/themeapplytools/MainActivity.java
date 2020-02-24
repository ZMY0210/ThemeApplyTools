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

public class MainActivity extends AppCompatActivity {
    private static final String GITHUB_URL = "https://github.com/VergeDX/ThemeApplyTools";
    private static final String ME_COOLAPK_URL = "https://coolapk.com/u/506843";

    private static final int snackBarLocation = R.id.sb_location;
    static boolean applied = false;
    private static String filePath;

    @Override
    protected void onResume() {
        super.onResume();

        if (applied) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("完成")
                    .setMessage("主题已应用，\n" +
                            "若没有效果，可能是该主题不在根目录下，\n" +
                            "请仔细阅读说明.")
                    .setPositiveButton("好的", null)
                    .show();

            applied = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 7 && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            Cursor cursor = getContentResolver().query(fileUri, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                if (!fileName.endsWith(".mtz")) {
                    Snackbar.make(findViewById(R.id.bt_chooseFile).getRootView(), R.string.not_mtz_file, Snackbar.LENGTH_LONG)
                            .setAnchorView(snackBarLocation)
                            .show();
                    fileName = null;
                } else {
                    Snackbar.make(findViewById(R.id.bt_chooseFile).getRootView(), R.string.ensure_mtz, Snackbar.LENGTH_LONG)
                            .setAnchorView(snackBarLocation)
                            .show();
                }

                if (fileName == null) {
                    filePath = null;
                } else {
                    filePath = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openFileManagerButton = findViewById(R.id.bt_openFileManager);
        openFileManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName("com.android.fileexplorer", "com.android.fileexplorer.activity.FileActivity"));
            startActivity(intent);
        });

        Button chooseFileButton = findViewById(R.id.bt_chooseFile);
        chooseFileButton.setOnClickListener(v -> ThemeUtils.chooseFile(this));

        Button applyThemeButton = findViewById(R.id.bt_applyTheme);
        applyThemeButton.setOnClickListener(v -> {
            if (filePath == null) {
                Snackbar.make(v, R.string.no_Choose_File, Snackbar.LENGTH_LONG)
                        .setAnchorView(snackBarLocation)
                        .show();
            } else {
                ThemeUtils.applyTheme(this, filePath);
                filePath = null;
            }
        });

        TextInputLayout textInputLayout = findViewById(R.id.til_path);
        textInputLayout.getEditText().setText("/sdcard/test.mtz");

        Button advApplyThemeButton = findViewById(R.id.bt_advAppleTheme);
        advApplyThemeButton.setOnClickListener(v -> {
            String input = textInputLayout.getEditText().getText().toString();

            if (input.equals("") || !input.endsWith(".mtz")) {
                Snackbar.make(v, R.string.input_isEmpty, Snackbar.LENGTH_LONG)
                        .setAnchorView(snackBarLocation)
                        .show();
            } else {
                ThemeUtils.applyTheme(this, input);
            }
        });

        Button githubButton = findViewById(R.id.bt_github);
        githubButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
            startActivity(intent);
        });

        Button howItWorkButton = findViewById(R.id.bt_howItWork);
        howItWorkButton.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("原理")
                        .setMessage("向 MIUI 主题商店发送 Intent 请求 \n" +
                                "在请求中指定特定的参数 \n" +
                                "\"theme_file_path\" -> 对应主题路径 \n" +
                                "\"api_called_from\" -> \"test\" \n" +
                                "之后发送请求即可")
                        .setPositiveButton("返回", null)
                        .show()
        );

        Button meCoolapkButton = findViewById(R.id.bt_meCoolapk);
        meCoolapkButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ME_COOLAPK_URL));
            startActivity(intent);
        });
    }
}
