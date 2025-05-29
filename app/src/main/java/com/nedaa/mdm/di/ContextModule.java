package com.nedaa.mdm.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ContextModule {

    @Provides
    @Singleton
    @ApplicationContext
    public Context provideContext(Application application) {
        return application.getApplicationContext();
    }
}
