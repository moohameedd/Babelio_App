package com.example.tp;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity
 * ────────────
 * Every Activity in the app should extend BaseActivity instead of AppCompatActivity.
 * This ensures the locale (Arabic / English) is applied before each screen inflates.
 *
 * MIGRATION — replace in every Activity file:
 *   public class XxxActivity extends AppCompatActivity {
 * with:
 *   public class XxxActivity extends BaseActivity {
 *
 * Activities already updated:
 *   - HomeActivity
 *   - activity_langage
 *   - SettingsActivity
 *
 * Still needs updating (search & replace in your IDE):
 *   - login
 *   - signup / RegisterActivity
 *   - ProfileDetailsActivity
 *   - ChangePasswordActivity
 *   - ForgotPasswordActivity
 *   - BookDetailsActivity
 *   - NotificationActivity
 *   - SplashActivity / MainActivity
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        // Wrap the base context with the saved locale before the Activity inflates
        super.attachBaseContext(LocaleHelper.wrap(base));
    }
}