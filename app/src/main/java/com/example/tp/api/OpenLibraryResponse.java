package com.example.tp.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OpenLibraryResponse {
    @SerializedName("docs")
    private List<Doc> docs;

    public List<Doc> getDocs() { return docs; }

    public static class Doc {
        @SerializedName("title")
        private String title;
        
        @SerializedName("author_name")
        private List<String> authorName;
        
        @SerializedName("cover_i")
        private String coverI;

        public String getTitle() { return title; }
        public List<String> getAuthorName() { return authorName; }
        public String getCoverI() { return coverI; }

        public Book toBook() {
            String author = (authorName != null && !authorName.isEmpty()) ? authorName.get(0) : "Unknown Author";
            String imageUrl = (coverI != null) ? "https://covers.openlibrary.org/b/id/" + coverI + "-M.jpg" : null;
            return new Book(title, author, imageUrl);
        }
    }
}