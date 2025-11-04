package com.example.test05.utils

import android.content.Context
import com.example.CLYRedNote.model.*
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class JsonDataLoader(private val context: Context) {
    private var usersCache: List<User>? = null
    
    private fun getUsersCache(): List<User> {
        if (usersStaticCache == null) {
            usersStaticCache = loadUsersInternal().toMutableList()
        }
        return usersStaticCache!!
    }
    
    private fun getNotesCache(): MutableList<Note> {
        if (JsonDataLoader.notesCache == null) {
            JsonDataLoader.notesCache = loadNotesInternal().toMutableList()
        }
        return JsonDataLoader.notesCache!!
    }
    
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create()

    fun loadUsers(): List<User> {
        return getUsersCache()
    }
    
    private fun loadUsersInternal(): List<User> {
        return try {
            val jsonString = loadJsonFromAssets("data/users.json")
            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            jsonArray.map { parseUser(it.asJsonObject) }
        } catch (e: Exception) {
            // Return mock data if loading fails
            listOf(
                User(
                    id = "user_current",
                    username = "CLY",
                    nickname = "CLY",
                    followerCount = 128,
                    followingCount = 89,
                    noteCount = 23,
                    level = 2
                )
            )
        }
    }

    fun loadNotes(): List<Note> {
        return getNotesCache()
    }
    
    private fun loadNotesInternal(): List<Note> {
        return try {
            val jsonString = loadJsonFromAssets("data/notes.json")
            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            jsonArray.map { parseNote(it.asJsonObject) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadCollections(): List<com.example.CLYRedNote.model.Collection> {
        return try {
            val jsonString = loadJsonFromAssets("data/collections.json")
            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            jsonArray.map { parseCollection(it.asJsonObject) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadFollows(): List<Follow> {
        return try {
            val jsonString = loadJsonFromAssets("data/follows.json")
            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            jsonArray.map { parseFollow(it.asJsonObject) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadProducts(): List<Product> {
        return try {
            val jsonString = loadJsonFromAssets("data/products.json")
            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            jsonArray.map { parseProduct(it.asJsonObject) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadMessages(): List<Message> {
        return try {
            // First try to load from internal storage (where we save messages)
            val messagesFile = java.io.File(context.filesDir, "messages.json")
            val jsonString = if (messagesFile.exists()) {
                messagesFile.readText()
            } else {
                // Fall back to assets if no saved messages
                // Copy initial data from assets to internal storage for future updates
                val initialData = loadJsonFromAssets("data/messages.json")
                if (initialData.isNotEmpty()) {
                    try {
                        messagesFile.writeText(initialData)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                initialData
            }

            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            jsonArray.map { parseMessage(it.asJsonObject) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadComments(): List<Comment> {
        return getCommentsCache()
    }
    
    private fun loadCommentsInternal(): List<Comment> {
        return try {
            val jsonString = loadJsonFromAssets("data/comments.json")
            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            jsonArray.map { parseComment(it.asJsonObject) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCurrentUser(): User? {
        return loadUsers().find { it.id == "user_current" }
    }
    
    fun updateUser(updatedUser: User) {
        val usersList = getUsersCache() as MutableList<User>
        val index = usersList.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            usersList[index] = updatedUser
        }
        
        // Also update the instance cache
        usersCache = usersList
    }

    fun saveNote(note: Note) {
        // Add the new note to the beginning of the list (newest first)
        getNotesCache().add(0, note)
    }
    
    fun saveComment(comment: Comment) {
        // Add the new comment to the comments cache
        getCommentsCache().add(comment)
    }
    
    private fun getCommentsCache(): MutableList<Comment> {
        if (commentsCache == null) {
            commentsCache = loadCommentsInternal().toMutableList()
        }
        return commentsCache!!
    }
    
    companion object {
        private var notesCache: MutableList<Note>? = null
        private var commentsCache: MutableList<Comment>? = null
        private var usersStaticCache: MutableList<User>? = null
    }

    fun loadShoppingCart(): List<CartItemJson> {
        return try {
            val jsonString = loadJsonFromAssets("data/shopping_cart.json")
            val jsonArray = JsonParser.parseString(jsonString).asJsonArray
            jsonArray.map { parseCartItem(it.asJsonObject) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseUser(json: JsonObject): User {
        return User(
            id = json.get("id").asString,
            username = json.get("username").asString,
            nickname = json.get("nickname").asString,
            avatar = json.get("avatar")?.asString,
            bio = json.get("bio")?.asString,
            followerCount = json.get("followerCount")?.asInt ?: 0,
            followingCount = json.get("followingCount")?.asInt ?: 0,
            noteCount = json.get("noteCount")?.asInt ?: 0,
            isFollowed = json.get("isFollowed")?.asBoolean ?: false,
            isVerified = json.get("isVerified")?.asBoolean ?: false,
            level = json.get("level")?.asInt ?: 1,
            createdAt = parseDate(json.get("createdAt")?.asString)
        )
    }

    private fun parseNote(json: JsonObject): Note {
        val users = getUsersCache() // Use cached users to avoid recursive calls
        val author = users.find { it.id == json.get("authorId").asString } 
            ?: User(id = "unknown", username = "unknown", nickname = "Unknown")
            
        return Note(
            id = json.get("id").asString,
            title = json.get("title").asString,
            content = json.get("content").asString,
            type = NoteType.valueOf(json.get("type")?.asString ?: "TEXT"),
            author = author,
            coverImage = json.get("coverImage")?.asString,
            images = json.get("images")?.asJsonArray?.map { it.asString } ?: emptyList(),
            tags = json.get("tags")?.asJsonArray?.map { it.asString } ?: emptyList(),
            topics = json.get("topics")?.asJsonArray?.map { it.asString } ?: emptyList(),
            visibility = NoteVisibility.valueOf(json.get("visibility")?.asString ?: "PUBLIC"),
            likeCount = json.get("likeCount")?.asInt ?: 0,
            commentCount = json.get("commentCount")?.asInt ?: 0,
            shareCount = json.get("shareCount")?.asInt ?: 0,
            collectCount = json.get("collectCount")?.asInt ?: 0,
            viewCount = json.get("viewCount")?.asInt ?: 0,
            isLiked = json.get("isLiked")?.asBoolean ?: false,
            isCollected = json.get("isCollected")?.asBoolean ?: false,
            createdAt = parseDate(json.get("createdAt")?.asString)
        )
    }

    private fun parseCollection(json: JsonObject): com.example.CLYRedNote.model.Collection {
        val notes = loadNotes() // Cache this to avoid repeated loading
        val note = notes.find { it.id == json.get("noteId").asString } 
            ?: Note(id = "unknown", title = "Unknown", content = "", type = NoteType.TEXT, 
                    author = User(id = "unknown", username = "unknown", nickname = "Unknown"))
            
        return com.example.CLYRedNote.model.Collection(
            id = json.get("id").asString,
            userId = json.get("userId").asString,
            noteId = json.get("noteId").asString,
            note = note,
            folderId = json.get("folderId")?.asString,
            folderName = json.get("folderName")?.asString,
            tags = json.get("tags")?.asJsonArray?.map { it.asString } ?: emptyList(),
            notes = json.get("notes")?.asString,
            collectedAt = parseDate(json.get("collectedAt")?.asString)
        )
    }

    private fun parseFollow(json: JsonObject): Follow {
        val users = loadUsers() // Cache this to avoid repeated loading
        val follower = users.find { it.id == json.get("followerId").asString }
            ?: User(id = "unknown", username = "unknown", nickname = "Unknown")
        val following = users.find { it.id == json.get("followingId").asString }
            ?: User(id = "unknown", username = "unknown", nickname = "Unknown")
            
        return Follow(
            id = json.get("id").asString,
            followerId = json.get("followerId").asString,
            followingId = json.get("followingId").asString,
            follower = follower,
            following = following,
            followedAt = parseDate(json.get("followedAt")?.asString),
            isMutual = json.get("isMutual")?.asBoolean ?: false,
            isSpecialFollow = json.get("isSpecialFollow")?.asBoolean ?: false,
            tags = json.get("tags")?.asJsonArray?.map { it.asString } ?: emptyList()
        )
    }

    private fun parseProduct(json: JsonObject): Product {
        return Product(
            id = json.get("id").asString,
            name = json.get("name").asString,
            description = json.get("description").asString,
            brand = json.get("brand")?.asString,
            category = json.get("category").asString,
            price = json.get("price").asBigDecimal,
            originalPrice = json.get("originalPrice")?.asBigDecimal,
            discountRate = json.get("discountRate")?.asDouble,
            images = json.get("images")?.asJsonArray?.map { it.asString } ?: emptyList(),
            thumbnailImage = json.get("thumbnailImage")?.asString,
            sellerId = json.get("sellerId").asString,
            sellerName = json.get("sellerName").asString,
            stock = json.get("stock")?.asInt ?: 0,
            salesCount = json.get("salesCount")?.asInt ?: 0,
            rating = json.get("rating")?.asDouble ?: 0.0,
            reviewCount = json.get("reviewCount")?.asInt ?: 0,
            tags = json.get("tags")?.asJsonArray?.map { it.asString } ?: emptyList(),
            specifications = json.get("specifications")?.asJsonObject?.entrySet()?.associate { 
                it.key to it.value.asString 
            } ?: emptyMap(),
            createdAt = parseDate(json.get("createdAt")?.asString)
        )
    }

    private fun parseMessage(json: JsonObject): Message {
        val users = loadUsers() // Cache this to avoid repeated loading
        val sender = users.find { it.id == json.get("senderId").asString } 
            ?: User(id = "unknown", username = "unknown", nickname = "Unknown")
        val receiver = users.find { it.id == json.get("receiverId").asString } 
            ?: User(id = "unknown", username = "unknown", nickname = "Unknown")
            
        return Message(
            id = json.get("id").asString,
            content = json.get("content").asString,
            type = MessageType.valueOf(json.get("type")?.asString ?: "TEXT"),
            sender = sender,
            receiver = receiver,
            conversationId = json.get("conversationId").asString,
            isRead = json.get("isRead")?.asBoolean ?: false,
            emoji = json.get("emoji")?.asString,
            note = null, // Could be loaded if needed
            createdAt = parseDate(json.get("createdAt")?.asString)
        )
    }

    private fun parseDate(dateString: String?): Date {
        return try {
            if (dateString != null) {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                format.parse(dateString) ?: Date()
            } else {
                Date()
            }
        } catch (e: Exception) {
            Date()
        }
    }

    private fun parseComment(json: JsonObject): Comment {
        val users = getUsersCache() // Use cached users to avoid recursive calls
        val author = users.find { it.id == json.get("authorId").asString } 
            ?: User(id = "unknown", username = "unknown", nickname = "Unknown")
            
        return Comment(
            id = json.get("id").asString,
            content = json.get("content").asString,
            author = author,
            noteId = json.get("noteId").asString,
            parentCommentId = json.get("parentCommentId")?.asString,
            replyToUserId = json.get("replyToUserId")?.asString,
            replyToUsername = json.get("replyToUsername")?.asString,
            likeCount = json.get("likeCount")?.asInt ?: 0,
            replyCount = json.get("replyCount")?.asInt ?: 0,
            isLiked = json.get("isLiked")?.asBoolean ?: false,
            images = json.get("images")?.asJsonArray?.map { it.asString } ?: emptyList(),
            status = json.get("status")?.asString?.let { CommentStatus.valueOf(it) } ?: CommentStatus.NORMAL,
            createdAt = parseDate(json.get("createdAt")?.asString),
            updatedAt = parseDate(json.get("updatedAt")?.asString),
            isAuthorReply = json.get("isAuthorReply")?.asBoolean ?: false,
            isPinned = json.get("isPinned")?.asBoolean ?: false
        )
    }

    private fun parseCartItem(json: JsonObject): CartItemJson {
        return CartItemJson(
            id = json.get("id").asString,
            productId = json.get("productId").asString,
            userId = json.get("userId").asString,
            quantity = json.get("quantity").asInt,
            selectedSpecs = json.get("selectedSpecs")?.asJsonObject?.entrySet()?.associate { 
                it.key to it.value.asString 
            } ?: emptyMap(),
            isSelected = json.get("isSelected")?.asBoolean ?: true,
            addedAt = parseDate(json.get("addedAt")?.asString)
        )
    }

    private fun loadJsonFromAssets(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ""
        }
    }
}

data class CartItemJson(
    val id: String,
    val productId: String,
    val userId: String,
    val quantity: Int,
    val selectedSpecs: Map<String, String>,
    val isSelected: Boolean,
    val addedAt: Date
)