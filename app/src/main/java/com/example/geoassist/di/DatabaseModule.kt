package com.example.geoassist.di

import android.content.Context
import androidx.room.Room
import com.example.geoassist.data.local.AppDatabase

object DatabaseModule {
    private var database: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "geoassist-db"
        ).build().also { database = it }
    }
}