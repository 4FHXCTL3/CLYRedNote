package com.example.test05.utils

import android.content.Context
import com.example.CLYRedNote.model.BrowsingHistory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class BrowsingHistoryStorage(private val context: Context) {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create()

    private val readGson = Gson()

    /**
     * Save browsing history to internal storage
     * Path: context.filesDir/browsing_history.json
     */
    suspend fun saveBrowsingHistory(history: BrowsingHistory) {
        withContext(Dispatchers.IO) {
            try {
                val existingHistory = loadBrowsingHistory()
                val updatedHistory = existingHistory + history

                val historyFile = File(context.filesDir, "browsing_history.json")
                val jsonString = gson.toJson(updatedHistory)

                FileWriter(historyFile).use { writer ->
                    writer.write(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Load browsing history from internal storage
     */
    private fun loadBrowsingHistory(): List<BrowsingHistory> {
        return try {
            val historyFile = File(context.filesDir, "browsing_history.json")
            if (historyFile.exists()) {
                val jsonString = historyFile.readText()
                val type = object : TypeToken<List<BrowsingHistory>>() {}.type
                readGson.fromJson(jsonString, type) ?: emptyList()
            } else {
                // Load from assets as initial data
                loadOriginalFromAssets()
            }
        } catch (e: Exception) {
            loadOriginalFromAssets()
        }
    }

    private fun loadOriginalFromAssets(): List<BrowsingHistory> {
        return try {
            val inputStream = context.assets.open("data/browsing_history.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            val type = object : TypeToken<List<BrowsingHistory>>() {}.type
            readGson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
