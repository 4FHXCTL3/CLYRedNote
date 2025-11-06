package com.example.test05.presenter

import com.example.CLYRedNote.model.*
import com.example.test05.ui.tabs.notedetail.NoteDetailContract
import com.example.test05.utils.JsonDataLoader
import com.example.test05.utils.DataStorage
import kotlinx.coroutines.*
import java.util.Date

class NoteDetailPresenter(
    private val dataLoader: JsonDataLoader,
    private val dataStorage: DataStorage
) : NoteDetailContract.Presenter {
    private var view: NoteDetailContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var currentNote: Note? = null
    private var comments: List<Comment> = emptyList()
    private var users: List<User> = emptyList()
    private var follows: List<String> = emptyList() // Following user IDs
    private var sourceType: SourceType = SourceType.DIRECT

    override fun attachView(view: NoteDetailContract.View) {
        this.view = view
        loadUserData()
    }

    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    private fun loadUserData() {
        presenterScope.launch {
            try {
                users = dataLoader.loadUsers()
                follows = dataLoader.loadFollows().filter { it.followerId == "user_current" }.map { it.followingId }
            } catch (e: Exception) {
                view?.showError("Failed to load user data: ${e.message}")
            }
        }
    }

    fun setSourceType(source: SourceType) {
        this.sourceType = source
    }

    override fun loadNoteDetail(noteId: String) {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val notes = dataLoader.loadNotes()
                val note = notes.find { it.id == noteId }
                if (note != null) {
                    currentNote = note
                    view?.showNote(currentNote!!)
                    loadComments(noteId)

                    // Save browsing history
                    saveBrowsingHistory(note)
                } else {
                    view?.showError("Note not found")
                }
            } catch (e: Exception) {
                view?.showError("Failed to load note: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private suspend fun saveBrowsingHistory(note: Note) {
        try {
            val browsingHistory = BrowsingHistory(
                id = "browsing_${System.currentTimeMillis()}",
                userId = "user_current",
                noteId = note.id,
                noteTitle = note.title,
                noteAuthor = note.author,
                browsedAt = Date(),
                viewType = ViewType.DETAIL,
                sourceType = sourceType,
                noteType = note.type,
                noteCoverImage = note.coverImage
            )
            dataStorage.saveBrowsingHistory(browsingHistory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loadComments(noteId: String) {
        presenterScope.launch {
            try {
                val allComments = dataLoader.loadComments()
                val noteComments = allComments.filter { it.noteId == noteId }
                comments = noteComments
                view?.showComments(comments)
            } catch (e: Exception) {
                view?.showError("Failed to load comments: ${e.message}")
            }
        }
    }

    override fun onLikeClicked(noteId: String) {
        currentNote?.let { note ->
            val newLikeStatus = !note.isLiked
            val newLikeCount = if (newLikeStatus) note.likeCount + 1 else note.likeCount - 1

            currentNote = note.copy(
                isLiked = newLikeStatus,
                likeCount = newLikeCount
            )

            view?.updateLikeStatus(newLikeStatus, newLikeCount)

            // Save like record
            if (newLikeStatus) {
                presenterScope.launch {
                    try {
                        val like = Like(
                            id = "like_${System.currentTimeMillis()}",
                            userId = "user_current",
                            targetId = noteId,
                            targetType = LikeTargetType.NOTE,
                            likedAt = Date()
                        )
                        dataStorage.saveLike(like)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onCollectClicked(noteId: String) {
        currentNote?.let { note ->
            val newCollectStatus = !note.isCollected
            val newCollectCount = if (newCollectStatus) note.collectCount + 1 else note.collectCount - 1

            currentNote = note.copy(
                isCollected = newCollectStatus,
                collectCount = newCollectCount
            )

            view?.updateCollectStatus(newCollectStatus, newCollectCount)

            // Save collection record
            if (newCollectStatus) {
                presenterScope.launch {
                    try {
                        val collection = Collection(
                            id = "collection_${System.currentTimeMillis()}",
                            userId = "user_current",
                            noteId = noteId,
                            note = note,
                            collectedAt = Date()
                        )
                        dataStorage.saveCollection(collection)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onShareClicked(noteId: String) {
        currentNote?.let { note ->
            val newShareCount = note.shareCount + 1
            currentNote = note.copy(shareCount = newShareCount)

            // Save share record
            presenterScope.launch {
                try {
                    val share = Share(
                        id = "share_${System.currentTimeMillis()}",
                        userId = "user_current",
                        noteId = noteId,
                        note = note,
                        platform = SharePlatform.SYSTEM_SHARE,
                        sharedAt = Date()
                    )
                    dataStorage.saveShare(share)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onFollowClicked(authorId: String) {
        val isCurrentlyFollowing = follows.contains(authorId)
        val newFollowStatus = !isCurrentlyFollowing

        follows = if (newFollowStatus) {
            follows + authorId
        } else {
            follows - authorId
        }

        view?.updateFollowStatus(newFollowStatus)

        // Save or remove follow record
        presenterScope.launch {
            try {
                if (newFollowStatus) {
                    // Create follow record
                    val currentUser = users.find { it.id == "user_current" }
                    val followingUser = users.find { it.id == authorId }
                    if (currentUser != null && followingUser != null) {
                        val follow = Follow(
                            id = "follow_${System.currentTimeMillis()}",
                            followerId = "user_current",
                            followingId = authorId,
                            follower = currentUser,
                            following = followingUser,
                            followedAt = Date()
                        )
                        dataStorage.saveFollow(follow)
                    }
                } else {
                    // Remove follow record
                    dataStorage.removeFollow("user_current", authorId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCommentLikeClicked(commentId: String) {
        val comment = comments.find { it.id == commentId }
        comment?.let {
            val newLikeStatus = !it.isLiked
            val newLikeCount = if (newLikeStatus) it.likeCount + 1 else it.likeCount - 1

            comments = comments.map { c ->
                if (c.id == commentId) {
                    c.copy(isLiked = newLikeStatus, likeCount = newLikeCount)
                } else {
                    c
                }
            }

            view?.showCommentLiked(commentId, newLikeStatus, newLikeCount)

            // Save comment like record
            if (newLikeStatus) {
                presenterScope.launch {
                    try {
                        val like = Like(
                            id = "like_${System.currentTimeMillis()}",
                            userId = "user_current",
                            targetId = commentId,
                            targetType = LikeTargetType.COMMENT,
                            likedAt = Date()
                        )
                        dataStorage.saveLike(like)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onAddComment(noteId: String, content: String) {
        if (content.isBlank()) return

        val currentUser = users.find { it.id == "user_current" }
        if (currentUser != null) {
            val newComment = Comment(
                id = "comment_${System.currentTimeMillis()}",
                content = content,
                author = currentUser,
                noteId = noteId,
                createdAt = Date()
            )

            comments = comments + newComment

            // Save comment to both DataStorage and DataLoader
            presenterScope.launch {
                try {
                    dataStorage.saveComment(newComment)
                    dataLoader.saveComment(newComment) // Add to cache
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            view?.showCommentAdded(newComment)

            // Update note comment count
            currentNote?.let { note ->
                currentNote = note.copy(commentCount = note.commentCount + 1)
            }
        }
    }

    override fun onReplyComment(commentId: String, content: String, replyToUserId: String?) {
        if (content.isBlank()) return

        val currentUser = users.find { it.id == "user_current" }
        val parentComment = comments.find { it.id == commentId }

        if (currentUser != null && parentComment != null) {
            val replyToUser = replyToUserId?.let { userId -> users.find { it.id == userId } }

            val newReply = Comment(
                id = "comment_${System.currentTimeMillis()}",
                content = content,
                author = currentUser,
                noteId = parentComment.noteId,
                parentCommentId = commentId,
                replyToUserId = replyToUserId,
                replyToUsername = replyToUser?.nickname,
                createdAt = Date()
            )

            comments = comments + newReply
            view?.showCommentAdded(newReply)

            // Save reply to both DataStorage and DataLoader
            presenterScope.launch {
                try {
                    dataStorage.saveComment(newReply)
                    dataLoader.saveComment(newReply) // Add to cache
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDislikeClicked(noteId: String) {
        // Save dislike record
        presenterScope.launch {
            try {
                val dislike = Dislike(
                    id = "dislike_${System.currentTimeMillis()}",
                    userId = "user_current",
                    noteId = noteId,
                    reason = DislikeReason.NOT_INTERESTED,
                    dislikedAt = Date()
                )
                dataStorage.saveDislike(dislike)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBackClicked() {
        // Handle back navigation - will be implemented in the screen
    }
}