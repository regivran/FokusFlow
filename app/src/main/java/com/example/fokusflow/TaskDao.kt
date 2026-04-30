package com.example.fokusflow

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Hlavní seznam - nehotové, nesmazané, volné (bez termínu)
    @Query("""
        SELECT * FROM tasks 
        WHERE isDeleted = 0 AND isCompleted = 0 AND dueDate IS NULL 
        ORDER BY 
            CASE priority 
                WHEN 'High' THEN 1 
                WHEN 'Medium' THEN 2 
                WHEN 'Low' THEN 3 
            END ASC, 
            id DESC
    """)
    fun getFreeTasks(): Flow<List<Task>>

    // Hlavní seznam - nehotové, nesmazané, s termínem
    @Query("""
        SELECT * FROM tasks 
        WHERE isDeleted = 0 AND isCompleted = 0 AND dueDate IS NOT NULL 
        ORDER BY 
            dueDate ASC, 
            CASE priority 
                WHEN 'High' THEN 1 
                WHEN 'Medium' THEN 2 
                WHEN 'Low' THEN 3 
            END ASC
    """)
    fun getDeadlineTasks(): Flow<List<Task>>

    // Hotové úkoly
    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND isCompleted = 1 ORDER BY id DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    // Koš
    @Query("SELECT * FROM tasks WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // Smazat úkoly z koše starší než 30 dní
    @Query("DELETE FROM tasks WHERE isDeleted = 1 AND deletedAt <= :thresholdDate")
    suspend fun purgeOldDeletedTasks(thresholdDate: java.time.LocalDate)
}