package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "answers")
data class AnswerEntity(
    @PrimaryKey val questionId: String,
    val attemptId: String,
    val selectedOptionId: String?,
    val essayText: String?,
    val isFlagged: Boolean,
    val savedAt: Long,
    val isSynced: Boolean
)

@Entity(tableName = "violations")
data class ViolationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: String,
    val eventType: String,
    val timestamp: Long,
    val durationMs: Long,
    val scoreIncrement: Int,
    val severity: String,
    val description: String
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: String,
    val payloadType: String, // ANSWER, VIOLATION, HEARTBEAT
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
