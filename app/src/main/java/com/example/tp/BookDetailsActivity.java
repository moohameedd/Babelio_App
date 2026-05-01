package com.example.tp;

import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tp.api.BookService;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BookDetailsActivity extends BaseActivity {

    private ImageView   ivCover;
    private TextView    tvTitle, tvAuthor, tvPrice, tvDescription, tvRatingValue;
    private RatingBar   ratingBar;
    private Button      btnAddFavorite, btnAddShop;
    private Book        book;
    
    private static final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        ivCover        = findViewById(R.id.ivBookCoverLarge);
        tvTitle        = findViewById(R.id.tvBookTitleDetails);
        tvAuthor       = findViewById(R.id.tvBookAuthorDetails);
        tvPrice        = findViewById(R.id.tvBookPriceDetails);
        tvDescription  = findViewById(R.id.tvBookDescription);
        tvRatingValue  = findViewById(R.id.tvRatingValue);
        ratingBar      = findViewById(R.id.ratingBarDetails);
        btnAddFavorite = findViewById(R.id.btnAddFavorite);
        btnAddShop     = findViewById(R.id.btnAddShop);

        book = (Book) getIntent().getSerializableExtra("book");

        if (book != null) {
            tvTitle.setText(book.getTitle());
            tvAuthor.setText(book.getAuthor());
            tvPrice.setText(String.format(Locale.getDefault(), getString(R.string.currency_format), book.getPrice()));

            ratingBar.setRating(book.getUserRating());
            updateRatingLabel();

            if (book.getCoverUrl() != null)
                Picasso.get().load(book.getCoverUrl()).placeholder(android.R.drawable.ic_menu_gallery).into(ivCover);

            fetchBookDetails(book.getKey());
        }

        if (findViewById(R.id.btnBackDetails) != null)
            findViewById(R.id.btnBackDetails).setOnClickListener(v -> finish());

        ratingBar.setOnRatingBarChangeListener((rb, stars, fromUser) -> {
            if (fromUser && book != null) {
                book.addRating(stars);
                updateRatingLabel();
            }
        });

        btnAddFavorite.setOnClickListener(v -> {
            boolean alreadyFav = DataManager.getInstance().getFavorites().contains(book);
            if (alreadyFav) {
                DataManager.getInstance().removeFavorite(book);
                btnAddFavorite.setText(getString(R.string.add_to_favorites));
            } else {
                DataManager.getInstance().addFavorite(book);
                btnAddFavorite.setText("★ " + getString(R.string.favorites));
                NotificationActivity.addNotification(getString(R.string.notif_added_fav, book.getTitle()));
            }
        });

        btnAddShop.setOnClickListener(v -> {
            boolean inCart = DataManager.getInstance().getCart().contains(book);
            if (inCart) {
                DataManager.getInstance().removeFromCart(book);
                btnAddShop.setText(getString(R.string.add_to_cart));
            } else {
                DataManager.getInstance().addToCart(book);
                btnAddShop.setText("✓ " + getString(R.string.my_cart));
                Toast.makeText(this, getString(R.string.added_to_cart), Toast.LENGTH_SHORT).show();
                NotificationActivity.addNotification(getString(R.string.notif_added_cart, book.getTitle()));
            }
        });

        if (DataManager.getInstance().getFavorites().contains(book))
            btnAddFavorite.setText("★ " + getString(R.string.favorites));
        if (DataManager.getInstance().getCart().contains(book))
            btnAddShop.setText("✓ " + getString(R.string.my_cart));
    }

    private void updateRatingLabel() {
        if (tvRatingValue == null || book == null) return;
        if (book.getRatingCount() > 0) {
            tvRatingValue.setText(getString(R.string.rating_format, book.getUserRating(), book.getRatingCount()));
        } else {
            tvRatingValue.setText(getString(R.string.no_ratings_yet));
        }
    }

    private void fetchBookDetails(String workId) {
        if (workId == null) return;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openlibrary.org/")
                .build();

        BookService service = retrofit.create(BookService.class);
        service.getBookDetailsRaw("https://openlibrary.org" + workId + ".json")
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String json = response.body().string();
                                JSONObject obj = new JSONObject(json);
                                String description = getString(R.string.no_description);
                                if (obj.has("description")) {
                                    Object descObj = obj.get("description");
                                    description = (descObj instanceof JSONObject)
                                            ? ((JSONObject) descObj).getString("value")
                                            : descObj.toString();
                                }
                                
                                if (LocaleHelper.isArabic(BookDetailsActivity.this)) {
                                    translateToArabic(description);
                                } else {
                                    tvDescription.setText(description);
                                }
                            }
                        } catch (Exception e) {
                            tvDescription.setText(getString(R.string.no_description));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        tvDescription.setText(getString(R.string.no_description));
                    }
                });
    }

    private void translateToArabic(String text) {
        if (text == null || text.trim().isEmpty() || text.equals(getString(R.string.no_description))) {
            runOnUiThread(() -> tvDescription.setText(text));
            return;
        }

        runOnUiThread(() -> tvDescription.setText(getString(R.string.loading)));

        // Truncate to avoid API limits (Free MyMemory tier handles roughly 1000 chars)
        String textToTranslate = text.length() > 900 ? text.substring(0, 900) : text;
        
        RequestBody formBody = new FormBody.Builder()
                .add("q", textToTranslate)
                .add("langpair", "en|ar")
                .build();

        Request request = new Request.Builder()
                .url("https://api.mymemory.translated.net/get")
                .post(formBody)
                .build();
        
        new Thread(() -> {
            try {
                okhttp3.Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    JSONObject obj = new JSONObject(json);
                    String translated = obj.getJSONObject("responseData").getString("translatedText");
                    
                    final String finalText;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        finalText = Html.fromHtml(translated, Html.FROM_HTML_MODE_LEGACY).toString();
                    } else {
                        finalText = Html.fromHtml(translated).toString();
                    }
                    
                    runOnUiThread(() -> tvDescription.setText(finalText));
                } else {
                    runOnUiThread(() -> tvDescription.setText(text)); 
                }
            } catch (Exception e) {
                runOnUiThread(() -> tvDescription.setText(text));
            }
        }).start();
    }
}