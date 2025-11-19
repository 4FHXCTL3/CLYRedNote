import subprocess
import json
import sys
import io

# 设置 UTF-8 编码以支持 emoji 输出
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def LikeCollectCommentCheck(userId, noteKeyword):
    """
    检查用户是否对标题含指定关键词的笔记进行了点赞、收藏和评论
    任务4: 搜索并打开一篇标题含"穿搭"的笔记，对其进行点赞、收藏并评论"很有用！"
    """
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

    # 从设备获取浏览历史（用于获取笔记信息）
    browsing_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/browsing_history.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if likes_result.returncode != 0:
        print(f"❌ Failed to read likes file")
        print(f"   Reason: ADB command failed (return code: {likes_result.returncode})")
        return False
    if collections_result.returncode != 0:
        print(f"❌ Failed to read collections file")
        print(f"   Reason: ADB command failed (return code: {collections_result.returncode})")
        return False
    if browsing_result.returncode != 0:
        print(f"❌ Failed to read browsing history file")
        print(f"   Reason: ADB command failed (return code: {browsing_result.returncode})")
        return False

    # 解析 JSON
    try:
        likes_data = json.loads(likes_result.stdout.strip()) if likes_result.stdout.strip() else []
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse likes.json")
        print(f"   Error: {e}")
        return False

    try:
        collections_data = json.loads(collections_result.stdout.strip()) if collections_result.stdout.strip() else []
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse collections.json")
        print(f"   Error: {e}")
        return False

    # comments.json 可能不存在，如果不存在则为空列表
    try:
        if comments_result.returncode == 0 and comments_result.stdout.strip():
            comments_data = json.loads(comments_result.stdout.strip())
        else:
            comments_data = []
    except (json.JSONDecodeError, TypeError):
        comments_data = []

    # 从浏览历史中提取笔记信息
    try:
        browsing_data = json.loads(browsing_result.stdout.strip()) if browsing_result.stdout.strip() else []
        # 从浏览历史中构建笔记列表
        notes_data = []
        seen_note_ids = set()
        for item in browsing_data:
            note_id = item.get('noteId')
            if note_id and note_id not in seen_note_ids:
                notes_data.append({
                    'id': note_id,
                    'title': item.get('noteTitle', '')
                })
                seen_note_ids.add(note_id)
    except (json.JSONDecodeError, TypeError) as e:
        print(f"❌ Failed to parse browsing_history.json")
        print(f"   Error: {e}")
        return False

    # 查找标题包含关键词的笔记
    try:
        target_notes = [note for note in notes_data if noteKeyword in note.get('title', '')]

        if not target_notes:
            print(f"❌ No notes found with keyword '{noteKeyword}'")
            print(f"   Reason: No matching notes in the database")
            return False

        # 检查这些笔记是否被点赞、收藏和评论
        for note in target_notes:
            note_id = note.get('id')

            # 检查点赞
            has_liked = any(like.get('userId') == userId
                           and like.get('targetId') == note_id
                           and like.get('targetType') == 'NOTE'
                           for like in likes_data)

            # 检查收藏
            has_collected = any(col.get('userId') == userId
                               and col.get('noteId') == note_id
                               for col in collections_data)

            # 检查评论（内容为"很有用！"）
            has_commented = any(comment.get('author', {}).get('id') == userId
                               and comment.get('noteId') == note_id
                               and comment.get('content') == '很有用！'
                               for comment in comments_data)

            # 如果找到一个笔记同时满足三个条件，返回 True
            if has_liked and has_collected and has_commented:
                print(f"✓ Successfully liked, collected and commented on note '{note.get('title')}'")
                return True

        # 检查哪些操作缺失
        note = target_notes[0]
        note_id = note.get('id')
        has_liked = any(like.get('userId') == userId and like.get('targetId') == note_id for like in likes_data)
        has_collected = any(col.get('userId') == userId and col.get('noteId') == note_id for col in collections_data)
        has_commented = any(comment.get('author', {}).get('id') == userId and comment.get('noteId') == note_id for comment in comments_data)

        print(f"❌ Missing actions on note '{note.get('title')}'")
        print(f"   Note ID: {note_id}")
        print(f"   Liked: {'✓' if has_liked else '✗'}")
        print(f"   Collected: {'✓' if has_collected else '✗'}")
        print(f"   Commented: {'✗' if not has_commented else '✓'}")
        return False

    except Exception as e:
        print(f"❌ Error while checking interactions")
        print(f"   Reason: {e}")
        return False

if __name__ == "__main__":
    print(LikeCollectCommentCheck(
        userId='user_current',
        noteKeyword='穿搭'
    ))
