package com.example.tp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
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
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private View homeContent, settingsContent, favoritesContent, shopContent;
    private TextView tvToolbarTitle, tvTotalPrice;
    private TextView tvNotifBadge;

    private RecyclerView rvBooks, rvFavorites, rvShop;
    private BookAdapter  bookAdapter, favAdapter, shopAdapter;

    private final List<Book> popularBooks = new ArrayList<>();
    private final List<Book> bookList = new ArrayList<>();

    private boolean isViewAll = false;

    private final Handler  searchHandler  = new Handler(Looper.getMainLooper());
    private       Runnable searchRunnable;

    private FirebaseAuth      auth;
    private FirebaseFirestore db;

    private TextView tvUserFullnameSettings, navUserName, navUserEmail;

    // Optimized fields to request from OpenLibrary to speed up the response
    private static final String SEARCH_FIELDS = "key,title,author_name,cover_i,ratings_average";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        drawerLayout         = findViewById(R.id.drawer_layout);
        NavigationView nav   = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        ImageButton btnMenu  = findViewById(R.id.btnMenu);
        tvToolbarTitle       = findViewById(R.id.tvToolbarTitle);
        tvNotifBadge         = findViewById(R.id.tvNotifBadge);

        homeContent      = findViewById(R.id.home_content);
        settingsContent  = findViewById(R.id.settings_content);
        favoritesContent = findViewById(R.id.favorites_content);
        shopContent      = findViewById(R.id.shop_content);

        rvBooks      = findViewById(R.id.rvBooks);
        rvFavorites  = findViewById(R.id.rvFavorites);
        rvShop       = findViewById(R.id.rvShop);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        Button btnBuyNow = findViewById(R.id.btnBuyNow);

        bookAdapter = new BookAdapter(bookList);
        rvBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvBooks.setAdapter(bookAdapter);

        favAdapter = new BookAdapter(DataManager.getInstance().getFavorites(), BookAdapter.Mode.FAVORITES);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(favAdapter);

        shopAdapter = new BookAdapter(DataManager.getInstance().getCart(), BookAdapter.Mode.CART);
        rvShop.setLayoutManager(new LinearLayoutManager(this));
        rvShop.setAdapter(shopAdapter);

        if (nav != null) {
            View header  = nav.getHeaderView(0);
            navUserName  = header.findViewById(R.id.nav_user_name);
            navUserEmail = header.findViewById(R.id.nav_user_email);
        }
        tvUserFullnameSettings = findViewById(R.id.tvUserFullname);

        loadUserData();
        fetchPopularBooks();

        if (btnMenu != null)
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        findViewById(R.id.tvViewAll).setOnClickListener(v -> toggleViewAll());

        ImageButton btnNotif = findViewById(R.id.btnNotification);
        if (btnNotif != null)
            btnNotif.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));

        if (btnBuyNow != null)
            btnBuyNow.setOnClickListener(v -> {
                if (DataManager.getInstance().getCart().isEmpty())
                    Toast.makeText(this, getString(R.string.cart_empty), Toast.LENGTH_SHORT).show();
                else sendCartInvoiceEmail();
            });

        setupSearchAndFilter();
        setupSettingsSection();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else if (isViewAll) toggleViewAll();
                else if (homeContent.getVisibility() != View.VISIBLE) {
                    showSection(homeContent, getString(R.string.home));
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)           showSection(homeContent, getString(R.string.home));
            else if (id == R.id.nav_favorites) { showSection(favoritesContent, getString(R.string.favorites)); favAdapter.notifyDataSetChanged(); }
            else if (id == R.id.nav_cart)      { showSection(shopContent, getString(R.string.my_cart)); updateCartTotal(); shopAdapter.notifyDataSetChanged(); }
            else if (id == R.id.nav_settings)    showSection(settingsContent, getString(R.string.settings));
            return true;
        });

        if (nav != null) {
            nav.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_profile)
                    startActivity(new Intent(this, ProfileDetailsActivity.class));
                else if (itemId == R.id.nav_settings_drawer) { showSection(settingsContent, getString(R.string.settings)); bottomNavigationView.setSelectedItemId(R.id.nav_settings); }
                else if (itemId == R.id.nav_favorites_drawer) { showSection(favoritesContent, getString(R.string.favorites)); bottomNavigationView.setSelectedItemId(R.id.nav_favorites); favAdapter.notifyDataSetChanged(); }
                else if (itemId == R.id.nav_orders)           { showSection(shopContent, getString(R.string.my_cart)); bottomNavigationView.setSelectedItemId(R.id.nav_cart); updateCartTotal(); shopAdapter.notifyDataSetChanged(); }
                else if (itemId == R.id.nav_share)   shareApp();
                else if (itemId == R.id.nav_contact) contactSupport();
                else if (itemId == R.id.nav_logout)  { auth.signOut(); startActivity(new Intent(this, login.class)); finish(); }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void setupSearchAndFilter() {
        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                if (query.isEmpty()) {
                    bookList.clear();
                    bookList.addAll(popularBooks);
                    bookAdapter.notifyDataSetChanged();
                    return;
                }

                searchRunnable = () -> performSearch(query);
                searchHandler.postDelayed(searchRunnable, 600);
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) performSearch(query);
            else {
                bookList.clear();
                bookList.addAll(popularBooks);
                bookAdapter.notifyDataSetChanged();
            }
            return true;
        });

        ImageButton btnFilter = findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, btnFilter);
                popup.getMenu().add(getString(R.string.sort_a_z));
                popup.getMenu().add(getString(R.string.sort_price));
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals(getString(R.string.sort_a_z)))
                        Collections.sort(bookList, (b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()));
                    else
                        Collections.sort(bookList, (b1, b2) -> Double.compare(b1.getPrice(), b2.getPrice()));
                    bookAdapter.notifyDataSetChanged();
                    return true;
                });
                popup.show();
            });
        }
    }

    private Retrofit buildRetrofit() {
        // Increase timeouts to prevent "Search failed" on slow connections
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl("https://openlibrary.org/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void fetchPopularBooks() {
        buildRetrofit().create(BookService.class).searchBooks("popular", "rating", 20, SEARCH_FIELDS)
                .enqueue(new Callback<SearchResult>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchResult> call, @NonNull Response<SearchResult> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().getDocs() != null) {
                            popularBooks.clear();
                            for (SearchResult.Doc doc : r.body().getDocs()) {
                                String author = (doc.getAuthorName() != null && !doc.getAuthorName().isEmpty())
                                        ? doc.getAuthorName().get(0) : getString(R.string.unknown_author);
                                double rating = doc.getRatingsAverage() != null ? doc.getRatingsAverage() : 0.0;
                                Book b = new Book(doc.getKey(), doc.getTitle(), author, doc.getCoverI(), rating);
                                popularBooks.add(b);
                            }
                            EditText etSearch = findViewById(R.id.etSearch);
                            if (etSearch != null && etSearch.getText().toString().trim().isEmpty()) {
                                bookList.clear();
                                bookList.addAll(popularBooks);
                                bookAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    @Override public void onFailure(@NonNull Call<SearchResult> c, @NonNull Throwable t) {}
                });
    }

    private void performSearch(String query) {
        buildRetrofit().create(BookService.class).searchBooks(query, null, 20, SEARCH_FIELDS)
                .enqueue(new Callback<SearchResult>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchResult> call, @NonNull Response<SearchResult> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().getDocs() != null) {
                            bookList.clear();
                            for (SearchResult.Doc doc : r.body().getDocs()) {
                                String author = (doc.getAuthorName() != null && !doc.getAuthorName().isEmpty())
                                        ? doc.getAuthorName().get(0) : getString(R.string.unknown_author);
                                double rating = doc.getRatingsAverage() != null ? doc.getRatingsAverage() : 0.0;
                                Book b = new Book(doc.getKey(), doc.getTitle(), author, doc.getCoverI(), rating);
                                bookList.add(b);
                            }
                            bookAdapter.notifyDataSetChanged();
                            if (bookList.isEmpty())
                                Toast.makeText(HomeActivity.this, getString(R.string.no_books_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HomeActivity.this, getString(R.string.search_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<SearchResult> c, @NonNull Throwable t) {
                        Toast.makeText(HomeActivity.this, getString(R.string.search_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

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
            tvTotalPrice.setText(String.format(Locale.getDefault(), getString(R.string.currency_format), total));
    }

    private void toggleViewAll() {
        isViewAll = !isViewAll;
        if (isViewAll) {
            rvBooks.setLayoutManager(new GridLayoutManager(this, 2));
            findViewById(R.id.tvQuestion).setVisibility(View.GONE);
            findViewById(R.id.searchSection).setVisibility(View.GONE);
        } else {
            rvBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            findViewById(R.id.tvQuestion).setVisibility(View.VISIBLE);
            findViewById(R.id.searchSection).setVisibility(View.VISIBLE);
        }
    }

    private void setupSettingsSection() {
        View itemProfile  = findViewById(R.id.itemProfile);
        View itemSecurity = findViewById(R.id.itemSecurity);
        View itemLanguage = findViewById(R.id.itemLanguage);
        View itemShare    = findViewById(R.id.itemShare);
        View itemContact  = findViewById(R.id.itemContactUs);
        View btnLogout    = findViewById(R.id.btnLogout);

        if (itemProfile  != null) itemProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileDetailsActivity.class)));
        if (itemSecurity != null) itemSecurity.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
        if (itemLanguage != null) itemLanguage.setOnClickListener(v -> startActivity(new Intent(this, activity_langage.class)));
        if (itemShare    != null) itemShare.setOnClickListener(v -> shareApp());
        if (itemContact  != null) itemContact.setOnClickListener(v -> contactSupport());
        if (btnLogout    != null) btnLogout.setOnClickListener(v -> {
            auth.signOut();
            getSharedPreferences("sharedPrefs", MODE_PRIVATE).edit().putBoolean("rememberMe", false).apply();
            Toast.makeText(this, getString(R.string.logged_out), Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, login.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i); finish();
        });
    }

    private void shareApp() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        startActivity(Intent.createChooser(i, getString(R.string.share_chooser_title)));
    }

    private void contactSupport() {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:bablio.isamm@gmail.com"));
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_subject));
        try { startActivity(Intent.createChooser(i, getString(R.string.nav_contact))); }
        catch (Exception e) { Toast.makeText(this, getString(R.string.no_email_client), Toast.LENGTH_SHORT).show(); }
    }

    private void sendCartInvoiceEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        String userEmail = user.getEmail();
        String userName  = (navUserName != null) ? navUserName.getText().toString() : getString(R.string.default_user_name);
        List<Book> cartItems = new ArrayList<>(DataManager.getInstance().getCart());
        double subtotal = DataManager.getInstance().getTotalCartPrice();
        EmailSender.sendOrderConfirmation(userEmail, userName, cartItems, subtotal);
        DataManager.getInstance().clearCart();
        updateCartTotal();
        shopAdapter.notifyDataSetChanged();
        String notifMsg = String.format(getString(R.string.order_placed_notif), userEmail);
        NotificationActivity.addNotification(notifMsg);
        Toast.makeText(this, getString(R.string.order_placed), Toast.LENGTH_LONG).show();
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        if (navUserEmail != null) navUserEmail.setText(user.getEmail());
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String name = task.getResult().getString("fullname");
                        if (name != null) {
                            if (tvUserFullnameSettings != null) tvUserFullnameSettings.setText(name);
                            if (navUserName != null) navUserName.setText(name);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotifBadge();
    }

    private void updateNotifBadge() {
        if (tvNotifBadge == null) return;
        int count = NotificationActivity.getUnreadCount();
        if (count > 0) { tvNotifBadge.setVisibility(View.VISIBLE); tvNotifBadge.setText(count > 9 ? "9+" : String.valueOf(count)); }
        else tvNotifBadge.setVisibility(View.GONE);
    }
}