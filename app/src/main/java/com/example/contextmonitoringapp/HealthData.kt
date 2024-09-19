package com.example.contextmonitoringapp

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "health")
data class Health(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val heartRate: Double,
    val respiratoryRate: Double,
    val nausea:Int,
    val headache: Int,
    val diarrhea: Int,
    val soarThroat: Int,
    val fever: Int,
    val muscleAche: Int,
    val lossOfSmellOrTaste: Int,
    val cough: Int,
    val shortnessOfBreath: Int,
    val feelingTired: Int,
)

@Dao
interface HealthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthData(health: Health)

    @Query("SELECT * FROM health")
    suspend fun getAllHealthData(): List<Health>

}

@Database(entities = [Health::class], version = 2, exportSchema = false)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao
}
