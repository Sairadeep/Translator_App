package com.turbotech.translatordemo.dependencyInjection

import android.content.Context
import androidx.room.Room
import com.turbotech.translatordemo.data.TranslationDB
import com.turbotech.translatordemo.data.TranslationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    // Creation of actual DB as we are passing application context as a parameter.
    @Singleton
    @Provides
    fun provideTranslationDao(translationDB: TranslationDB): TranslationDao =
        translationDB.translationDao()

    @Singleton
    @Provides
    fun provideAppDB(@ApplicationContext context: Context): TranslationDB = Room.databaseBuilder(
        context,
        TranslationDB::class.java,
        name = "translation_db"
    ).fallbackToDestructiveMigration().build()
}