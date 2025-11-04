import subprocess
import json

def LikeCollectCommentCheck(userId, noteKeyword):
    """
    检查用户是否对标题含指定关键词的笔记进行了点赞、收藏和评论
    任务4: 搜索并打开一篇标题含"美妆"的笔记，对其进行点赞、收藏并评论"很有用！"
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

    # 从设备获取笔记列表
    notes_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/notes.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if (likes_result.returncode != 0 or collections_result.returncode != 0 or
        comments_result.returncode != 0 or notes_result.returncode != 0):
        return False

    # 解析 JSON
    try:
        likes_data = json.loads(likes_result.stdout) if likes_result.stdout else []
        collections_data = json.loads(collections_result.stdout) if collections_result.stdout else []
        comments_data = json.loads(comments_result.stdout) if comments_result.stdout else []
        notes_data = json.loads(notes_result.stdout) if notes_result.stdout else []
    except (json.JSONDecodeError, TypeError):
        return False

    # 查找标题包含关键词的笔记
    try:
        target_notes = [note for note in notes_data if noteKeyword in note.get('title', '')]

        if not target_notes:
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
                return True

        return False

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(LikeCollectCommentCheck(
        userId='user_current',
        noteKeyword='美妆'
    ))
