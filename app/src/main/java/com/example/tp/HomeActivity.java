package com.example.tp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp.api.BookService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private View homeContent, settingsContent, favoritesContent, shopContent;
    private TextView tvToolbarTitle, tvTotalPrice;
    private TextView tvNotifBadge;          // red circle badge on bell

    private RecyclerView rvBooks, rvFavorites, rvShop;
    private BookAdapter  bookAdapter, favAdapter, shopAdapter;
    private final List<Book> bookList = new ArrayList<>();

    private boolean isViewAll = false;

    private FirebaseAuth      auth;
    private FirebaseFirestore db;

    private TextView tvUserFullnameSettings, navUserName, navUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        drawerLayout       = findViewById(R.id.drawer_layout);
        NavigationView nav = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        ImageButton btnMenu  = findViewById(R.id.btnMenu);
        tvToolbarTitle       = findViewById(R.id.tvToolbarTitle);
        tvNotifBadge         = findViewById(R.id.tvNotifBadge);

        homeContent      = findViewById(R.id.home_content);
        settingsContent  = findViewById(R.id.settings_content);
        favoritesContent = findViewById(R.id.favorites_content);
        shopContent      = findViewById(R.id.shop_content);

        rvBooks    = findViewById(R.id.rvBooks);
        rvFavorites = findViewById(R.id.rvFavorites);
        rvShop     = findViewById(R.id.rvShop);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        Button btnBuyNow = findViewById(R.id.btnBuyNow);

        // Browse adapter (no delete)
        bookAdapter = new BookAdapter(bookList);
        rvBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvBooks.setAdapter(bookAdapter);

        // Favorites adapter (with delete)
        favAdapter = new BookAdapter(DataManager.getInstance().getFavorites(), BookAdapter.Mode.FAVORITES);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(favAdapter);

        // Cart adapter (with delete)
        shopAdapter = new BookAdapter(DataManager.getInstance().getCart(), BookAdapter.Mode.CART);
        rvShop.setLayoutManager(new LinearLayoutManager(this));
        rvShop.setAdapter(shopAdapter);

        // Drawer header
        if (nav != null) {
            View header = nav.getHeaderView(0);
            navUserName  = header.findViewById(R.id.nav_user_name);
            navUserEmail = header.findViewById(R.id.nav_user_email);
        }
        tvUserFullnameSettings = findViewById(R.id.tvUserFullname);

        loadUserData();
        fetchPopularBooks();

        if (btnMenu != null)
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        findViewById(R.id.tvViewAll).setOnClickListener(v -> toggleViewAll());

        // Notification bell with badge
        ImageButton btnNotif = findViewById(R.id.btnNotification);
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                startActivity(new Intent(this, NotificationActivity.class));
            });
        }

        if (btnBuyNow != null) {
            btnBuyNow.setOnClickListener(v -> {
                if (DataManager.getInstance().getCart().isEmpty()) {
                    Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                } else {
                    sendCartInvoiceEmail();
                }
            });
        }

        setupSearchAndFilter();
        setupSettingsSection();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else if (isViewAll)
                    toggleViewAll();
                else if (homeContent.getVisibility() != View.VISIBLE) {
                    showSection(homeContent, "Home");
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)      showSection(homeContent, "Home");
            else if (id == R.id.nav_favorites) {
                showSection(favoritesContent, "Favorites");
                favAdapter.notifyDataSetChanged();
            } else if (id == R.id.nav_cart) {
                showSection(shopContent, "My Cart");
                updateCartTotal();
                shopAdapter.notifyDataSetChanged();
            } else if (id == R.id.nav_settings) showSection(settingsContent, "Settings");
            return true;
        });

        if (nav != null) {
            nav.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_profile)
                    startActivity(new Intent(this, ProfileDetailsActivity.class));
                else if (itemId == R.id.nav_settings_drawer) {
                    showSection(settingsContent, "Settings");
                    bottomNavigationView.setSelectedItemId(R.id.nav_settings);
                } else if (itemId == R.id.nav_favorites_drawer) {
                    showSection(favoritesContent, "Favorites");
                    bottomNavigationView.setSelectedItemId(R.id.nav_favorites);
                    favAdapter.notifyDataSetChanged();
                } else if (itemId == R.id.nav_orders) {
                    showSection(shopContent, "My Cart");
                    bottomNavigationView.setSelectedItemId(R.id.nav_cart);
                    updateCartTotal();
                    shopAdapter.notifyDataSetChanged();
                } else if (itemId == R.id.nav_share) {
                    shareApp();
                } else if (itemId == R.id.nav_contact) {
                    contactSupport();
                } else if (itemId == R.id.nav_logout) {
                    auth.signOut();
                    startActivity(new Intent(this, login.class));
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out Babelio, the best book app!");
        startActivity(Intent.createChooser(shareIntent, "Share Babelio via"));
    }

    private void contactSupport() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:bablio.isamm@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contact Us - Babelio");
        try {
            startActivity(Intent.createChooser(intent, "Send Email..."));
        } catch (Exception e) {
            Toast.makeText(this, "No email client found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendCartInvoiceEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userEmail = user.getEmail();
        String userName  = (navUserName != null) ? navUserName.getText().toString() : "Babelio User";

        List<Book> cartItems = new ArrayList<>(DataManager.getInstance().getCart());
        double subtotal = DataManager.getInstance().getTotalCartPrice();

        // Send confirmation email silently
        EmailSender.sendOrderConfirmation(userEmail, userName, cartItems, subtotal);

        // Clear cart locally & notify
        DataManager.getInstance().clearCart();
        updateCartTotal();
        shopAdapter.notifyDataSetChanged();
        NotificationActivity.addNotification("Order placed! Your facture has been sent to " + userEmail);
        Toast.makeText(this, "Order placed! Facture sent to your email.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh badge every time we return to home
        updateNotifBadge();
    }

    /* ── badge ─────────────────────────────────────────────── */
    private void updateNotifBadge() {
        if (tvNotifBadge == null) return;
        int count = NotificationActivity.getUnreadCount();
        if (count > 0) {
            tvNotifBadge.setVisibility(View.VISIBLE);
            tvNotifBadge.setText(count > 9 ? "9+" : String.valueOf(count));
        } else {
            tvNotifBadge.setVisibility(View.GONE);
        }
    }

    /* ── sections ───────────────────────────────────────────── */
    private void showSection(View section, String title) {
        homeContent.setVisibility(View.GONE);
        settingsContent.setVisibility(View.GONE);
        favoritesContent.setVisibility(View.GONE);
        shopContent.setVisibility(View.GONE);
        section.setVisibility(View.VISIBLE);
        tvToolbarTitle.setText(title);
        if (isViewAll && section != homeContent) isViewAll = false;
    }

    private void updateCartTotal() {
        double total = DataManager.getInstance().getTotalCartPrice();
        if (tvTotalPrice != null)
            tvTotalPrice.setText(String.format(Locale.getDefault(), "%.2f DT", total));
    }

    /* ── settings section wiring ───────────────────────────── */
    private void setupSettingsSection() {
        View itemProfile  = findViewById(R.id.itemProfile);
        View itemSecurity = findViewById(R.id.itemSecurity);
        View itemLanguage = findViewById(R.id.itemLanguage);
        View itemShare    = findViewById(R.id.itemShare);
        View itemContact  = findViewById(R.id.itemContactUs);
        View btnLogout    = findViewById(R.id.btnLogout);

        if (itemProfile != null)
            itemProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileDetailsActivity.class)));

        if (itemSecurity != null)
            itemSecurity.setOnClickListener(v ->
                    startActivity(new Intent(this, ChangePasswordActivity.class)));

        if (itemLanguage != null)
            itemLanguage.setOnClickListener(v ->
                    startActivity(new Intent(this, activity_langage.class)));

        if (itemShare != null)
            itemShare.setOnClickListener(v -> shareApp());

        if (itemContact != null)
            itemContact.setOnClickListener(v -> contactSupport());

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                android.content.SharedPreferences sp =
                        getSharedPreferences("sharedPrefs", MODE_PRIVATE);
                sp.edit().putBoolean("rememberMe", false).apply();
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    /* ── search & filter ───────────────────────────────────── */
    private void setupSearchAndFilter() {
        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) performSearch(query);
                else fetchPopularBooks();
                return true;
            });
        }

        ImageButton btnFilter = findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, btnFilter);
                popup.getMenu().add("A to Z");
                popup.getMenu().add("Price: Low to High");
                popup.setOnMenuItemClickListener(item -> {
                    if ("A to Z".equals(item.getTitle()))
                        Collections.sort(bookList, (b1, b2) ->
                                b1.getTitle().compareToIgnoreCase(b2.getTitle()));
                    else
                        Collections.sort(bookList, (b1, b2) ->
                                Double.compare(b1.getPrice(), b2.getPrice()));
                    bookAdapter.notifyDataSetChanged();
                    return true;
                });
                popup.show();
            });
        }
    }

    /* ── API calls ─────────────────────────────────────────── */
    private void fetchPopularBooks() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openlibrary.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofit.create(BookService.class)
                .searchBooks("popular", "rating")
                .enqueue(new Callback<SearchResult>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchResult> call,
                                           @NonNull Response<SearchResult> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            bookList.clear();
                            for (SearchResult.Doc doc : response.body().getDocs()) {
                                if (doc.getCoverI() != null) {
                                    String author = (doc.getAuthorName() != null
                                            && !doc.getAuthorName().isEmpty())
                                            ? doc.getAuthorName().get(0) : "Unknown Author";

                                    double rating = doc.getRatingsAverage() != null ? doc.getRatingsAverage() : 0.0;
                                    bookList.add(new Book(doc.getKey(), doc.getTitle(),
                                            author, doc.getCoverI(), rating));
                                }
                            }
                            bookAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<SearchResult> call, @NonNull Throwable t) {}
                });
    }

    private void performSearch(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openlibrary.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofit.create(BookService.class)
                .searchBooks(query, null)
                .enqueue(new Callback<SearchResult>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchResult> call,
                                           @NonNull Response<SearchResult> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            bookList.clear();
                            for (SearchResult.Doc doc : response.body().getDocs()) {
                                if (doc.getCoverI() != null) {
                                    String author = (doc.getAuthorName() != null
                                            && !doc.getAuthorName().isEmpty())
                                            ? doc.getAuthorName().get(0) : "Unknown Author";

                                    double rating = doc.getRatingsAverage() != null ? doc.getRatingsAverage() : 0.0;
                                    bookList.add(new Book(doc.getKey(), doc.getTitle(),
                                            author, doc.getCoverI(), rating));
                                }
                            }
                            bookAdapter.notifyDataSetChanged();
                            if (bookList.isEmpty()) {
                                Toast.makeText(HomeActivity.this, "No books found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<SearchResult> call, @NonNull Throwable t) {
                        Toast.makeText(HomeActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* ── view-all toggle ───────────────────────────────────── */
    private void toggleViewAll() {
        isViewAll = !isViewAll;
        if (isViewAll) {
            rvBooks.setLayoutManager(new GridLayoutManager(this, 2));
            findViewById(R.id.tvQuestion).setVisibility(View.GONE);
            findViewById(R.id.searchSection).setVisibility(View.GONE);
        } else {
            rvBooks.setLayoutManager(new LinearLayoutManager(
                    this, LinearLayoutManager.HORIZONTAL, false));
            findViewById(R.id.tvQuestion).setVisibility(View.VISIBLE);
            findViewById(R.id.searchSection).setVisibility(View.VISIBLE);
        }
    }

    /* ── user data ─────────────────────────────────────────── */
    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (navUserEmail != null) navUserEmail.setText(user.getEmail());
            db.collection("users").document(user.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String name = task.getResult().getString("fullname");
                            if (name != null) {
                                if (tvUserFullnameSettings != null)
                                    tvUserFullnameSettings.setText(name);
                                if (navUserName != null) navUserName.setText(name);
                            }
                        }
                    });
        }
    }
}
