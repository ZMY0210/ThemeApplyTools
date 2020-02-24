package org.hydev.themeapplytools;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

class ThemeUtils {
    static void chooseFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.startActivityForResult(intent, 7);
    }

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
}
