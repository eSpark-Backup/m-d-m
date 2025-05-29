package com.nedaa.mdm.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppManager {
    private static final String TAG = "AppManager";
    private static final String NEDAA_PACKAGE = "com.nedaa";

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getAppVersion(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean installApp(Context context, String apkUrl) {
        try {
            File apkFile = downloadApk(context, apkUrl);
            if (apkFile == null) {
                return false;
            }

            // Install the APK
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        apkFile
                );
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }

            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error installing app", e);
            return false;
        }
    }

    public static boolean updateApp(Context context, String apkUrl) {
        // First uninstall the existing app
        if (isAppInstalled(context, NEDAA_PACKAGE)) {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + NEDAA_PACKAGE));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        // Then install the new version
        return installApp(context, apkUrl);
    }

    public static boolean rollbackApp(Context context, String previousVersionUrl) {
        return installApp(context, previousVersionUrl);
    }

    private static File downloadApk(Context context, String apkUrl) {
        try {
            URL url = new URL(apkUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // Create a temporary file
            File outputFile = new File(context.getCacheDir(), "temp.apk");
            FileOutputStream fos = new FileOutputStream(outputFile);

            // Download the file
            InputStream input = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            input.close();

            return outputFile;
        } catch (IOException e) {
            Log.e(TAG, "Error downloading APK", e);
            return null;
        }
    }
}