package com.example.contextmonitoringapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.contextmonitoringapp.ui.theme.ContextMonitoringAppTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : ComponentActivity() {
    private lateinit var healthViewModel: HealthViewModel

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(
            applicationContext,
            HealthDatabase::class.java, "health-database"
        ).fallbackToDestructiveMigration().build()
        val healthDao = db.healthDao()
        val healthViewModelFactory = HealthViewModelFactory(healthDao)
        healthViewModel = ViewModelProvider(this, healthViewModelFactory)[HealthViewModel::class.java]


        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Read permission","Permission provided")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
        }
        setContent {

            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "monitoring") {
                composable("monitoring") {
                    MonitoringScreen(
                        navController = navController,
                        healthViewModel = healthViewModel,
                        activity = this@MainActivity
                    )
                }
                composable("logSymptoms") {
                    LogSymptomsScreen(navController = navController, healthViewModel = healthViewModel)
                }
            }
        }
    }

    @Composable
    fun MonitoringScreen(
        navController: NavController,
        healthViewModel: HealthViewModel,
        activity: MainActivity,
    ) {
        val accelXData = CSVParserUtil.parseCsvData(activity, "CSVBreatheX.csv").flatten()
        val accelYData = CSVParserUtil.parseCsvData(activity, "CSVBreatheY.csv").flatten()
        val accelZData = CSVParserUtil.parseCsvData(activity, "CSVBreatheZ.csv").flatten()

        val displayRespRate by healthViewModel.respiratoryRate.observeAsState(0.0)
        val displayHeartRate by healthViewModel.heartRate.observeAsState(0.0)


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Heart Rate: $displayHeartRate",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = "Respiratory Rate: $displayRespRate",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {

                        val videoUri = exportingMp4toStorage(this@MainActivity, "HeartRate.mp4")
                        if (videoUri != null) {
                            Log.d("video URL", videoUri.toString())
                                val rate = healthViewModel.startHeartRateProcessing(videoUri, contentResolver)
                        } else {
                            Log.e("HeartRate", "Failed to get video URI")
                        }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Measure Heart Rate")
            }

            Button(
                onClick = {
                    healthViewModel.calculateAndSetRespiratoryRate(accelXData, accelYData, accelZData)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Measure Respiratory Rate")
            }

            Button(
                onClick = { navController.navigate("logSymptoms") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Log Symptoms")
            }
        }
    }



    private fun exportingMp4toStorage(context: Context, fileName: String = "HeartRate.mp4"): Uri? {
        val assetManager = context.assets
        val outFile = File(context.getExternalFilesDir(null), fileName)

        return try {
            assetManager.open(fileName).use { inputStream ->
                FileOutputStream(outFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                }
            }
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                outFile
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @Composable
    fun LogSymptomsScreen(navController: NavController, healthViewModel: HealthViewModel = viewModel()) {
        val symptomRatings = remember { mutableStateMapOf<String, Int>() }

        var selectedSymptom by remember { mutableStateOf("") }
        var rating by remember { mutableIntStateOf(1) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Log Symptoms", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.size(16.dp))

            // Symptom dropdown
            SymptomsDropdown(
                selectedSymptom = selectedSymptom,
                onSymptomSelected = { symptom ->
                    selectedSymptom = symptom
                    rating = symptomRatings.getOrDefault(symptom, 1)
                }
            )

            if (selectedSymptom.isNotEmpty()) {
                Slider(

                    value = rating.toFloat(),
                    onValueChange = {
                        rating = it.toInt()
                        symptomRatings[selectedSymptom] = rating
                    },
                    valueRange = 0f..5f,
                    steps = 4
                )
                Spacer(Modifier.size(16.dp))
            }

            Button(
                onClick = {
                    healthViewModel.saveHealthData(symptomRatings)
                    navController.popBackStack()
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Save Symptoms")
            }

            Button(
                onClick = {
                    healthViewModel.printAllHealthRecords()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Print All Health Records")
            }
        }
    }
    @Composable
    fun SymptomsDropdown(selectedSymptom: String, onSymptomSelected: (String) -> Unit) {
        val symptoms = listOf("Nausea","Headache","Diarrhea","Soar Throat","Fever","Muscle Ache","Loss of Smell or Taste","Cough","Shortness of Breath","Feeling Tired")
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedSymptom.ifEmpty { "Select Symptom" })
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                symptoms.forEach { symptom ->
                    DropdownMenuItem(
                        text = { Text(symptom) },
                        onClick = {
                            onSymptomSelected(symptom)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun MonitoringScreenPreview() {
    ContextMonitoringAppTheme {
    }
}