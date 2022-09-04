package com.steelrazor47.scantrino.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(entities = [Receipt::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptsDao(): ReceiptsDao
}

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    fun provideReceiptDao(appDatabase: AppDatabase): ReceiptsDao {
        return appDatabase.receiptsDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "SCANtrino"
        ).build()
    }
}