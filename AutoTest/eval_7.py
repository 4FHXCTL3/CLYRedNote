import subprocess
import json

def ViewFollowingNoteCheck(userId):
    """
    检查用户是否查看了关注列表中第一个博主的第一条笔记
    任务7: 在"我"界面进入"关注"的博主列表，找到第一个博主的第一条笔记并打开
    """
    # 从设备获取关注列表
    follows_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/follows.json'],
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

    # 从设备获取笔记列表
    notes_result = subprocess.run(
        ['adb', 'exec-out', 'run-as', 'com.example.test05', 'cat', 'files/notes.json'],
        capture_output=True,
        encoding='utf-8',
        errors='replace'
    )

    # 检查命令是否成功执行
    if (follows_result.returncode != 0 or browsing_result.returncode != 0 or
        notes_result.returncode != 0):
        return False

    # 解析 JSON
    try:
        follows_data = json.loads(follows_result.stdout) if follows_result.stdout else []
        browsing_data = json.loads(browsing_result.stdout) if browsing_result.stdout else []
        notes_data = json.loads(notes_result.stdout) if notes_result.stdout else []
    except (json.JSONDecodeError, TypeError):
        return False

    # 检查是否查看了关注博主的笔记
    try:
        # 获取用户的关注列表，按关注时间排序
        user_follows = [f for f in follows_data if f.get('followerId') == userId]
        if not user_follows:
            return False

        user_follows.sort(key=lambda x: x.get('followedAt', ''))
        first_following_id = user_follows[0].get('followingId')

        # 获取该博主的第一条笔记
        author_notes = [note for note in notes_data
                       if note.get('author', {}).get('id') == first_following_id]
        if not author_notes:
            return False

        author_notes.sort(key=lambda x: x.get('createdAt', ''))
        first_note_id = author_notes[0].get('id')

        # 检查浏览历史中是否有查看该笔记的记录，且来源为用户主页
        has_viewed = any(item.get('userId') == userId
                        and item.get('noteId') == first_note_id
                        and item.get('sourceType') == 'USER_PROFILE'
                        for item in browsing_data)

        return has_viewed

    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    print(ViewFollowingNoteCheck(
        userId='user_current'
    ))
