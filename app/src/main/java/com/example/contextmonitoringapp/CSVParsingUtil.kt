package com.example.contextmonitoringapp


import android.content.Context
import com.opencsv.CSVReader
import java.io.InputStreamReader

object CSVParserUtil {
    fun parseCsvData(context: Context, fileName: String): List<List<Float>> {
        val assetManager = context.assets
        val inputStream = assetManager.open(fileName)
        val reader = CSVReader(InputStreamReader(inputStream))

        val data = mutableListOf<List<Float>>()
        var line: Array<String>?
        while (reader.readNext().also { line = it } != null) {
            line?.let {
                data.add(it.map { value -> value.toFloat() })
            }
        }
        reader.close()
        return data
    }
}