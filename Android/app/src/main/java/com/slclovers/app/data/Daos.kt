package com.slclovers.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    fun observeCurrent(): Flow<UserEntity?>

    @Query("DELETE FROM users") suspend fun clear()
}

@Dao
interface PairingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pairing: PairingEntity)

    @Query("SELECT * FROM pairings LIMIT 1")
    fun observeCurrent(): Flow<PairingEntity?>

    @Query("SELECT * FROM pairings LIMIT 1")
    suspend fun getCurrent(): PairingEntity?

    @Query("DELETE FROM pairings") suspend fun clear()
}

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages") suspend fun clear()
}

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: PhotoEntity)

    @Query("SELECT * FROM photos ORDER BY takenAt DESC")
    fun observeAll(): Flow<List<PhotoEntity>>

    @Query("DELETE FROM photos") suspend fun clear()
}

@Dao
interface DiaryDao {
    @Insert
    suspend fun insert(entry: DiaryEntryEntity)

    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DiaryEntryEntity>>

    @Delete suspend fun delete(entry: DiaryEntryEntity)
    @Query("DELETE FROM diary_entries") suspend fun clear()
}

@Dao
interface TodoDao {
    @Insert
    suspend fun insert(todo: TodoEntity)

    @Update
    suspend fun update(todo: TodoEntity)

    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, createdAt DESC")
    fun observeAll(): Flow<List<TodoEntity>>

    @Delete suspend fun delete(todo: TodoEntity)
    @Query("DELETE FROM todos") suspend fun clear()
}

@Dao
interface BucketDao {
    @Insert
    suspend fun insert(item: BucketItemEntity)

    @Update
    suspend fun update(item: BucketItemEntity)

    @Query("SELECT * FROM buckets ORDER BY isAchieved ASC, createdAt DESC")
    fun observeAll(): Flow<List<BucketItemEntity>>

    @Delete suspend fun delete(item: BucketItemEntity)
    @Query("DELETE FROM buckets") suspend fun clear()
}

@Dao
interface AnniversaryDao {
    @Insert
    suspend fun insert(anniversary: AnniversaryEntity)

    @Delete suspend fun delete(anniversary: AnniversaryEntity)
    @Query("SELECT * FROM anniversaries ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<AnniversaryEntity>>

    @Query("DELETE FROM anniversaries") suspend fun clear()
}

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity)

    @Query("SELECT * FROM locations")
    fun observeAll(): Flow<List<LocationEntity>>

    @Query("DELETE FROM locations") suspend fun clear()
}

@Dao
interface CapsuleDao {
    @Insert
    suspend fun insert(capsule: TimeCapsuleEntity)

    @Update
    suspend fun update(capsule: TimeCapsuleEntity)

    @Query("SELECT * FROM capsules ORDER BY unlockDate ASC")
    fun observeAll(): Flow<List<TimeCapsuleEntity>>

    @Delete suspend fun delete(capsule: TimeCapsuleEntity)
    @Query("DELETE FROM capsules") suspend fun clear()
}

@Dao
interface StickyNoteDao {
    @Insert
    suspend fun insert(note: StickyNoteEntity)

    @Delete suspend fun delete(note: StickyNoteEntity)
    @Query("SELECT * FROM sticky_notes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<StickyNoteEntity>>

    @Query("DELETE FROM sticky_notes") suspend fun clear()
}

@Dao
interface HobbyDao {
    @Insert
    suspend fun insert(hobby: HobbyEntity)

    @Delete suspend fun delete(hobby: HobbyEntity)
    @Query("SELECT * FROM hobbies ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<HobbyEntity>>

    @Query("DELETE FROM hobbies") suspend fun clear()
}