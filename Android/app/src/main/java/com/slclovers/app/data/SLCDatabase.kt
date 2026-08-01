package com.slclovers.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.slclovers.app.data.model.AnniversaryEntity
import com.slclovers.app.data.model.BucketItemEntity
import com.slclovers.app.data.model.DiaryEntryEntity
import com.slclovers.app.data.model.HobbyEntity
import com.slclovers.app.data.model.LocationEntity
import com.slclovers.app.data.model.MessageEntity
import com.slclovers.app.data.model.PairingEntity
import com.slclovers.app.data.model.PhotoEntity
import com.slclovers.app.data.model.StickyNoteEntity
import com.slclovers.app.data.model.TimeCapsuleEntity
import com.slclovers.app.data.model.TodoEntity
import com.slclovers.app.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PairingEntity::class,
        MessageEntity::class,
        PhotoEntity::class,
        DiaryEntryEntity::class,
        TodoEntity::class,
        BucketItemEntity::class,
        AnniversaryEntity::class,
        LocationEntity::class,
        TimeCapsuleEntity::class,
        StickyNoteEntity::class,
        HobbyEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SLCDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun pairingDao(): PairingDao
    abstract fun messageDao(): MessageDao
    abstract fun photoDao(): PhotoDao
    abstract fun diaryDao(): DiaryDao
    abstract fun todoDao(): TodoDao
    abstract fun bucketDao(): BucketDao
    abstract fun anniversaryDao(): AnniversaryDao
    abstract fun locationDao(): LocationDao
    abstract fun capsuleDao(): CapsuleDao
    abstract fun stickyNoteDao(): StickyNoteDao
    abstract fun hobbyDao(): HobbyDao
}