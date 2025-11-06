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
     * Save follow record
     */
    suspend fun saveFollow(follow: Follow) {
        saveToFile("follows.json", follow, object : TypeToken<List<Follow>>() {}.type)
    }

    /**
     * Save comment
     */
    suspend fun saveComment(comment: Comment) {
        saveToFile("comments.json", comment, object : TypeToken<List<Comment>>() {}.type)
    }

    /**
     * Load likes
     */
    fun loadLikes(): List<Like> {
        return loadFromFile("likes.json", object : TypeToken<List<Like>>() {}.type)
    }

    /**
     * Update user information
     */
    suspend fun updateUser(updatedUser: User) {
        withContext(Dispatchers.IO) {
            try {
                val existingUsers = loadFromFile<User>("users.json", object : TypeToken<List<User>>() {}.type)
                val updatedUsers = existingUsers.map { user ->
                    if (user.id == updatedUser.id) updatedUser else user
                }

                val dataFile = File(context.filesDir, "users.json")
                val jsonString = gson.toJson(updatedUsers)

                FileWriter(dataFile).use { writer ->
                    writer.write(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Remove follow record (for unfollow)
     */
    suspend fun removeFollow(followerId: String, followingId: String) {
        withContext(Dispatchers.IO) {
            try {
                val existingFollows = loadFromFile<Follow>("follows.json", object : TypeToken<List<Follow>>() {}.type)
                val updatedFollows = existingFollows.filterNot {
                    it.followerId == followerId && it.followingId == followingId
                }

                val dataFile = File(context.filesDir, "follows.json")
                val jsonString = gson.toJson(updatedFollows)

                FileWriter(dataFile).use { writer ->
                    writer.write(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Generic method to save data to file
     */
    private suspend fun <T> saveToFile(fileName: String, newItem: T, typeToken: java.lang.reflect.Type) {
        withContext(Dispatchers.IO) {
            try {
                val dataFile = File(context.filesDir, fileName)

                // Ensure parent directory exists
                dataFile.parentFile?.mkdirs()

                // Create file if it doesn't exist
                if (!dataFile.exists()) {
                    dataFile.createNewFile()
                    // Initialize with empty array
                    FileWriter(dataFile).use { writer ->
                        writer.write("[]")
                    }
                }

                val existingData = loadFromFile<T>(fileName, typeToken)
                val updatedData = existingData + newItem

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
