package com.example.tp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResult {
    @SerializedName("docs")
    private List<Doc> docs;

    public List<Doc> getDocs() {
        return docs;
    }

    public static class Doc {
        @SerializedName("key")
        private String key;

        @SerializedName("title")
        private String title;

        @SerializedName("cover_i")
        private String coverI;

        @SerializedName("author_name")
        private List<String> authorName;

        @SerializedName("ratings_average")
        private Double ratingsAverage;

        public String getKey() {
            return key;
        }

        public String getTitle() {
            return title;
        }

        public String getCoverI() {
            return coverI;
        }

        public List<String> getAuthorName() {
            return authorName;
        }

        public Double getRatingsAverage() {
            return ratingsAverage;
        }
    }
}