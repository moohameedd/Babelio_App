package com.example.tp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tp.api.BookService;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BookDetailsActivity extends AppCompatActivity {

    private ImageView   ivCover;
    private TextView    tvTitle, tvAuthor, tvPrice, tvDescription, tvRatingValue;
    private RatingBar   ratingBar;
    private Button      btnAddFavorite, btnAddShop;
    private Book        book;

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
            tvPrice.setText(String.format("%.2f DT", book.getPrice()));

            // Show existing rating
            ratingBar.setRating(book.getUserRating());
            updateRatingLabel();

            if (book.getCoverUrl() != null)
                Picasso.get().load(book.getCoverUrl()).into(ivCover);

            fetchBookDetails(book.getKey());
        }

        if (findViewById(R.id.btnBackDetails) != null)
            findViewById(R.id.btnBackDetails).setOnClickListener(v -> finish());

        // User submits a rating
        ratingBar.setOnRatingBarChangeListener((rb, stars, fromUser) -> {
            if (fromUser && book != null) {
                book.addRating(stars);
                updateRatingLabel();
                Toast.makeText(this, "Thanks for rating!", Toast.LENGTH_SHORT).show();
            }
        });

        // Favourite toggle
        btnAddFavorite.setOnClickListener(v -> {
            boolean alreadyFav = DataManager.getInstance().getFavorites().contains(book);
            if (alreadyFav) {
                DataManager.getInstance().removeFavorite(book);
                btnAddFavorite.setText("Add to Favorites");
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            } else {
                DataManager.getInstance().addFavorite(book);
                btnAddFavorite.setText("★ Favorited");
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
            }
        });

        // Cart toggle
        btnAddShop.setOnClickListener(v -> {
            boolean inCart = DataManager.getInstance().getCart().contains(book);
            if (inCart) {
                DataManager.getInstance().removeFromCart(book);
                btnAddShop.setText("Add to Cart");
                Toast.makeText(this, "Removed from Cart", Toast.LENGTH_SHORT).show();
            } else {
                DataManager.getInstance().addToCart(book);
                btnAddShop.setText("✓ In Cart");
                Toast.makeText(this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        // Sync button labels on open
        if (DataManager.getInstance().getFavorites().contains(book))
            btnAddFavorite.setText("★ Favorited");
        if (DataManager.getInstance().getCart().contains(book))
            btnAddShop.setText("✓ In Cart");
    }

    private void updateRatingLabel() {
        if (tvRatingValue == null || book == null) return;
        if (book.getRatingCount() > 0) {
            tvRatingValue.setText(String.format("%.1f / 5  (%d ratings)",
                    book.getUserRating(), book.getRatingCount()));
        } else {
            tvRatingValue.setText("No ratings yet – be the first!");
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
                                String description = "No description available.";
                                if (obj.has("description")) {
                                    Object descObj = obj.get("description");
                                    description = (descObj instanceof JSONObject)
                                            ? ((JSONObject) descObj).getString("value")
                                            : descObj.toString();
                                }
                                tvDescription.setText(description);
                            }
                        } catch (Exception e) {
                            tvDescription.setText("Error loading description.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        tvDescription.setText("Failed to fetch details.");
                    }
                });
    }
}
