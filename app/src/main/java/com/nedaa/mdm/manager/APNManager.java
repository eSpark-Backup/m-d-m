package com.nedaa.mdm.manager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

public class APNManager {
    private static final String TAG = "APNManager";
    private static final String LEBARA_APN = "lebara";

    public static boolean setDefaultAPN(Context context) {
        try {
            ContentResolver resolver = context.getContentResolver();

            Cursor cursor = resolver.query(
                    Telephony.Carriers.CONTENT_URI,
                    null,
                    Telephony.Carriers.APN + "=?",
                    new String[]{LEBARA_APN},
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(Telephony.Carriers._ID);
                if (columnIndex != -1) {
                    String apnId = cursor.getString(columnIndex);

                    ContentValues values = new ContentValues();
                    values.put(Telephony.Carriers.CURRENT, "1");
                    resolver.update(
                            Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, apnId),
                            values,
                            null,
                            null
                    );

                    cursor.close();
                } else {
                    resolver.insert(Telephony.Carriers.CONTENT_URI, createNewApn());
                    Log.e("NEW APN", "Inserted");
                }
            } else {
                resolver.insert(Telephony.Carriers.CONTENT_URI, createNewApn());
                Log.e("NEW APN", "Inserted");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error setting default APN", e);
            return false;
        }
    }

    private static ContentValues createNewApn(){
        ContentValues values = new ContentValues();
        values.put(Telephony.Carriers.NAME, "Lebara");
        values.put(Telephony.Carriers.APN, LEBARA_APN);
        values.put(Telephony.Carriers.CURRENT, "1");
        values.put(Telephony.Carriers.TYPE, "default,supl");
        values.put(Telephony.Carriers.MCC, "234"); // UK MCC
        values.put(Telephony.Carriers.MNC, "15");  // Lebara MNC
        return values;
    }

    public static String getCurrentAPN(Context context) {
        Cursor cursor = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            cursor = resolver.query(
                    Telephony.Carriers.CONTENT_URI,
                    new String[]{Telephony.Carriers.APN},
                    Telephony.Carriers.CURRENT + "=1",
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int apnIndex = cursor.getColumnIndex(Telephony.Carriers.APN);
                if (apnIndex != -1) {
                    return cursor.getString(apnIndex);
                } else {
                    Log.e(TAG, "APN column not found in cursor");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting current APN", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}