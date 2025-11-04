# Data Storage Integration Guide

## Overview
This document explains how to integrate data storage functionality in the app to save user actions for evaluation scripts.

## Files Created

### 1. DataStorage.kt
Location: `app/src/main/java/com/example/test05/utils/DataStorage.kt`

A universal storage class that handles saving various types of user actions to internal storage:
- Browsing history
- Search history
- Likes (notes and comments)
- Collections
- Shares
- Dislikes

### 2. BrowsingHistoryStorage.kt
Location: `app/src/main/java/com/example/test05/utils/BrowsingHistoryStorage.kt`

Specialized class for saving browsing history (can be removed if using DataStorage).

### 3. Data Model Updates

#### User.kt
Added `password` field to support password setting verification (eval_10.py).

#### Collection.kt
Added `Dislike` data class and `DislikeReason` enum for dislike functionality (eval_8.py).

## Integration Steps

### Step 1: Create DataStorage Instance

In your Activity or where you create presenters, add:

```kotlin
val dataStorage = DataStorage(context)
```

### Step 2: Update NoteDetailPresenter Creation

Update NoteDetailPresenter to accept DataStorage:

```kotlin
val noteDetailPresenter = NoteDetailPresenter(
    dataLoader = JsonDataLoader(context),
    dataStorage = DataStorage(context)
)
```

### Step 3: Set Source Type (Optional)

To track where users are browsing from, set the source type:

```kotlin
noteDetailPresenter.setSourceType(SourceType.HOME_FEED)
// or
noteDetailPresenter.setSourceType(SourceType.SEARCH_RESULT)
// or other source types
```

## Data Files Generated

The following files will be created in `files/` directory:

1. `browsing_history.json` - Records when users view notes
2. `likes.json` - Records when users like notes or comments
3. `collections.json` - Records when users collect/favorite notes
4. `shares.json` - Records when users share notes
5. `dislikes.json` - Records when users click "dislike"
6. `search_history.json` - Records search queries (need to implement)

## Automatic Data Saving

The following actions are now automatically saved:

### Browsing History
- Saved when: User opens note detail page
- Data: noteId, userId, browsedAt, sourceType, viewType

### Likes
- Saved when: User clicks like button (note or comment)
- Data: targetId, userId, targetType, likedAt

### Collections
- Saved when: User clicks collect button
- Data: noteId, userId, collectedAt

### Shares
- Saved when: User clicks share button
- Data: noteId, userId, platform, sharedAt

### Dislikes
- Saved when: User clicks dislike button
- Data: noteId, userId, reason, dislikedAt

## TODO: Additional Integrations Needed

The following still need to be implemented:

### 1. Search History
Add to search functionality:
```kotlin
dataStorage.saveSearchHistory(SearchHistory(
    id = "search_${System.currentTimeMillis()}",
    userId = "user_current",
    query = searchQuery,
    searchedAt = Date()
))
```

### 2. User Profile Updates (eval_10, eval_11)
Implement in profile edit functionality to save changes to users.json.

### 3. Note Publishing (eval_12)
Implement in note creation flow to save new notes to notes.json.

## Testing

To test if data is being saved:

```bash
# Connect device via ADB
adb devices

# Check if files exist
adb exec-out run-as com.example.test05 ls files/

# View file content
adb exec-out run-as com.example.test05 cat files/browsing_history.json
```

## Evaluation Scripts

The following evaluation scripts will use these data files:

- `eval_2.py` - Uses browsing_history.json
- `eval_3.py` - Uses search_history.json and browsing_history.json
- `eval_4.py` - Uses likes.json, collections.json, and comments.json
- `eval_5.py` - Uses follows.json
- `eval_8.py` - Uses dislikes.json
- `eval_9.py` - Uses collections.json and browsing_history.json
- `eval_13.py` - Uses shares.json
- `eval_15.py` - Uses likes.json (comment likes)

## Notes

- All data files use UTF-8 encoding
- Dates are formatted as "yyyy-MM-dd'T'HH:mm:ss'Z'"
- Files are stored in app's private storage (context.filesDir)
- Initial data is loaded from assets/data/ if files don't exist
