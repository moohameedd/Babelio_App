package com.example.tp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public enum Mode { BROWSE, FAVORITES, CART }

    private final List<Book> bookList;
    private final Mode mode;
    private OnItemRemovedListener removalListener;

    public interface OnItemRemovedListener {
        void onItemRemoved();
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.removalListener = listener;
    }

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
        this.mode = Mode.BROWSE;
    }

    public BookAdapter(List<Book> bookList, Mode mode) {
        this.bookList = bookList;
        this.mode = mode;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        
        // Fully localized currency
        String priceText = holder.itemView.getContext().getString(R.string.currency_format, book.getPrice());
        holder.tvPrice.setText(priceText);

        float avg   = book.getUserRating();
        int   count = book.getRatingCount();

        if (holder.ratingBar != null) {
            holder.ratingBar.setRating(avg);
        }
        if (holder.tvRatingCount != null) {
            if (count > 0) {
                holder.tvRatingCount.setVisibility(View.VISIBLE);
                // Fully localized rating format
                String ratingText = holder.itemView.getContext().getString(R.string.rating_format, avg, count);
                holder.tvRatingCount.setText(ratingText);
            } else {
                holder.tvRatingCount.setVisibility(View.GONE);
            }
        }

        Picasso.get()
                .load(book.getCoverUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivCover);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BookDetailsActivity.class);
            intent.putExtra("book", book);
            v.getContext().startActivity(intent);
        });

        if (holder.btnDelete != null) {
            if (mode == Mode.FAVORITES || mode == Mode.CART) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> {
                    v.setEnabled(false); 
                    int pos = holder.getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;

                    if (mode == Mode.FAVORITES)
                        DataManager.getInstance().removeFavoriteAt(pos);
                    else
                        DataManager.getInstance().removeFromCartAt(pos);
                    
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, bookList.size());
                    
                    if (removalListener != null) {
                        removalListener.onItemRemoved();
                    }
                    v.setEnabled(true);
                });
            } else {
                holder.btnDelete.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() { return bookList.size(); }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView    tvTitle, tvAuthor, tvPrice, tvRatingCount;
        ImageView   ivCover;
        RatingBar   ratingBar;
        ImageButton btnDelete;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle       = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor      = itemView.findViewById(R.id.tvBookAuthor);
            tvPrice       = itemView.findViewById(R.id.tvBookPrice);
            ivCover       = itemView.findViewById(R.id.ivBookCover);
            ratingBar     = itemView.findViewById(R.id.ratingBarBook);
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount);
            btnDelete     = itemView.findViewById(R.id.btnDeleteBook);
        }
    }
}
