package com.example.test05.utils

import android.content.Context
import com.example.CLYRedNote.model.Message
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MessageStorage(private val context: Context) {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create()
    
    private val readGson = Gson() // For reading existing files with original format
    
    /**
     * Save message to internal storage
     * Both methods save to the same location: files/messages.json
     */
    suspend fun saveMessageToAssets(message: Message) {
        saveMessage(message)
    }

    suspend fun saveMessageToInternalStorage(message: Message) {
        saveMessage(message)
    }

    /**
     * Unified save method - saves messages to internal storage
     * Path: context.filesDir/messages.json
     */
    private suspend fun saveMessage(message: Message) {
        withContext(Dispatchers.IO) {
            try {
                val messagesFile = File(context.filesDir, "messages.json")

                // Ensure parent directory exists
                messagesFile.parentFile?.mkdirs()

                // Create file if it doesn't exist
                if (!messagesFile.exists()) {
                    messagesFile.createNewFile()
                    // Initialize with empty array
                    FileWriter(messagesFile).use { writer ->
                        writer.write("[]")
                    }
                }

                val existingMessages = loadMessagesFromInternalStorage()
                val updatedMessages = existingMessages + message

                val jsonString = gson.toJson(updatedMessages)

                FileWriter(messagesFile).use { writer ->
                    writer.write(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadMessagesFromInternalStorage(): List<Message> {
        return try {
            // Try to load from internal storage first
            val messagesFile = File(context.filesDir, "messages.json")
            if (messagesFile.exists()) {
                val jsonString = messagesFile.readText()
                val type = object : TypeToken<List<Message>>() {}.type
                readGson.fromJson(jsonString, type) ?: emptyList()
            } else {
                // If not exists, load from assets as initial data
                loadOriginalMessagesFromAssets()
            }
        } catch (e: Exception) {
            loadOriginalMessagesFromAssets()
        }
    }
    
    private fun loadOriginalMessagesFromAssets(): List<Message> {
        return try {
            val inputStream = context.assets.open("data/messages.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            val type = object : TypeToken<List<Message>>() {}.type
            readGson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}