package com.example.tp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class activity_langage extends AppCompatActivity {


    private RadioButton radioEnglish, radioArabic;
    private RelativeLayout cardEnglish, cardArabic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //link to activity to the langage.xml
        setContentView(R.layout.activity_langage);

        ImageButton btnBack = findViewById(R.id.btnBack);
        AppCompatButton btnSaveLanguage = findViewById(R.id.btnSaveLanguage);

        radioEnglish = findViewById(R.id.radioEnglish);
        radioArabic = findViewById(R.id.radioArabic);

        cardEnglish = findViewById(R.id.cardEnglish);
        cardArabic = findViewById(R.id.cardArabic);


        cardEnglish.setOnClickListener(v -> selectEnglish());
        radioEnglish.setOnClickListener(v -> selectEnglish());


        cardArabic.setOnClickListener(v -> selectArabic());
        radioArabic.setOnClickListener(v -> selectArabic());

        // Back Button
        btnBack.setOnClickListener(v -> finish());

        //save Button:
        btnSaveLanguage.setOnClickListener(v -> {

            finish();
        });
    }


     /* show Engas selected and Arabe as unselected*/

    private void selectEnglish() {
        radioEnglish.setChecked(true);
        radioArabic.setChecked(false);
    }


     /*show arabe as selected and eng as unselected */

    private void selectArabic() {
        radioArabic.setChecked(true);
        radioEnglish.setChecked(false);
    }
}