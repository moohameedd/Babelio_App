package com.example.tp;

import com.google.gson.JsonElement;
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
        private JsonElement coverI;

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
            if (coverI != null && coverI.isJsonPrimitive()) {
                return coverI.getAsString();
            }
            return "";
        }

        public List<String> getAuthorName() {
            return authorName;
        }

        public Double getRatingsAverage() {
            return ratingsAverage;
        }
    }
}