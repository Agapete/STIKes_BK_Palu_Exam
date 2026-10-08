package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AnswerDao
import com.example.data.local.dao.SyncQueueDao
import com.example.data.local.dao.ViolationDao
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.SyncQueueEntity
import com.example.data.local.entity.ViolationEntity

@Database(
    entities = [AnswerEntity::class, ViolationEntity::class, SyncQueueEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ExamDatabase : RoomDatabase() {
    abstract fun answerDao(): AnswerDao
    abstract fun violationDao(): ViolationDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: ExamDatabase? = null

        fun getInstance(context: Context): ExamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExamDatabase::class.java,
                    "campusexam_secure.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
