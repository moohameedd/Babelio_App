package com.example.tp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * LocaleHelper
 * ────────────
 * Persists the user's chosen language ("ar" or "en") in SharedPreferences
 * and applies it to every Activity via attachBaseContext / applyLocale.
 *
 * Usage
 * ─────
 *  1. In every Activity, override attachBaseContext:
 *
 *       @Override
 *       protected void attachBaseContext(Context base) {
 *           super.attachBaseContext(LocaleHelper.wrap(base));
 *       }
 *
 *  2. In activity_langage (language chooser), save then restart:
 *
 *       LocaleHelper.setLocale(this, "ar"); // or "en"
 *       // Then recreate the root activity
 *
 *  3. In Application.onCreate (optional but recommended):
 *
 *       LocaleHelper.applyLocale(this);
 */
public class LocaleHelper {

    private static final String PREFS_NAME = "sharedPrefs";
    private static final String KEY_LANG   = "app_language";
    private static final String DEFAULT    = "en";

    /** Save a language code and return a context wrapped with that locale. */
    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return wrap(context);
    }

    /** Wrap a context with the saved locale (call from attachBaseContext). */
    public static Context wrap(Context context) {
        String language = getSavedLanguage(context);
        Locale locale   = new Locale(language);
        Locale.setDefault(locale);

        Resources res    = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            // RTL layout direction for Arabic
            config.setLayoutDirection(locale);
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }

    /**
     * Quick apply for activities that don't override attachBaseContext.
     * Call this at the top of onCreate(), BEFORE setContentView().
     */
    public static void applyLocale(Context context) {
        wrap(context);
    }

    /** Returns the persisted language code, defaulting to "en". */
    public static String getSavedLanguage(Context context) {
        return context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANG, DEFAULT);
    }

    /** Persists the chosen language code. */
    public static void persist(Context context, String language) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANG, language)
                .apply();
    }

    /** Returns true when Arabic is currently active. */
    public static boolean isArabic(Context context) {
        return "ar".equals(getSavedLanguage(context));
    }
}