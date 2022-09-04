package com.steelrazor47.scantrino

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
    abstract fun receiptDao(): ReceiptDao
}

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    fun provideReceiptDao(appDatabase: AppDatabase): ReceiptDao {
        return appDatabase.receiptDao()
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