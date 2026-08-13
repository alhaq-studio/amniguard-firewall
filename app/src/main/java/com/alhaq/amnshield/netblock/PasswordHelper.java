package com.alhaq.amnshield.netblock;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHelper {
    private static final String PREF_PASSWORD_HASH = "password_hash";
    private static final String PREF_PASSWORD_APP_ENABLED = "password_app_enabled";
    private static final String PREF_PASSWORD_CHANGES_ENABLED = "password_changes_enabled";

    public static boolean isPasswordSet(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hash = prefs.getString(PREF_PASSWORD_HASH, null);
        return !TextUtils.isEmpty(hash);
    }

    public static boolean isAppPasswordEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_PASSWORD_APP_ENABLED, false);
    }

    public static boolean isChangesPasswordEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_PASSWORD_CHANGES_ENABLED, false);
    }

    public static void setAppPasswordEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(PREF_PASSWORD_APP_ENABLED, enabled).apply();
    }

    public static void setChangesPasswordEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(PREF_PASSWORD_CHANGES_ENABLED, enabled).apply();
    }

    public static void setPassword(Context context, String password) {
        String hash = hashPassword(password);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PREF_PASSWORD_HASH, hash).apply();
    }

    public static void removePassword(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .remove(PREF_PASSWORD_HASH)
                .putBoolean(PREF_PASSWORD_APP_ENABLED, false)
                .putBoolean(PREF_PASSWORD_CHANGES_ENABLED, false)
                .apply();
    }

    public static boolean verifyPassword(Context context, String password) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String storedHash = prefs.getString(PREF_PASSWORD_HASH, null);
        if (storedHash == null) return true; // No password set
        
        String inputHash = hashPassword(password);
        return storedHash.equals(inputHash);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
