package com.vccare.mananwason.vcare.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Manan Wason on 14/09/16.
 */
public class APIClient {

//    static final int CONNECT_TIMEOUT_MILLIS = 20 * 1000; // 15s
//
//    static final int READ_TIMEOUT_MILLIS = 50 * 1000; // 20s
//
//    private final MoviesApi moviesApi;
//    public static final String BASE_URL = "http://api.themoviedb.org/";
//
//
//    public APIClient() {
//        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
//                .connectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
//                .readTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
//                .addInterceptor(new HttpLoggingInterceptor().
//                        setLevel(HttpLoggingInterceptor.Level.BASIC))
//                .build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClient)
//                .build();
//
//        moviesApi = retrofit.create(MoviesApi.class);
//    }
//
//    public MoviesApi getMoviesAPI() {
//
//        return moviesApi;
//    }


}
