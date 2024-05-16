package com.turbotech.translatordemo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.turbotech.translatordemo.model.TranslationText
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    //    DAO is an object that all it does is responsible for directly accessing our DB. And data comes from DAO.
    @Query("Select * from translation_textTbl")
    fun getHistory(): Flow<List<TranslationText>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(translationText: TranslationText)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(translationText: TranslationText)

    @Delete
    suspend fun delete(translationText: TranslationText)

}