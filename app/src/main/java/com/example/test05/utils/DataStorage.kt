package com.example.test05.utils

import android.content.Context
import com.example.CLYRedNote.model.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

/**
 * Universal storage class for various data types
 */
class DataStorage(private val context: Context) {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create()

    private val readGson = Gson()

    /**
     * Save search history
     */
    suspend fun saveSearchHistory(history: SearchHistory) {
        saveToFile("search_history.json", history, object : TypeToken<List<SearchHistory>>() {}.type)
    }

    /**
     * Save like record
     */
    suspend fun saveLike(like: Like) {
        saveToFile("likes.json", like, object : TypeToken<List<Like>>() {}.type)
    }

    /**
     * Save share record
     */
    suspend fun saveShare(share: Share) {
        saveToFile("shares.json", share, object : TypeToken<List<Share>>() {}.type)
    }

    /**
     * Save dislike record
     */
    suspend fun saveDislike(dislike: Dislike) {
        saveToFile("dislikes.json", dislike, object : TypeToken<List<Dislike>>() {}.type)
    }

    /**
     * Save collection
     */
    suspend fun saveCollection(collection: com.example.CLYRedNote.model.Collection) {
        saveToFile("collections.json", collection, object : TypeToken<List<com.example.CLYRedNote.model.Collection>>() {}.type)
    }

    /**
     * Save browsing history
     */
    suspend fun saveBrowsingHistory(history: BrowsingHistory) {
        saveToFile("browsing_history.json", history, object : TypeToken<List<BrowsingHistory>>() {}.type)
    }

    /**
     * Generic method to save data to file
     */
    private suspend fun <T> saveToFile(fileName: String, newItem: T, typeToken: java.lang.reflect.Type) {
        withContext(Dispatchers.IO) {
            try {
                val existingData = loadFromFile<T>(fileName, typeToken)
                val updatedData = existingData + newItem

                val dataFile = File(context.filesDir, fileName)
                val jsonString = gson.toJson(updatedData)

                FileWriter(dataFile).use { writer ->
                    writer.write(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Generic method to load data from file
     */
    private fun <T> loadFromFile(fileName: String, typeToken: java.lang.reflect.Type): List<T> {
        return try {
            val dataFile = File(context.filesDir, fileName)
            if (dataFile.exists()) {
                val jsonString = dataFile.readText()
                readGson.fromJson(jsonString, typeToken) ?: emptyList()
            } else {
                // Load from assets as initial data
                loadFromAssets(fileName, typeToken)
            }
        } catch (e: Exception) {
            loadFromAssets(fileName, typeToken)
        }
    }

    /**
     * Load initial data from assets
     */
    private fun <T> loadFromAssets(fileName: String, typeToken: java.lang.reflect.Type): List<T> {
        return try {
            val inputStream = context.assets.open("data/$fileName")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            readGson.fromJson(jsonString, typeToken) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
