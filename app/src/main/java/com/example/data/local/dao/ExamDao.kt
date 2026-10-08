package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.SyncQueueEntity
import com.example.data.local.entity.ViolationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers WHERE attemptId = :attemptId")
    fun getAnswersForAttempt(attemptId: String): Flow<List<AnswerEntity>>

    @Query("SELECT * FROM answers WHERE questionId = :questionId LIMIT 1")
    suspend fun getAnswer(questionId: String): AnswerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnswerEntity)

    @Query("UPDATE answers SET isSynced = 1 WHERE questionId = :questionId")
    suspend fun markAsSynced(questionId: String)

    @Query("DELETE FROM answers WHERE attemptId = :attemptId")
    suspend fun clearAnswersForAttempt(attemptId: String)
}

@Dao
interface ViolationDao {
    @Query("SELECT * FROM violations WHERE attemptId = :attemptId ORDER BY timestamp DESC")
    fun getViolationsForAttempt(attemptId: String): Flow<List<ViolationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViolation(violation: ViolationEntity)

    @Query("SELECT SUM(scoreIncrement) FROM violations WHERE attemptId = :attemptId")
    suspend fun getTotalViolationScore(attemptId: String): Int?
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getPendingSyncItems(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}
