package com.example.tp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

public class activity_langage extends BaseActivity {

    private RadioButton radioEnglish, radioArabic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_langage);

        radioEnglish = findViewById(R.id.radioEnglish);
        radioArabic  = findViewById(R.id.radioArabic);

        // Pre-select the currently saved language
        String current = LocaleHelper.getSavedLanguage(this);
        if ("ar".equals(current)) {
            radioArabic.setChecked(true);
            radioEnglish.setChecked(false);
        } else {
            radioEnglish.setChecked(true);
            radioArabic.setChecked(false);
        }

        RelativeLayout cardEnglish = findViewById(R.id.cardEnglish);
        RelativeLayout cardArabic  = findViewById(R.id.cardArabic);

        if (cardEnglish != null)
            cardEnglish.setOnClickListener(v -> {
                radioEnglish.setChecked(true);
                radioArabic.setChecked(false);
            });

        if (cardArabic != null)
            cardArabic.setOnClickListener(v -> {
                radioArabic.setChecked(true);
                radioEnglish.setChecked(false);
            });

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null)
            btnBack.setOnClickListener(v -> finish());

        AppCompatButton btnSave = findViewById(R.id.btnSaveLanguage);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String lang = radioArabic.isChecked() ? "ar" : "en";
                String prev = LocaleHelper.getSavedLanguage(this);

                LocaleHelper.persist(this, lang);

                if (!lang.equals(prev)) {
                    Toast.makeText(this,
                            "ar".equals(lang) ? getString(R.string.language_changed_ar) : getString(R.string.language_changed_en),
                            Toast.LENGTH_SHORT).show();

                    // Restart app from the root (login) so all Activities rebuild
                    Intent intent = new Intent(this, login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                }
            });
        }
    }
}