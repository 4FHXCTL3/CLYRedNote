import subprocess
import json

def ViewLikedNotesAndCommentCheck(userId, viewCount=3, commentContent="很有用"):
    """
    检查用户是否浏览了赞过的前N条笔记并进行评论
    任务21: 在"我"页面浏览"赞过"的前3条笔记详情，并评论"很有用"
    """
    # 从设备获取点赞记录
    likes_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/likes.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 从设备获取浏览历史
    browsing_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/browsing_history.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 从设备获取评论记录
    comments_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/comments.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if likes_result.returncode != 0 or not likes_result.stdout:
        print(f"❌ Failed to read likes file")
        print(f"   Reason: ADB command failed (return code: {likes_result.returncode})")
        if likes_result.stderr:
            print(f"   Error: {likes_result.stderr}")
        return False
    if browsing_result.returncode != 0 or not browsing_result.stdout:
        print(f"❌ Failed to read browsing history file")
        print(f"   Reason: ADB command failed (return code: {browsing_result.returncode})")
        return False
    if comments_result.returncode != 0:
        print(f"❌ Failed to read comments file")
        print(f"   Reason: ADB command failed (return code: {comments_result.returncode})")
        return False

    # 解析 JSON
    try:
        likes_data = json.loads(likes_result.stdout)
        browsing_data = json.loads(browsing_result.stdout)
        comments_data = json.loads(comments_result.stdout) if comments_result.stdout else []
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse JSON data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查浏览和评论
    try:
        # 获取用户点赞的笔记列表
        user_liked_notes = [like for like in likes_data
                           if like.get('userId') == userId
                           and like.get('targetType') == 'NOTE']

        if not user_liked_notes:
            print(f"❌ No liked notes found")
            print(f"   Reason: User has not liked any notes")
            return False

        if len(user_liked_notes) < viewCount:
            print(f"❌ Not enough liked notes")
            print(f"   Reason: User has liked {len(user_liked_notes)} note(s), expected at least {viewCount}")
            return False

        # 获取前N个点赞的笔记ID
        sorted_likes = sorted(user_liked_notes, key=lambda x: x.get('likedAt', ''), reverse=True)
        target_note_ids = [like.get('targetId') for like in sorted_likes[:viewCount]]

        # 检查这些笔记是否都被浏览和评论
        success_count = 0
        for note_id in target_note_ids:
            # 检查是否浏览过（任意来源）
            has_browsed = any(item.get('userId') == userId
                             and item.get('noteId') == note_id
                             for item in browsing_data)

            # 检查是否评论过
            has_commented = any(comment.get('author', {}).get('id') == userId
                               and comment.get('noteId') == note_id
                               and comment.get('content') == commentContent
                               for comment in comments_data)

            if has_browsed and has_commented:
                success_count += 1

        if success_count >= viewCount:
            print(f"✓ Successfully viewed and commented on {viewCount} liked notes")
            print(f"   All notes have been browsed and commented with '{commentContent}'")
            return True
        else:
            print(f"❌ Not all liked notes have been viewed and commented")
            print(f"   Reason: Only {success_count} out of {viewCount} notes meet requirements")
            # Show details for each note
            for i, note_id in enumerate(target_note_ids):
                has_browsed = any(item.get('userId') == userId and item.get('noteId') == note_id for item in browsing_data)
                has_commented = any(comment.get('author', {}).get('id') == userId and comment.get('noteId') == note_id and comment.get('content') == commentContent for comment in comments_data)
                print(f"   Note {i+1} (ID: {note_id}): Browsed={'✓' if has_browsed else '✗'}, Commented={'✓' if has_commented else '✗'}")
            return False

    except Exception as e:
        print(f"❌ Error while checking liked notes")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(ViewLikedNotesAndCommentCheck(
        userId='user_current',
        viewCount=3,
        commentContent='很有用'
    ))
