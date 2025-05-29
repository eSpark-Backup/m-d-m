package com.nedaa.mdm.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nedaa.mdm.service.retrofit.ApiService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String BASE_URL = "https://your.server.api/";

    @Provides
    @Singleton
    public static Gson provideGson() {
        return new GsonBuilder().create();
    }

    @Provides
    @Singleton
    public static Retrofit provideRetrofit(Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();
    }

    @Provides
    @Singleton
    public static ApiService provideDeviceApi(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
