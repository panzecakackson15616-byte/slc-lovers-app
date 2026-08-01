package com.slclovers.app.data.repository

import android.content.Context
import androidx.room.Room
import com.slclovers.app.data.SLCDatabase

/**
 * 数据库单例
 */
object DatabaseProvider {
    @Volatile private var INSTANCE: SLCDatabase? = null

    fun get(context: Context): SLCDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                SLCDatabase::class.java,
                "slc-lovers.db"
            )
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}