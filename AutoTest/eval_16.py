import subprocess
import json
import sys
import io

# 设置 UTF-8 编码以支持 emoji 输出
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def BrowseAndInteractCheck(userId, viewCount=2, commentContent="很精彩"):
    """
    检查用户是否浏览了首页前N篇笔记并进行了点赞、收藏和评论
    任务16: 进入首页，浏览前2篇推荐笔记点击进入笔记详情，对笔记进行点赞、收藏、发送评论"很精彩"
    """
    # 从设备获取浏览历史
    browsing_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/browsing_history.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 从设备获取点赞记录
    likes_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/likes.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 从设备获取收藏记录
    collections_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/collections.json'],
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
    if browsing_result.returncode != 0 or not browsing_result.stdout:
        print(f"❌ Failed to read browsing history file")
        print(f"   Reason: ADB command failed (return code: {browsing_result.returncode})")
        if browsing_result.stderr:
            print(f"   Error: {browsing_result.stderr}")
        return False
    if likes_result.returncode != 0:
        print(f"❌ Failed to read likes file")
        print(f"   Reason: ADB command failed (return code: {likes_result.returncode})")
        return False
    if collections_result.returncode != 0:
        print(f"❌ Failed to read collections file")
        print(f"   Reason: ADB command failed (return code: {collections_result.returncode})")
        return False

    # 解析 JSON
    try:
        browsing_data = json.loads(browsing_result.stdout.strip()) if browsing_result.stdout.strip() else []
        likes_data = json.loads(likes_result.stdout.strip()) if likes_result.stdout.strip() else []
        collections_data = json.loads(collections_result.stdout.strip()) if collections_result.stdout.strip() else []
        # comments.json 可能不存在，需要容错处理
        if comments_result.returncode == 0 and comments_result.stdout and comments_result.stdout.strip():
            comments_data = json.loads(comments_result.stdout.strip())
        else:
            comments_data = []
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse JSON data")
        print(f"   Reason: Invalid JSON format")
        print(f"   Error: {e}")
        return False

    # 检查浏览、点赞、收藏和评论
    try:
        # 获取用户从首页浏览的笔记
        home_browsing = [item for item in browsing_data
                        if item.get('userId') == userId
                        and item.get('sourceType') == 'HOME_FEED']

        if len(home_browsing) < viewCount:
            print(f"❌ Not enough home feed browsing records")
            print(f"   Reason: Found {len(home_browsing)} records, expected {viewCount}")
            return False

        # 获取最近浏览的N篇笔记
        recent_browsing = sorted(home_browsing, key=lambda x: x.get('browsedAt', ''), reverse=True)[:viewCount]
        note_ids = [item.get('noteId') for item in recent_browsing]

        # 检查这些笔记是否都被点赞、收藏和评论
        success_count = 0
        for note_id in note_ids:
            has_liked = any(like.get('userId') == userId
                           and like.get('targetId') == note_id
                           and like.get('targetType') == 'NOTE'
                           for like in likes_data)

            has_collected = any(col.get('userId') == userId
                               and col.get('noteId') == note_id
                               for col in collections_data)

            has_commented = any(comment.get('author', {}).get('id') == userId
                               and comment.get('noteId') == note_id
                               and comment.get('content') == commentContent
                               for comment in comments_data)

            if has_liked and has_collected and has_commented:
                success_count += 1

        if success_count >= viewCount:
            print(f"✓ Successfully browsed {viewCount} notes and interacted with them")
            print(f"   Liked, collected and commented on {success_count} notes")
            return True
        else:
            print(f"❌ Not all notes have complete interactions")
            print(f"   Reason: Only {success_count} out of {viewCount} notes have all interactions")
            # Show details for each note
            for i, note_id in enumerate(note_ids):
                has_liked = any(like.get('userId') == userId and like.get('targetId') == note_id for like in likes_data)
                has_collected = any(col.get('userId') == userId and col.get('noteId') == note_id for col in collections_data)
                has_commented = any(comment.get('author', {}).get('id') == userId and comment.get('noteId') == note_id for comment in comments_data)
                print(f"   Note {i+1} (ID: {note_id}): Liked={'✓' if has_liked else '✗'}, Collected={'✓' if has_collected else '✗'}, Commented={'✓' if has_commented else '✗'}")
            return False

    except Exception as e:
        print(f"❌ Error while checking interactions")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(BrowseAndInteractCheck(
        userId='user_current',
        viewCount=2,
        commentContent='很精彩'
    ))
