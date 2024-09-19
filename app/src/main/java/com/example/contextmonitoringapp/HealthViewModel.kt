package com.example.contextmonitoringapp

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

class HealthViewModel(private val healthDao: HealthDao) : ViewModel() {

    private val _respiratoryRate = MutableLiveData<Double>()
    val respiratoryRate: LiveData<Double> = _respiratoryRate
    private val _heartRate = MutableLiveData<Double>()
    val heartRate: LiveData<Double> = _heartRate

    fun calculateAndSetRespiratoryRate(
        accelValuesX: List<Float>,
        accelValuesY: List<Float>,
        accelValuesZ: List<Float>
    ) {
        val calculatedRate = calculateRespiratoryRate(accelValuesX, accelValuesY, accelValuesZ)
        _respiratoryRate.value = calculatedRate.toDouble()
    }

    private fun calculateRespiratoryRate(
        accelValuesX: List<Float>,
        accelValuesY: List<Float>,
        accelValuesZ: List<Float>
    ): Int {
        var previousValue = 10f
        var currentValue: Float
        var k = 0

        for (i in 11 until accelValuesY.size) {
            currentValue = kotlin.math.sqrt(
                accelValuesZ[i].toDouble().pow(2.0) + accelValuesX[i].toDouble()
                    .pow(2.0) + accelValuesY[i].toDouble().pow(2.0)
            ).toFloat()
            if (abs(previousValue - currentValue) > 0.15) {
                k++
            }
            previousValue = currentValue
        }

        val result = (k.toDouble() / 45.00)
        return (result * 30).toInt()
    }

    fun startHeartRateProcessing(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val heartRateResult = heartRateCalculator(uri, contentResolver)
            _heartRate.postValue(heartRateResult.toDouble())
        }
    }

    private suspend fun heartRateCalculator(uri: Uri, contentResolver: ContentResolver): Int {
        return withContext(Dispatchers.IO) {
            val result: Int
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(uri, proj, null, null, null)
            val columnIndex =
                cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            val path = cursor?.getString(columnIndex ?: 0)
            cursor?.close()

            val retriever = MediaMetadataRetriever()
            val frameList = ArrayList<Bitmap>()
            try {
                retriever.setDataSource(path)
                val duration =
                    retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT
                    )
                val frameDuration = min(duration!!.toInt(), 425)
                var i = 10
                while (i < frameDuration) {
                    val bitmap = retriever.getFrameAtIndex(i)
                    bitmap?.let { frameList.add(it) }
                    i += 15
                }
            } catch (e: Exception) {
                Log.d("MediaPath", "convertMediaUriToPath: ${e.stackTrace} ")
            } finally {
                retriever.release()
                var redBucket: Long
                var pixelCount: Long = 0
                val a = mutableListOf<Long>()
                for (i in frameList) {
                    redBucket = 0
                    for (y in 350 until 450) {
                        for (x in 350 until 450) {
                            val c: Int = i.getPixel(x, y)
                            pixelCount++
                            redBucket += Color.red(c) + Color.blue(c) +
                                    Color.green(c)
                        }
                    }
                    a.add(redBucket)
                }
                val b = mutableListOf<Long>()
                for (i in 0 until a.lastIndex - 5) {
                    val temp =
                        (a.elementAt(i) + a.elementAt(i + 1) + a.elementAt(i + 2)
                                + a.elementAt(
                            i + 3
                        ) + a.elementAt(
                            i + 4
                        )) / 4
                    b.add(temp)
                }
                var x = b.elementAt(0)
                var count = 0
                for (i in 1 until b.lastIndex) {
                    val p = b.elementAt(i)
                    if ((p - x) > 3500) {
                        count += 1
                    }
                    x = b.elementAt(i)
                }
                val rate = ((count.toFloat()) * 60).toInt()
                result = (rate / 4)
            }
            result
        }
    }

    private fun getHeartRate(): Double {
        return heartRate.value ?: 0.0
    }

    private fun getRespiratoryRate(): Double {
        return respiratoryRate.value ?: 0.0
    }

    fun saveHealthData(symptomRatings: Map<String, Int>) {
        viewModelScope.launch {
            val health = Health(
                heartRate = getHeartRate(),
                respiratoryRate = getRespiratoryRate(),
                nausea = symptomRatings.getOrDefault("Nausea", 0),  // Default 0
                headache = symptomRatings.getOrDefault("Headache", 0),
                diarrhea = symptomRatings.getOrDefault("Diarrhea", 0),
                soarThroat = symptomRatings.getOrDefault("Soar Throat", 0),
                fever = symptomRatings.getOrDefault("Fever", 0),
                muscleAche = symptomRatings.getOrDefault("Muscle Ache", 0),
                lossOfSmellOrTaste = symptomRatings.getOrDefault("Loss of Smell or Taste", 0),
                cough = symptomRatings.getOrDefault("Cough", 0),
                shortnessOfBreath = symptomRatings.getOrDefault("Shortness of Breath", 0),
                feelingTired = symptomRatings.getOrDefault("Feeling Tired", 0)
            )
            healthDao.insertHealthData(health)
        }
    }

    fun printAllHealthRecords() {
        viewModelScope.launch {
            val allRecords = healthDao.getAllHealthData()
            allRecords.forEach { record ->
                println("Health Record: $record")
            }
        }
    }
}
