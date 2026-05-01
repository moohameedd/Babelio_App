package com.example.tp.api;

import com.example.tp.SearchResult;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface BookService {
    @GET("search.json")
    Call<SearchResult> searchBooks(
            @Query("q") String query,
            @Query("sort") String sort,
            @Query("limit") Integer limit,
            @Query("fields") String fields
    );

    @GET
    Call<ResponseBody> getBookDetailsRaw(@Url String url);
}