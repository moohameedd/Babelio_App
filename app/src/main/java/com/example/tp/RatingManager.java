package com.example.tp;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class RatingManager {

    public interface RatingCallback {
        void onRatingLoaded(float average, int count);
    }

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static String keyFor(Book book) {
        String k = book.getKey();
        // Replace slashes with underscores as Firestore IDs cannot contain slashes
        return (k != null && !k.isEmpty()) ? k.replace("/", "_") : book.getTitle();
    }

    /** Submit a new rating and update the running total in Firestore atomically. */
    public static void addRating(Book book, float stars) {
        DocumentReference ref = db.collection("ratings").document(keyFor(book));

        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(ref);
            float total = 0f;
            long  count = 0L;

            if (snapshot.exists()) {
                Double existingTotal = snapshot.getDouble("total");
                Long existingCount = snapshot.getLong("count");
                total = (existingTotal != null) ? existingTotal.floatValue() : 0f;
                count = (existingCount != null) ? existingCount : 0L;
            }

            total += stars;
            count += 1;

            Map<String, Object> data = new HashMap<>();
            data.put("total",   total);
            data.put("count",   count);
            data.put("average", total / count);
            data.put("title",   book.getTitle());

            transaction.set(ref, data, SetOptions.merge());
            return null;
        }).addOnFailureListener(e -> {
            // Log or handle error
        });
    }

    /**
     * Real-time listener — updates whenever anyone submits a rating.
     */
    public static ListenerRegistration listenRating(Book book, RatingCallback callback) {
        return db.collection("ratings").document(keyFor(book))
                 .addSnapshotListener((doc, e) -> {
                     if (e != null || doc == null) return;
                     if (doc.exists()) {
                         Double avg   = doc.getDouble("average");
                         Long   count = doc.getLong("count");
                         callback.onRatingLoaded(
                             (avg != null) ? avg.floatValue() : 0f, 
                             (count != null) ? count.intValue() : 0
                         );
                     } else {
                         callback.onRatingLoaded(book.getUserRating(), book.getRatingCount());
                     }
                 });
    }
}
