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
    
    suspend fun saveMessageToAssets(message: Message) {
        withContext(Dispatchers.IO) {
            try {
                val existingMessages = loadMessagesFromAssets()
                val updatedMessages = existingMessages + message
                
                val messagesDir = File(context.filesDir, "assets/data")
                if (!messagesDir.exists()) {
                    messagesDir.mkdirs()
                }
                
                val messagesFile = File(messagesDir, "messages.json")
                val jsonString = gson.toJson(updatedMessages)
                
                FileWriter(messagesFile).use { writer ->
                    writer.write(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun saveMessageToInternalStorage(message: Message) {
        withContext(Dispatchers.IO) {
            try {
                val existingMessages = loadMessagesFromInternalStorage()
                val updatedMessages = existingMessages + message
                
                val messagesFile = File(context.filesDir, "messages.json")
                val jsonString = gson.toJson(updatedMessages)
                
                FileWriter(messagesFile).use { writer ->
                    writer.write(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadMessagesFromAssets(): List<Message> {
        return try {
            val messagesFile = File(context.filesDir, "assets/data/messages.json")
            if (messagesFile.exists()) {
                val jsonString = messagesFile.readText()
                val type = object : TypeToken<List<Message>>() {}.type
                readGson.fromJson(jsonString, type) ?: emptyList()
            } else {
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
    
    private fun loadMessagesFromInternalStorage(): List<Message> {
        return try {
            val messagesFile = File(context.filesDir, "messages.json")
            if (messagesFile.exists()) {
                val jsonString = messagesFile.readText()
                val type = object : TypeToken<List<Message>>() {}.type
                readGson.fromJson(jsonString, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}